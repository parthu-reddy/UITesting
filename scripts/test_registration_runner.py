import contextlib
import io
import json
from pathlib import Path
import re
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import run_registration_e2e as runner

class RegistrationRunnerIsolationTest(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.commands=[];self.sql=[];self.phone=None;self.profile='dev';self.new=True;self.busy=False
        self.user='601feefa-f89f-4b44-b43c-92ade231ea22'

    def execute(self, command, **kwargs):
        self.commands.append(command)
        statement=kwargs.get('input') or ''
        if statement:self.sql.append(statement)
        stdout='';code=0;error=''
        if command[0]=='mvn':
            self.phone=next(c.split('=',1)[1] for c in command if c.startswith('-Dregistration.customer.phone='))
        elif 'printenv SPRING_PROFILES_ACTIVE' in command[-1]:stdout=self.profile
        elif 'SELECT clock_timestamp()' in statement:stdout='2026-10-01 00:00:00+00'
        elif 'SELECT EXISTS' in statement:stdout='f'
        elif 'json_agg' in statement:stdout=json.dumps([dict(id=self.user,phone=self.phone)])
        elif 'SELECT created_at >=' in statement:stdout='t' if self.new else 'f'
        elif 'delivery_executives' in statement and self.busy:code=1;error='Cleanup refused: disposable rider is not offline'
        elif 'redis-cli --raw GET' in command[-1]:stdout=json.dumps([dict(sessionId='run-session-1')])
        return subprocess.CompletedProcess(command,code,stdout,error)

    def run_fixture(self):
        with patch.object(runner,'ROOT',Path(self.temp.name)), patch.object(runner.subprocess,'run',side_effect=self.execute), patch('sys.argv',['runner','--app-url','https://dev.example/','--only','customer']), contextlib.redirect_stdout(io.StringIO()):
            return runner.main()

    def test_retires_only_allocated_accounts_and_revokes_remaining_sessions(self):
        self.assertEqual(0,self.run_fixture())
        writes=[s for s in self.sql if 'UPDATE ' in s]
        self.assertEqual(3,len(writes))
        for statement in writes:
            self.assertIn(self.user,statement)
            self.assertNotIn('DELETE ',statement)
        self.assertRegex(self.phone,r'^8999[0-9]{6}$')
        self.assertTrue(any('BLACKLIST:SESSION:run-session-1' in c[-1] for c in self.commands))
        report=json.loads(next((Path(self.temp.name)/'target/registration').glob('*.json')).read_text())
        self.assertTrue(report['retired'])
        self.assertEqual([self.user],report['accountIds'])

    def test_invokes_maven_test_goal_for_selected_registration_flow(self):
        self.assertEqual(0,self.run_fixture())
        command=next(c for c in self.commands if c[0]=='mvn')
        self.assertEqual('test',command[-1])
        self.assertIn('-Dtest=RegistrationUiTest#customerRegistrationFlow',command)

    def test_refuses_to_retire_an_account_older_than_the_run(self):
        self.new=False
        with self.assertRaisesRegex(RuntimeError,'predates'):self.run_fixture()
        self.assertFalse(any('UPDATE ' in s for s in self.sql))

    def test_refuses_production_before_allocating_or_running_tests(self):
        self.profile='dev,prod'
        with self.assertRaisesRegex(RuntimeError,'exclusively Dev'):self.run_fixture()
        self.assertFalse(self.sql)
        self.assertFalse(any(c[0]=='mvn' for c in self.commands))

    def test_leaves_active_delivery_state_untouched(self):
        self.busy=True
        with self.assertRaisesRegex(RuntimeError,'not offline'):self.run_fixture()
        self.assertFalse(any('UPDATE users' in s for s in self.sql))
        self.assertFalse(any('redis-cli' in c[-1] for c in self.commands))

if __name__=='__main__':unittest.main()
