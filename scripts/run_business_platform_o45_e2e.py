#!/usr/bin/env python3
"""Run combined O4/O5 gates through the public Oracle Dev UI; retain all dummy fixtures."""
import argparse
from datetime import datetime, timezone
import fcntl
import json
import os
from pathlib import Path
import secrets
import subprocess
from urllib.parse import urlparse
from run_registration_e2e import candidate

ROOT = Path(__file__).resolve().parents[1]
CASES = {
    'login': ('OneLoginEntitlementsTest', [('fresh.customer', '8999')]),
    'admin': ('AdminStepUpTest', []),
    'revocation': ('EntitlementRevocationTest#acceptedStaffApprovalAndRemovalRefreshOnNormalUiRequests', [('entitlement.owner', '9999'), ('entitlement.member', '8999')]),
    'suspension': ('EntitlementRevocationTest#ownBrandSuspensionAndReinstatementChangeAccessAndDiscovery', [('suspension', '9999')]),
    'launcher': ('PortalLauncherUiTest', []),
    'membership': ('BusinessHubOrganisationUiTest', [('hub.owner', '9999'), ('hub.member', '8999')]),
    'restaurant': ('RestaurantApplicationWizardUiTest', [('restaurant', '9999')]),
    'delivery': ('DeliveryOnboardingUiTest', [('delivery', '7999')]),
    'csp': ('CspSmokeUiTest', []),
}
O4 = ['login', 'admin', 'revocation', 'suspension']
O5 = ['launcher', 'membership', 'restaurant', 'delivery', 'csp']

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    parser.add_argument('--only', choices=['o4', 'o5', *CASES])
    parser.add_argument('--admin-phone', choices=['1000000001', '1000000002'], default='1000000001',
                        help='Use an existing seeded staff actor; ordinary UI step-up limits still apply')
    args = parser.parse_args()
    url = urlparse(args.app_url)
    if url.scheme != 'https' or not url.hostname or not url.hostname.endswith('.trycloudflare.com') or url.username or url.password:
        raise ValueError('Use the current public Oracle Dev HTTPS tunnel')
    selected = O4 if args.only == 'o4' else O5 if args.only == 'o5' else [args.only] if args.only else O4 + O5
    folder = ROOT / 'target/business-platform/o45'
    lockdir = ROOT / 'target/registration'
    folder.mkdir(parents=True, exist_ok=True); lockdir.mkdir(parents=True, exist_ok=True)
    with (lockdir / 'runner.lock').open('w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        used = set()
        for path in folder.glob('allocation-*.json'):
            prior = json.loads(path.read_text()); used.update(prior.get('phoneCandidates', {}).values())
        phones = {key: candidate(prefix, used) for case in selected for key, prefix in CASES[case][1]}
        run_id = secrets.token_hex(8); manifest = folder / ('allocation-' + run_id + '.json')
        report = dict(runId=run_id, startedAt=datetime.now(timezone.utc).isoformat(), appUrl=args.app_url,
                      selectedCases=selected, phoneCandidates=phones, adminPhone=args.admin_phone, uiOnly=True,
                      allocationPolicy='local candidates; fresh profile completion required in UI',
                      dataPolicy='retain', cleanupPerformed=False, externalDatabaseOrRedisAccess=False)
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        command = ['mvn', '-q', '-Dtest=' + ','.join(CASES[case][0] for case in selected),
                   '-Dapp.url=' + args.app_url.rstrip('/'), '-Dbp.o45.preflight=true',
                   '-Dcustomer.phone=8000000001', '-Drestaurant.phone=9000000001', '-Drider.phone=7000000001', '-Dadmin.phone=' + args.admin_phone,
                   '-Dheadless=true', '-Dslow.mo=0', '-Drecord.video=false', '-De2e.otp.enabled=false', '-DexcludedGroups=slow-auth,auth-rate-limit']
        command.extend('-Dbp.o45.phone.' + key + '=' + phone for key, phone in phones.items()); command.append('test')
        print('Running combined UI gate; retained candidates: ' + str(manifest), flush=True)
        code = 1
        try:
            code = subprocess.run(command, cwd=ROOT, env={**os.environ, 'PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD': '1'}).returncode
        except OSError as failure:
            report['runnerError'] = type(failure).__name__; raise
        finally:
            report['exitCode'] = code; report['finishedAt'] = datetime.now(timezone.utc).isoformat()
            manifest.write_text(json.dumps(report, indent=2) + '\n')
            print('All owned business fixtures retained; no server cleanup performed', flush=True)
        return code

if __name__ == '__main__': raise SystemExit(main())
