#!/usr/bin/env python3
"""Allocate two unused customer phones read-only, then run O1 through the deployed gateway.

Requires owner deployment of checkpoint34 and O1. Uses normal browser signup/Dev Autofill Code;
retains every created record. Never deploys, seeds, resets or cleans up the server.
"""
import argparse
import fcntl
import json
import os
from pathlib import Path
import secrets
import shlex
import subprocess

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = 'Food Delivery.nosync/Deployment'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    args = parser.parse_args()
    ssh = ['ssh', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=20', '-i',
           os.environ.get('SSH_KEY', '/Users/parthureddy/Documents/OracleSSH/ssh-key-2026-08-16.key'),
           os.environ.get('VM', 'ubuntu@140.245.234.137')]

    def remote(command, data=None):
        result = subprocess.run(ssh + ['cd ' + shlex.quote(COMPOSE) + ' && ' + command],
                                input=data, text=True, capture_output=True, timeout=90)
        if result.returncode:
            raise RuntimeError('Read-only deployment preflight failed')
        return result.stdout.strip()

    def sql(statement):
        return remote('docker compose exec -T -u postgres postgres psql -X -qAt '
                      '-v ON_ERROR_STOP=1 -d identity_db', 'BEGIN READ ONLY;\n' + statement + '\nCOMMIT;')

    profiles = remote('docker compose exec -T identity-service printenv SPRING_PROFILES_ACTIVE').lower().split(',')
    if 'dev' not in profiles or 'prod' in profiles:
        raise RuntimeError('O1 E2E requires an exclusively Dev deployment')
    if sql("SELECT to_regclass('public.organisations') IS NOT NULL;") != 't':
        raise RuntimeError('O1 organisation migration is not deployed; stop before signup')
    folder = ROOT / 'target' / 'business-platform' / 'o1'
    folder.mkdir(parents=True, exist_ok=True)
    # Coordinate allocation with the existing registration runner.
    registration = ROOT / 'target' / 'registration'
    registration.mkdir(parents=True, exist_ok=True)
    with (registration / 'runner.lock').open('w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        phones = {}
        for label in ['a', 'b']:
            for _ in range(100):
                candidate = '8999' + f'{secrets.randbelow(1000000):06d}'
                if candidate not in phones.values() and sql(f"SELECT EXISTS(SELECT FROM users WHERE phone_number='{candidate}');") == 'f':
                    phones[label] = candidate
                    break
            else:
                raise RuntimeError('Could not allocate unused customer phones')
        report = {'runId': secrets.token_hex(8), 'startedAt': sql('SELECT clock_timestamp();'),
                  'phones': phones, 'dataPolicy': 'retain', 'cleanupPerformed': False}
        manifest = folder / ('allocation-' + report['runId'] + '.json')
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        command = ['mvn', '-q', '-Dtest=OrganisationLifecycleApiTest', '-Dapp.url=' + args.app_url,
                   '-Dbp.o1.preflight=true', '-Dbp.o1.phone.a=' + phones['a'], '-Dbp.o1.phone.b=' + phones['b'],
                   '-Dheadless=true', '-Dslow.mo=0', '-De2e.otp.enabled=false',
                   '-DexcludedGroups=slow-auth,auth-rate-limit', 'test']
        print('Running O1; allocation manifest: ' + str(manifest), flush=True)
        code = 1
        try:
            code = subprocess.run(command, cwd=ROOT, env={**os.environ, 'PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD': '1'}).returncode
        finally:
            report['exitCode'] = code
            try:
                phone_list = ','.join("'" + p + "'" for p in phones.values())
                report['accounts'] = json.loads(sql("SELECT COALESCE(json_agg(json_build_object('id',id,'phone',phone_number)), '[]') FROM users WHERE phone_number IN (" + phone_list + ');'))
            except Exception:
                report['accountAuditError'] = 'Read-only account audit failed; inspect owned phones'
            manifest.write_text(json.dumps(report, indent=2) + '\n')
            print('O1 data retained; no server cleanup performed', flush=True)
        return code


if __name__ == '__main__':
    raise SystemExit(main())
