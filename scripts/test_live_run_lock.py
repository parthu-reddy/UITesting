import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import live_run_lock
from unittest.mock import patch
import contextlib, io
import run_registration_e2e, run_business_platform_o45_e2e, run_partner_applications_o3_e2e, run_approved_brand_search_ui
import json


class LiveRunLockTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(); self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name); self.handoff = self.root / 'e2e-plan' / '_handoff'
        self.handoff.mkdir(parents=True)

    def test_takes_and_releases_the_shared_lock(self):
        with live_run_lock.held(self.root):
            self.assertTrue((self.handoff / 'RUN-IN-PROGRESS').read_text().startswith(f'{os.getpid()} '))
        self.assertFalse((self.handoff / 'RUN-IN-PROGRESS').exists())

    def test_refuses_while_another_live_run_holds_it(self):
        other = subprocess.Popen([sys.executable, '-c', 'import time; time.sleep(30)'])
        self.addCleanup(other.kill)
        (self.handoff / 'RUN-IN-PROGRESS').write_text(f'{other.pid} 2026-10-08T07:00:00\n')
        with self.assertRaises(SystemExit) as refused:
            with live_run_lock.held(self.root):
                self.fail('must not run')
        self.assertIn(str(other.pid), str(refused.exception))
        self.assertTrue((self.handoff / 'RUN-IN-PROGRESS').read_text().startswith(f'{other.pid} '), 'never steals a live lock')

    def test_replaces_a_stale_lock_from_a_dead_run(self):
        dead = subprocess.Popen([sys.executable, '-c', 'pass']); dead.wait()
        (self.handoff / 'RUN-IN-PROGRESS').write_text(f'{dead.pid} 2026-10-08T07:00:00\n')
        with live_run_lock.held(self.root):
            pass
        self.assertFalse((self.handoff / 'RUN-IN-PROGRESS').exists())

    def test_refuses_during_an_owner_deploy(self):
        (self.handoff / 'DEPLOY-IN-PROGRESS').touch()
        with self.assertRaises(SystemExit):
            with live_run_lock.held(self.root):
                self.fail('must not run')
        self.assertFalse((self.handoff / 'RUN-IN-PROGRESS').exists())

    def test_releases_on_failure(self):
        with self.assertRaises(RuntimeError):
            with live_run_lock.held(self.root):
                raise RuntimeError('mvn crashed')
        self.assertFalse((self.handoff / 'RUN-IN-PROGRESS').exists())


    def test_every_fresh_person_runner_refuses_before_mvn_while_locked(self):
        other = subprocess.Popen([sys.executable, '-c', 'import time; time.sleep(30)'])
        self.addCleanup(other.kill)
        (self.handoff / 'RUN-IN-PROGRESS').write_text(f'{other.pid} 2026-10-08T07:00:00\n')
        for runner in (run_registration_e2e, run_business_platform_o45_e2e, run_partner_applications_o3_e2e):
            launched = []
            with self.subTest(runner=runner.__name__), patch.object(runner, 'ROOT', self.root), \
                    patch.object(runner.subprocess, 'run', side_effect=lambda c, **k: launched.append(c)), \
                    patch('sys.argv', ['runner', '--app-url', 'https://dev.trycloudflare.com']), contextlib.redirect_stdout(io.StringIO()):
                with self.assertRaises(SystemExit):
                    runner.main()
                self.assertEqual([], launched, 'mvn must not start while another live run holds the lock')
        fixture = self.root / 'o3-fixture.json'
        fixture.write_text(json.dumps({'phoneCandidates': {'restaurant.lifecycle': '9999123456'}}))
        launched = []
        with patch.object(run_approved_brand_search_ui, 'ROOT', self.root), \
                patch.object(run_approved_brand_search_ui.subprocess, 'run', side_effect=lambda c, **k: launched.append(c)), \
                patch('sys.argv', ['runner', '--app-url', 'https://dev.trycloudflare.com', '--fixture-manifest', str(fixture)]), \
                contextlib.redirect_stdout(io.StringIO()):
            with self.assertRaises(SystemExit):
                run_approved_brand_search_ui.main()
        self.assertEqual([], launched)


if __name__ == '__main__':
    unittest.main()
