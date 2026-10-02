import contextlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import run_registration_e2e as runner

class RegistrationRunnerRetentionTest(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.commands=[];self.sql=[];self.phone=None;self.profile='dev';self.maven_code=0
        self.user='601feefa-f89f-4b44-b43c-92ade231ea22'

    def execute(self, command, **kwargs):
        self.commands.append(command)
        statement=kwargs.get('input') or ''
        if statement:self.sql.append(statement)
        stdout='';code=0
        if command[0]=='mvn':
            self.phone=next(c.split('=',1)[1] for c in command if c.startswith('-Dregistration.customer.phone='))
            code=self.maven_code
        elif 'printenv SPRING_PROFILES_ACTIVE' in command[-1]:stdout=self.profile
        elif 'SELECT clock_timestamp()' in statement:stdout='2026-10-01 00:00:00+00'
        elif 'SELECT EXISTS' in statement:stdout='f'
        elif 'json_agg' in statement:stdout=json.dumps([dict(id=self.user,phone=self.phone)])
        return subprocess.CompletedProcess(command,code,stdout,'')

    def run_fixture(self, selection=None):
        with patch.object(runner,'ROOT',Path(self.temp.name)), patch.object(runner.subprocess,'run',side_effect=self.execute), patch('sys.argv',['runner','--app-url','https://dev.example/']+(selection or ['--only','customer'])), contextlib.redirect_stdout(io.StringIO()):
            return runner.main()

    def assert_no_cleanup(self):
        for statement in self.sql:
            self.assertIn('BEGIN READ ONLY;',statement)
            for write in ['UPDATE ','DELETE ','INSERT ','TRUNCATE ','DROP ']:self.assertNotIn(write,statement.upper())
        self.assertFalse(any('redis-cli' in c[-1] for c in self.commands))
        self.assertFalse(any('-d delivery_db' in c[-1] or '-d restaurant_db' in c[-1] for c in self.commands))

    def manifest(self):
        return json.loads(next((Path(self.temp.name)/'target/registration').glob('*.json')).read_text())

    def test_success_retains_accounts_and_sessions_without_remote_cleanup(self):
        self.assertEqual(0,self.run_fixture());self.assert_no_cleanup()
        self.assertRegex(self.phone,r'^8999[0-9]{6}$')
        report=self.manifest()
        self.assertFalse(report['retired']);self.assertFalse(report['cleanupPerformed'])
        self.assertEqual('retain',report['dataPolicy']);self.assertEqual([self.user],report['accountIds'])

    def test_failure_keeps_created_data_and_preserves_test_exit_code(self):
        self.maven_code=7
        self.assertEqual(7,self.run_fixture());self.assert_no_cleanup()
        self.assertEqual(7,self.manifest()['exitCode']);self.assertEqual([self.user],self.manifest()['accountIds'])
        self.assertFalse(self.manifest()['cleanupPerformed'])

    def test_invokes_selected_flow_and_excludes_deferred_tests(self):
        self.assertEqual(0,self.run_fixture())
        command=next(c for c in self.commands if c[0]=='mvn')
        self.assertEqual('test',command[-1])
        self.assertIn('-Dtest=RegistrationUiTest#customerRegistrationFlow',command)
        self.assertIn('-DexcludedGroups=slow-auth,auth-rate-limit',command)

    def test_session_cases_allocate_a_fresh_customer_without_cleanup(self):
        cases={'device-removal':'removeOwnSecondDeviceRevokesOnlyThatSession','limit-cancel':'sessionLimitCancelPreservesExistingDevices','limit-replacement':'sessionLimitReplacementRevokesSelectedDeviceOnly'}
        for case,method in cases.items():
            with self.subTest(case=case):
                self.commands=[];self.sql=[]
                self.assertEqual(0,self.run_fixture(['--session-case',case]));self.assert_no_cleanup()
                command=next(c for c in self.commands if c[0]=='mvn')
                self.assertIn('-Dtest=SessionManagementTest#'+method,command)
                self.assertIn('-Dsession.fixture.case='+case,command)
                self.assertIn('-Dcustomer.phone='+self.phone,command)
                self.assertIn('-DexcludedGroups=slow-auth,auth-rate-limit',command)

    def test_refuses_production_before_allocating_or_running_tests(self):
        self.profile='dev,prod'
        with self.assertRaisesRegex(RuntimeError,'exclusively Dev'):self.run_fixture()
        self.assertFalse(self.sql);self.assertFalse(any(c[0]=='mvn' for c in self.commands))

    def test_failed_read_only_audit_does_not_trigger_cleanup_or_mask_success(self):
        execute=self.execute
        def audit_unavailable(command,**kwargs):
            if 'json_agg' in (kwargs.get('input') or ''):
                self.commands.append(command);self.sql.append(kwargs['input'])
                return subprocess.CompletedProcess(command,1,'','Read-only audit unavailable')
            return execute(command,**kwargs)
        with patch.object(self,'execute',side_effect=audit_unavailable):self.assertEqual(0,self.run_fixture())
        self.assert_no_cleanup();self.assertIn('accountAuditError',self.manifest())
        self.assertFalse(self.manifest()['cleanupPerformed'])

if __name__=='__main__':unittest.main()
