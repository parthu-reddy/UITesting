#!/usr/bin/env python3
"""Run registration coverage on unused Dev phones and retain every created account and session.

The runner allocates phones and records a manifest using read-only queries. It does not
retire users, deactivate partners/outlets, revoke sessions or delete test data.
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
    selection=parser.add_mutually_exclusive_group()
    selection.add_argument('--only', choices=['customer','rider','restaurant'])
    selection.add_argument('--session-case', choices=['device-removal','limit-cancel','limit-replacement'])
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
        raise RuntimeError('Registration runs require an exclusively Dev deployment')
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
        report={'runId':run_id,'startedAt':started,'phones':phones,'retired':False,'cleanupPerformed':False,'dataPolicy':'retain'}
        if args.session_case: report['sessionCase']=args.session_case
        manifest=target/(run_id+'.json')
        manifest.write_text(json.dumps(report,indent=2)+'\n')
        methods={'customer':'customerRegistrationFlow','rider':'riderRegistrationFlow','restaurant':'restaurantRegistrationFlow'}
        selector='RegistrationUiTest'+('#'+methods[args.only] if args.only else '')
        if args.session_case:
            cases={'device-removal':'removeOwnSecondDeviceRevokesOnlyThatSession','limit-cancel':'sessionLimitCancelPreservesExistingDevices','limit-replacement':'sessionLimitReplacementRevokesSelectedDeviceOnly'}
            selector='SessionManagementTest#'+cases[args.session_case]
        command=['mvn','-q','-Dtest='+selector,'-Dapp.url='+args.app_url,'-Dregistration.enabled=true','-Dregistration.preflight=true','-Dheadless=true','-Dslow.mo=0','-Drecord.video=false','-De2e.otp.enabled=false','-DexcludedGroups=slow-auth,auth-rate-limit']
        command += ['-Dregistration.'+persona+'.phone='+phone for persona,phone in phones.items()]
        if args.session_case:
            command += ['-Dsession.fixture.case='+args.session_case,'-Dcustomer.phone='+phones['customer']]
        command.append('test')
        print('Running existing registration tests; allocation manifest: '+str(manifest),flush=True)
        code=1
        try:
            code=subprocess.run(command,cwd=ROOT).returncode
        finally:
            # Record created identities for inspection; never change the deployment after the tests.
            phone_list=','.join("'"+p+"'" for p in phones.values())
            report.update(exitCode=code)
            try:
                owned=sql('identity',f"""BEGIN READ ONLY;
SELECT COALESCE(json_agg(json_build_object('id',id,'phone',phone_number,'created',created_at)), '[]')
FROM users WHERE phone_number IN ({phone_list}); COMMIT;""")
                accounts=json.loads(owned)
                report.update(accountIds=[a['id'] for a in accounts],accounts=accounts)
            except Exception as audit_error:
                report['accountAuditError']=str(audit_error)
            manifest.write_text(json.dumps(report,indent=2)+'\n')
            print('Registration accounts and sessions retained; no cleanup performed',flush=True)
        return code

if __name__=='__main__':
    sys.exit(main())
