import contextlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import run_business_platform_o45_e2e as runner

class CombinedUiRunnerTest(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory(); self.addCleanup(self.folder.cleanup)
        self.commands = []; self.exit_code = 0
    def execute(self, command, **kwargs):
        self.commands.append(command); self.assertEqual('mvn', command[0]); self.assertNotIn('input', kwargs)
        return subprocess.CompletedProcess(command, self.exit_code)
    def run_case(self, case=None, url='https://dev.trycloudflare.com/'):
        args = ['runner', '--app-url', url] + (['--only', case] if case else [])
        with patch.object(runner, 'ROOT', Path(self.folder.name)), patch.object(runner.subprocess, 'run', side_effect=self.execute), \
             patch('sys.argv', args), contextlib.redirect_stdout(io.StringIO()): return runner.main()
    def manifest(self):
        return json.loads(next((Path(self.folder.name) / 'target/business-platform/o45').glob('allocation-*.json')).read_text())
    def test_full_selection_uses_only_named_ui_gates_and_retains_every_candidate(self):
        self.assertEqual(0, self.run_case()); self.assertEqual(1, len(self.commands))
        command = self.commands[0]; self.assertEqual('test', command[-1]); self.assertIn('-Dapp.url=https://dev.trycloudflare.com', command)
        self.assertIn('-Dbp.o45.preflight=true', command); self.assertIn('-DexcludedGroups=slow-auth,auth-rate-limit', command)
        report = self.manifest(); self.assertEqual(8, len(report['phoneCandidates'])); self.assertEqual(8, len(set(report['phoneCandidates'].values())))
        self.assertEqual(runner.O4 + runner.O5, report['selectedCases']); self.assertFalse(report['cleanupPerformed']); self.assertFalse(report['externalDatabaseOrRedisAccess'])
    def test_o4_selection_includes_owned_brand_and_member_fixtures(self):
        self.assertEqual(0, self.run_case('o4')); self.assertEqual(runner.O4, self.manifest()['selectedCases'])
        self.assertEqual({'fresh.customer', 'entitlement.owner', 'entitlement.member', 'suspension'}, set(self.manifest()['phoneCandidates']))
    def test_read_only_launcher_requires_no_disposable_person(self):
        self.assertEqual(0, self.run_case('launcher')); self.assertEqual({}, self.manifest()['phoneCandidates'])
        self.assertIn('-Dtest=PortalLauncherUiTest', self.commands[0])
    def test_failure_does_not_delete_created_data_or_mask_test_exit(self):
        self.exit_code = 9; self.assertEqual(9, self.run_case('membership'))
        self.assertEqual(9, self.manifest()['exitCode']); self.assertEqual('retain', self.manifest()['dataPolicy'])
    def test_non_dev_url_is_refused_before_any_test(self):
        with self.assertRaises(ValueError): self.run_case(url='https://production.example')
        self.assertEqual([], self.commands)

if __name__ == '__main__': unittest.main()
