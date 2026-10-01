#!/usr/bin/env python3
"""Run existing registration coverage on unused Dev phones, then retire only this run's accounts.

Accounts and onboarding records remain for audit; they are never reused or presented as seeds.
No order, payout, wallet, KYC-provider or financial history is deleted.
"""
import argparse
import fcntl
import json
import os
from pathlib import Path
import secrets
import shlex
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = 'Food Delivery.nosync/Deployment'

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    parser.add_argument('--only', choices=['customer','rider','restaurant'])
    args=parser.parse_args()
    ssh=['ssh','-o','BatchMode=yes','-o','ConnectTimeout=20','-i',os.environ.get('SSH_KEY','/Users/parthureddy/Documents/OracleSSH/ssh-key-2026-08-16.key'),os.environ.get('VM','ubuntu@140.245.234.137')]
    def remote(command, data=None):
        r=subprocess.run(ssh+['cd '+shlex.quote(COMPOSE)+' && '+command], input=data, text=True, capture_output=True, timeout=90)
        if r.returncode: raise RuntimeError(r.stderr.strip() or 'Remote command failed')
        return r.stdout.strip()
    def sql(db, statement):
        # Peer authentication inside the container; neither SQL nor credentials enter shell text.
        return remote(f'docker compose exec -T -u postgres postgres psql -X -qAt -v ON_ERROR_STOP=1 -d {db}_db', statement)
    profiles=remote('docker compose exec -T identity-service printenv SPRING_PROFILES_ACTIVE').lower().split(',')
    if 'dev' not in profiles or 'prod' in profiles:
        raise RuntimeError('Disposable registration runs require an exclusively Dev deployment')
    target=ROOT/'target'/'registration'
    target.mkdir(parents=True,exist_ok=True)
    with (target/'runner.lock').open('w') as lock:
        fcntl.flock(lock,fcntl.LOCK_EX)
        started=sql('identity','BEGIN READ ONLY; SELECT clock_timestamp(); COMMIT;')
        phones={}
        for persona,prefix in [('customer','8999'),('rider','7999'),('restaurant','9999')]:
            for _ in range(100):
                candidate=prefix+f'{secrets.randbelow(1000000):06d}'
                if sql('identity',f"BEGIN READ ONLY; SELECT EXISTS(SELECT FROM users WHERE phone_number='{candidate}'); COMMIT;") == 'f':
                    phones[persona]=candidate;break
            else: raise RuntimeError('Could not allocate an unused registration phone')
        run_id=secrets.token_hex(8)
        report={'runId':run_id,'startedAt':started,'phones':phones,'retired':False}
        manifest=target/(run_id+'.json')
        manifest.write_text(json.dumps(report,indent=2)+'\n')
        methods={'customer':'customerRegistrationFlow','rider':'riderRegistrationFlow','restaurant':'restaurantRegistrationFlow'}
        selector='RegistrationUiTest'+('#'+methods[args.only] if args.only else '')
        command=['mvn','-q','-Dtest='+selector,'-Dapp.url='+args.app_url,'-Dregistration.enabled=true','-Dregistration.preflight=true','-Dheadless=true','-Dslow.mo=0','-Drecord.video=false','-De2e.otp.enabled=false']
        command += ['-Dregistration.'+persona+'.phone='+phone for persona,phone in phones.items()]
        command.append('test')
        print('Running existing registration tests; allocation manifest: '+str(manifest),flush=True)
        code=1
        try:
            code=subprocess.run(command,cwd=ROOT).returncode
        finally:
            # Phones were absent at preflight and are never shared with baseline fixtures.
            # Fail closed if an older account or unexpected role appeared instead of our signup.
            phone_list=','.join("'"+p+"'" for p in phones.values())
            owned=sql('identity',f"""BEGIN READ ONLY;
SELECT COALESCE(json_agg(json_build_object('id',id,'phone',phone_number,'created',created_at)), '[]')
FROM users WHERE phone_number IN ({phone_list}); COMMIT;""")
            accounts=json.loads(owned)
            for account in accounts:
                if sql('identity',f"BEGIN READ ONLY; SELECT created_at >= '{started}'::timestamptz FROM users WHERE id='{account['id']}'; COMMIT;") != 't':
                    raise RuntimeError('Cleanup refused: account predates this run')
            ids=','.join("'"+a['id']+"'" for a in accounts)
            if accounts:
                # Keep a delivery in progress untouched; signup never requires online duty.
                sql('delivery',f"""BEGIN;
DO $$ BEGIN IF EXISTS(SELECT FROM delivery_executives WHERE id IN ({ids}) AND status <> 'OFFLINE')
THEN RAISE EXCEPTION 'Cleanup refused: disposable rider is not offline'; END IF; END $$;
UPDATE delivery_executives SET is_active=false WHERE id IN ({ids}); COMMIT;""")
                sql('restaurant',f"BEGIN; UPDATE outlets SET is_active=false WHERE brand_id IN (SELECT id FROM brands WHERE owner_id IN ({ids})); COMMIT;")
                # Revoke any remaining sessions, including those from a failed browser teardown.
                for account in accounts:
                    key='USER_SESSIONS:'+account['id']
                    raw=remote('docker compose exec -T redis redis-cli --raw GET '+shlex.quote(key))
                    sessions=json.loads(raw) if raw else []
                    for session in sessions:
                        sid=session['sessionId']
                        if not isinstance(sid,str) or not all(c.isalnum() or c=='-' for c in sid):
                            raise RuntimeError('Invalid session ID during cleanup')
                        remote('docker compose exec -T redis redis-cli SET '+shlex.quote('BLACKLIST:SESSION:'+sid)+' true EX 2592000')
                    remote('docker compose exec -T redis redis-cli DEL '+shlex.quote(key))
                sql('identity',f"BEGIN; UPDATE users SET is_active=false WHERE id IN ({ids}) AND phone_number IN ({phone_list}); COMMIT;")
            report.update(retired=True,exitCode=code,accountIds=[a['id'] for a in accounts])
            manifest.write_text(json.dumps(report,indent=2)+'\n')
            print('Disposable accounts retired; audit and onboarding records retained',flush=True)
        return code

if __name__=='__main__':
    sys.exit(main())
