#!/usr/bin/env python3
"""UI-only search regression using a retained local O3 fixture manifest; no product setup."""
import argparse
import fcntl
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
METHOD = 'ApprovedRestaurantBrandSearchUiTest#findsRenamedBrandAndKeepsOutletSearch'

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--app-url', required=True)
    parser.add_argument('--fixture-manifest', required=True, type=Path)
    args = parser.parse_args()
    url = urlparse(args.app_url)
    if url.scheme != 'https' or not url.hostname or not url.hostname.endswith('.trycloudflare.com'):
        raise ValueError('Use the current public Oracle HTTPS tunnel')
    fixture = json.loads(args.fixture_manifest.read_text())
    phone = fixture.get('phoneCandidates', {}).get('restaurant.lifecycle', '')
    if not re.fullmatch(r'9999[0-9]{6}', phone):
        raise ValueError('A retained restaurant lifecycle fixture is required')
    folder = ROOT / 'target/business-platform/o3'
    locks = ROOT / 'target/registration'
    folder.mkdir(parents=True, exist_ok=True)
    locks.mkdir(parents=True, exist_ok=True)
    with (locks / 'runner.lock').open('w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        report = dict(runId=secrets.token_hex(8), appUrl=args.app_url, selectedCases=['brand-search'],
                      selectedMethod=METHOD, fixtureSource=str(args.fixture_manifest),
                      phoneCandidates={'restaurant.search': phone}, dataPolicy='retain',
                      allocationPolicy='reuse previously approved local owned fixture; no server lookup',
                      cleanupPerformed=False, externalDatabaseOrRedisAccess=False)
        manifest = folder / f"allocation-{report['runId']}.json"
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        command = ['mvn', '-q', '-Dtest=' + METHOD, '-Dapp.url=' + args.app_url,
                   '-Dbp.o3.preflight=true', '-Dbp.o3.phone.restaurant.search=' + phone,
                   '-Dcustomer.phone=8000000001', '-Drestaurant.phone=9000000001',
                   '-Drider.phone=7000000001', '-Dadmin.phone=1000000001',
                   '-Dheadless=true', '-Dslow.mo=0', '-Drecord.video=false',
                   '-DexcludedGroups=slow-auth,auth-rate-limit', 'test']
        print('Running read-only UI brand search; retained manifest: ' + str(manifest), flush=True)
        result = subprocess.run(command, cwd=ROOT,
                                env={**os.environ, 'PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD': '1'})
        report['exitCode'] = result.returncode
        manifest.write_text(json.dumps(report, indent=2) + '\n')
        return result.returncode

if __name__ == '__main__':
    raise SystemExit(main())
