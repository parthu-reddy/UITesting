import contextlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import run_registration_e2e as runner

class RegistrationRunnerTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.commands = []
        self.code = 0
    def execute(self, command, **kwargs):
        self.commands.append(command)
        self.assertEqual('mvn', command[0], 'Runner may launch only UI tests')
        self.assertNotIn('input', kwargs)
        return subprocess.CompletedProcess(command, self.code)
    def run_fixture(self, selection=None, url='https://dev-verified.trycloudflare.com'):
        args = ['--only', 'customer'] if selection is None else selection
        with patch.object(runner, 'ROOT', Path(self.temp.name)), patch.object(runner.subprocess, 'run', side_effect=self.execute), \
             patch('sys.argv', ['runner', '--app-url', url] + args), contextlib.redirect_stdout(io.StringIO()):
            return runner.main()
    def manifest(self):
        return json.loads(next((Path(self.temp.name) / 'target/registration').glob('*.json')).read_text())
    def test_success_runs_ui_only_and_retains_candidates(self):
        self.assertEqual(0, self.run_fixture()); self.assertEqual(1, len(self.commands))
        command = self.commands[0]
        self.assertEqual('test', command[-1])
        self.assertIn('-Dtest=RegistrationUiTest#customerRegistrationFlow', command)
        self.assertIn('-DexcludedGroups=slow-auth,auth-rate-limit', command)
        report = self.manifest()
        self.assertRegex(report['phoneCandidates']['customer'], r'^8999[0-9]{6}$')
        self.assertFalse(report['cleanupPerformed']); self.assertFalse(report['externalDatabaseOrRedisAccess'])
        self.assertEqual('retain', report['dataPolicy']); self.assertNotIn('accountIds', report)
    def test_failure_preserves_exit_code_and_manifest(self):
        self.code = 7
        self.assertEqual(7, self.run_fixture()); self.assertEqual(7, self.manifest()['exitCode'])
        self.assertFalse(self.manifest()['cleanupPerformed'])
    def test_session_choices_run_the_owned_ui_fixture_method(self):
        for case, method in runner.SESSION.items():
            with self.subTest(case=case):
                self.assertEqual(0, self.run_fixture(['--session-case', case]))
                command = self.commands[-1]
                self.assertIn('-Dtest=SessionManagementTest#' + method, command)
                self.assertIn('-Dsession.fixture.case=' + case, command)
                self.assertTrue(any(value.startswith('-Dcustomer.phone=8999') for value in command))
    def test_all_registrations_have_explicit_test_goal(self):
        self.assertEqual(0, self.run_fixture([]))
        self.assertIn('-Dtest=RegistrationUiTest#customerRegistrationFlow+riderRegistrationFlow+restaurantRegistrationFlow', self.commands[-1])
        self.assertEqual('test', self.commands[-1][-1])
    def test_non_dev_or_insecure_urls_fail_before_tests(self):
        for url in ['http://dev.trycloudflare.com', 'https://production.example', 'https://trycloudflare.com',
                    'https://dev.trycloudflare.com.evil.example', 'https://user:password@dev.trycloudflare.com']:
            with self.subTest(url=url), self.assertRaises(ValueError): self.run_fixture(url=url)
        self.assertEqual([], self.commands)
    def test_local_collision_is_retried_without_server_lookup(self):
        used = {'8999000001'}
        with patch.object(runner.secrets, 'randbelow', side_effect=[1, 2]):
            self.assertEqual('8999000002', runner.candidate('8999', used))
        self.assertEqual([], self.commands)
    def test_launch_failure_still_retains_manifest(self):
        with patch.object(self, 'execute', side_effect=FileNotFoundError()), self.assertRaises(FileNotFoundError): self.run_fixture()
        self.assertEqual('FileNotFoundError', self.manifest()['runnerError'])
        self.assertFalse(self.manifest()['cleanupPerformed'])

if __name__ == '__main__': unittest.main()
