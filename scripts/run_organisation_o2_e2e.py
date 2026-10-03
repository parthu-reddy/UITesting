#!/usr/bin/env python3
"""Read-only Oracle Dev schema/seed preflight, unused phone allocation and O2 browser permission test.

Run only after O2 clean deployment/fresh seed. Never publishes, resets, seeds or cleans up server data.
The subsequent test uses normal Restaurant Partner signup and the visible Dev Autofill Code control.
"""
import argparse
import fcntl
import json
import os
from pathlib import Path
import secrets
import shlex
import subprocess
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = 'Food Delivery.nosync/Deployment'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    args = parser.parse_args()
    url = urlparse(args.app_url)
    if url.scheme != 'https' or not url.hostname or not url.hostname.endswith('.trycloudflare.com'):
        raise ValueError('Use the current public Oracle HTTPS tunnel from deployment-context.md')
    ssh = ['ssh', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=20', '-i',
           os.environ.get('SSH_KEY', '/Users/parthureddy/Documents/OracleSSH/ssh-key-2026-08-16.key'),
           os.environ.get('VM', 'ubuntu@140.245.234.137')]

    def remote(command, data=None):
        result = subprocess.run(ssh + ['cd ' + shlex.quote(COMPOSE) + ' && ' + command],
                                input=data, text=True, capture_output=True, timeout=60)
        if result.returncode:
            raise RuntimeError('Read-only O2 deployment preflight failed; no signup attempted')
        return result.stdout.strip()

    def sql(database, statement):
        if database not in {'identity_db', 'restaurant_db', 'chat_db'}:
            raise ValueError('Unexpected preflight database')
        return remote('docker compose exec -T -u postgres postgres psql -X -qAt '
                      '-v ON_ERROR_STOP=1 -d ' + database, 'BEGIN READ ONLY;\n' + statement + '\nCOMMIT;')

    for service in ['identity-service', 'restaurant-service', 'chat-service']:
        profiles = [p.strip().lower() for p in remote('docker compose exec -T ' + service +
                    ' printenv SPRING_PROFILES_ACTIVE').split(',')]
        if 'dev' not in profiles or 'prod' in profiles:
            raise RuntimeError('O2 browser mutations require exclusively Dev services')
    if sql('restaurant_db', "SELECT EXISTS(SELECT FROM information_schema.columns WHERE table_schema='public' "
           "AND table_name='brands' AND column_name='organisation_id' AND is_nullable='NO') "
           "AND EXISTS(SELECT FROM pg_indexes WHERE schemaname='public' AND indexname='uq_brands_organisation');") != 't':
        raise RuntimeError('O2 restaurant ownership schema is not deployed')
    if sql('chat_db', "SELECT count(*)=2 FROM information_schema.columns WHERE table_schema='public' AND "
           "((table_name='session_participants' AND column_name='entity_id') OR "
           "(table_name='messages' AND column_name='sender_entity_id')) AND is_nullable='NO';") != 't':
        raise RuntimeError('O2 outlet-entity chat schema is not deployed')
    organisations = json.loads(sql('identity_db', "SELECT COALESCE(json_agg(json_build_object('id',o.id,'role',m.role)), '[]') "
        "FROM organisations o JOIN organisation_members m ON m.organisation_id=o.id JOIN users u ON u.id=m.user_id "
        "WHERE u.phone_number='9000000001' AND m.status='ACTIVE' AND o.status='ACTIVE';"))
    if len(organisations) != 1 or organisations[0]['role'] != 'OWNER':
        raise RuntimeError('Seed owner must have exactly one active organisation')
    org = organisations[0]['id']
    # IDs originate in a UUID database column; validate before constructing a follow-up SQL literal.
    from uuid import UUID
    org = str(UUID(org))
    if sql('restaurant_db', "SELECT count(*)=1 AND bool_and(name='Brand 1') FROM brands "
           "WHERE organisation_id='" + org + "';") != 't':
        raise RuntimeError('Seed owner organisation must own Brand 1 only')
    folder = ROOT / 'target' / 'business-platform' / 'o2'
    folder.mkdir(parents=True, exist_ok=True)
    registration = ROOT / 'target' / 'registration'
    registration.mkdir(parents=True, exist_ok=True)
    with (registration / 'runner.lock').open('w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        for _ in range(100):
            phone = '9999' + f'{secrets.randbelow(1000000):06d}'
            if sql('identity_db', f"SELECT EXISTS(SELECT FROM users WHERE phone_number='{phone}');") == 'f':
                break
        else:
            raise RuntimeError('No unused O2 restaurant phone could be allocated')
        report = {'runId': secrets.token_hex(8), 'startedAt': sql('identity_db', 'SELECT clock_timestamp();'),
                  'phone': phone, 'organisationId': org, 'dataPolicy': 'retain', 'cleanupPerformed': False}
        manifest = folder / ('allocation-' + report['runId'] + '.json')
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        command = ['mvn', '-q', '-Dtest=OrganisationRestaurantAccessTest#organisationRestaurantAccess',
                   '-Dapp.url=' + args.app_url, '-Dbp.o2.preflight=true', '-Dbp.o2.phone=' + phone,
                   '-Dheadless=true', '-Dslow.mo=0', '-De2e.otp.enabled=false',
                   '-DexcludedGroups=slow-auth,auth-rate-limit', 'test']
        print('Running O2; retained allocation manifest: ' + str(manifest), flush=True)
        code = 1
        try:
            code = subprocess.run(command, cwd=ROOT, env={**os.environ, 'PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD': '1'}).returncode
        finally:
            report['exitCode'] = code
            try:
                report['accounts'] = json.loads(sql('identity_db', "SELECT COALESCE(json_agg(json_build_object('id',id,'phone',phone_number)), '[]') "
                    f"FROM users WHERE phone_number='{phone}';"))
            except Exception:
                report['accountAuditError'] = 'Read-only account audit failed; inspect retained phone manifest'
            manifest.write_text(json.dumps(report, indent=2) + '\n')
            print('O2 fixtures retained; no cleanup performed', flush=True)
        return code


if __name__ == '__main__':
    raise SystemExit(main())
