#!/usr/bin/env python3
"""Run retained fresh-person journeys through the public Oracle Dev UI.

Phones are local candidates. The browser must prove fresh profile completion or stop on collision.
No server lookup, backend request or automatic cleanup is performed by this runner.
"""
import argparse
from datetime import datetime, timezone
import fcntl
import json
import os
from pathlib import Path
import secrets
import subprocess
import live_run_lock
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
REGISTRATION = {'customer': 'customerRegistrationFlow', 'rider': 'riderRegistrationFlow', 'restaurant': 'restaurantRegistrationFlow'}
SESSION = {'device-removal': 'removeOwnSecondDeviceRevokesOnlyThatSession', 'limit-cancel': 'sessionLimitCancelPreservesExistingDevices', 'limit-replacement': 'sessionLimitReplacementRevokesSelectedDeviceOnly'}
PREFIXES = {'customer': '8999', 'rider': '7999', 'restaurant': '9999'}

def candidate(prefix, used):
    for _ in range(1000):
        phone = prefix + f'{secrets.randbelow(1_000_000):06d}'
        if phone not in used:
            used.add(phone)
            return phone
    raise RuntimeError('Unable to allocate a new local phone candidate')

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    selection = parser.add_mutually_exclusive_group()
    selection.add_argument('--only', choices=REGISTRATION)
    selection.add_argument('--session-case', choices=SESSION)
    args = parser.parse_args()
    url = urlparse(args.app_url)
    if url.scheme != 'https' or not url.hostname or not url.hostname.endswith('.trycloudflare.com') or url.username or url.password:
        raise ValueError('Use the current public Oracle Dev HTTPS tunnel')
    folder = ROOT / 'target' / 'registration'
    folder.mkdir(parents=True, exist_ok=True)
    with (folder / 'runner.lock').open('w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        used = set()
        for path in folder.glob('*.json'):
            try:
                prior = json.loads(path.read_text())
                used.update(prior.get('phoneCandidates', prior.get('phones', {})).values())
            except (ValueError, AttributeError, TypeError):
                raise RuntimeError(f'Cannot read retained candidate manifest: {path.name}') from None
        selected = ['customer'] if args.session_case else [args.only] if args.only else list(REGISTRATION)
        phones = {persona: candidate(PREFIXES[persona], used) for persona in selected}
        run_id = secrets.token_hex(8)
        manifest = folder / (run_id + '.json')
        report = dict(runId=run_id, startedAt=datetime.now(timezone.utc).isoformat(), appUrl=args.app_url,
                      selectedCases=selected, sessionCase=args.session_case, phoneCandidates=phones,
                      allocationPolicy='local candidates; fresh profile completion required in UI',
                      dataPolicy='retain', retired=False, cleanupPerformed=False, externalDatabaseOrRedisAccess=False)
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        selector = ('SessionManagementTest#' + SESSION[args.session_case]) if args.session_case else (
            'RegistrationUiTest#' + '+'.join(REGISTRATION[persona] for persona in selected))
        command = ['mvn', '-q', '-Dtest=' + selector, '-Dapp.url=' + args.app_url,
                   '-Dregistration.enabled=true', '-Dregistration.preflight=true', '-Dheadless=true',
                   '-Dslow.mo=0', '-Drecord.video=false', '-De2e.otp.enabled=false', '-DexcludedGroups=slow-auth,auth-rate-limit']
        command += ['-Dregistration.' + persona + '.phone=' + phone for persona, phone in phones.items()]
        if args.session_case:
            command += ['-Dsession.fixture.case=' + args.session_case, '-Dcustomer.phone=' + phones['customer']]
        command.append('test')
        print('Running UI-only registration; retained candidates: ' + str(manifest), flush=True)
        code = 1
        try:
            with live_run_lock.held(ROOT):
                code = subprocess.run(command, cwd=ROOT, env={**os.environ, 'PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD': '1'}).returncode
        except OSError as failure:
            report['runnerError'] = type(failure).__name__
            raise
        finally:
            report['exitCode'] = code
            report['finishedAt'] = datetime.now(timezone.utc).isoformat()
            manifest.write_text(json.dumps(report, indent=2) + '\n')
            print('Accounts, sessions and candidate manifest retained; no cleanup performed', flush=True)
        return code

if __name__ == '__main__':
    raise SystemExit(main())
