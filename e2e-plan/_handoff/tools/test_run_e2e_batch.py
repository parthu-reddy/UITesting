#!/usr/bin/env python3
"""Proves run_e2e_batch.py's guards fire: deploy lock, canary stop, fail-fast on a repeated
signature, and admin pacing. Live runs are faked; nothing touches Dev.   python3 test_run_e2e_batch.py"""
import json
import os
import sys
import tempfile
import unittest
from unittest import mock

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import run_e2e_batch as rb  # noqa: E402

REAL_TAKE_RUN_LOCK = rb.take_run_lock
rb.take_run_lock = lambda: None  # main() tests must never write the real handoff lock; RunLock tests the real one
# A real deploy lock exists during every owner deploy; the guards must stay testable then. The deploy-lock test
# fakes this path's existence itself.
rb.LOCK = os.path.join(tempfile.mkdtemp(), "DEPLOY-IN-PROGRESS")


class Fresh(unittest.TestCase):
    """Each test gets its own step-up history file; none may read or write the real one."""
    def setUp(self):
        rb.STEPUP_HISTORY = os.path.join(tempfile.mkdtemp(), "ADMIN-STEP-UPS.json")
        rb.stepups.clear()


def case(cls, outcome="pass", sig=None, limited=False):
    return dict(cls=cls, name="m", outcome=outcome, signature=sig, rate_limited=limited, report="/dev/null")


class Guards(Fresh):
    def setUp(self):
        super().setUp()
        self.ev = tempfile.mkdtemp()
        patches = [mock.patch.object(rb, "keep"), mock.patch.object(rb, "time"),
                   mock.patch.object(rb.subprocess, "run", return_value=mock.Mock(returncode=0, stdout="", stderr=""))]
        for p in patches:
            p.start()
            self.addCleanup(p.stop)
        rb.time.time.return_value = 1_000_000.0
        rb.time.strftime.return_value = "00:00:00"

    def main(self, *args):
        with mock.patch.object(sys, "argv", ["x", "--evidence", self.ev, *args]):
            rb.main()

    def test_deploy_lock_refuses(self):
        with mock.patch.object(rb.os.path, "exists", side_effect=lambda p: p == rb.LOCK or os.path.exists(p)):
            with self.assertRaises(SystemExit) as e:
                self.main("--classes", "RestaurantUiTest")
        self.assertIn("REFUSED", str(e.exception))

    def test_canary_failure_stops_before_work(self):
        calls = []

        def fake(sel, dry):
            calls.append(sel)
            return 0, [case("c", "failure", "LoginPage.java:71 Timeout")] if "PageReload" in sel else [case("c")]
        with mock.patch.object(rb, "run", side_effect=fake):
            with self.assertRaises(SystemExit) as e:
                self.main("--classes", "RestaurantUiTest,ProfileSettingsTest")
        self.assertIn("customer canary", str(e.exception))
        self.assertEqual(calls, [rb.CANARIES["customer"]], "nothing may run after a failed canary")

    def test_repeated_signature_stops_at_second_class(self):
        calls = []

        def fake(sel, dry):
            calls.append(sel)
            return 0, [case(sel, "failure", "SavedDeliveryAddressPage.java:15 Locator expected to be visible")]
        with mock.patch.object(rb, "run", side_effect=fake):
            with self.assertRaises(SystemExit) as e:
                self.main("--skip-canary", "--classes", "RestaurantUiTest,ProfileSettingsTest,CheckoutUiTest")
        self.assertIn("fail-fast", str(e.exception))
        self.assertEqual(calls, ["RestaurantUiTest", "ProfileSettingsTest"], "the third class must not run")

    def test_distinct_failures_do_not_stop(self):
        sigs = iter(["A.java:1 x", "B.java:2 y", "C.java:3 z"])
        with mock.patch.object(rb, "run", side_effect=lambda sel, dry: (0, [case(sel, "failure", next(sigs))])):
            self.main("--skip-canary", "--classes", "RestaurantUiTest,ProfileSettingsTest,CheckoutUiTest")

    def test_admin_pacing_waits_after_four(self):
        sleeps = []
        rb.time.sleep.side_effect = lambda s: sleeps.append(s)
        for _ in range(4):
            rb.wait_for_admin_budget(False)
        self.assertEqual(sleeps, [])
        rb.time.sleep.side_effect = lambda s: (sleeps.append(s), rb.stepups.clear())
        rb.wait_for_admin_budget(False)
        self.assertTrue(sleeps and sleeps[0] >= 300, f"5th step-up inside 5 min must wait a window, waited {sleeps}")


class RunLock(Fresh):
    def test_live_lock_refuses_and_stale_lock_is_taken(self):
        tmp = tempfile.mkdtemp()
        with mock.patch.object(rb, "RUN_LOCK", os.path.join(tmp, "RUN-IN-PROGRESS")):
            open(rb.RUN_LOCK, "w").write(f"{os.getpid()} now\n")  # this very process: alive
            with self.assertRaises(SystemExit) as e:
                REAL_TAKE_RUN_LOCK()
            self.assertIn("REFUSED", str(e.exception))
            open(rb.RUN_LOCK, "w").write("999999999 old\n")  # no such process: stale
            REAL_TAKE_RUN_LOCK()
            self.assertTrue(open(rb.RUN_LOCK).read().startswith(f"{os.getpid()} "))


class Interleave(Fresh):
    def test_non_admin_runs_while_admin_budget_is_spent(self):
        rb.stepups.clear()
        order = []
        clock = {"t": 1_000_000.0}
        with mock.patch.object(rb, "keep"), mock.patch.object(rb, "time") as t, \
                mock.patch.object(rb.subprocess, "run", return_value=mock.Mock(returncode=0, stdout="", stderr="")), \
                mock.patch.object(rb, "run", side_effect=lambda sel, dry: (order.append(sel), (0, [case(sel)]))[1]), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--skip-canary",
                                                "--classes", "AdminUiTest,ProfileSettingsTest"]):
            t.time.side_effect = lambda: clock["t"]
            t.sleep.side_effect = lambda s: clock.__setitem__("t", clock["t"] + s)
            t.strftime.return_value = "00:00:00"
            rb.stepups[:] = [clock["t"] - 10] * 4  # four step-ups in the last minute: budget spent
            rb.main()
        self.assertTrue(order[0].startswith("ProfileSettingsTest"), f"non-admin work must fill the admin wait: {order}")
        self.assertTrue(any(o.startswith("AdminUiTest#") for o in order[1:]), order)


class RetryKeepsHistory(Fresh):
    def test_rate_limited_retry_counts_the_refused_attempt_and_keeps_history(self):
        rb.stepups.clear()
        clock = {"t": 2_000_000.0}
        calls = []

        def fake(sel, dry):
            calls.append(sel)
            return 0, [case(sel, "failure", "LoginPage.java:89 ADMIN", limited=len(calls) == 1)]
        with mock.patch.object(rb, "keep"), mock.patch.object(rb, "time") as t, mock.patch.object(rb, "run", side_effect=fake), \
                mock.patch.object(rb.subprocess, "run", return_value=mock.Mock(returncode=0, stdout="", stderr="")), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--skip-canary",
                                                "--classes", "AdminUiTest#" + rb.test_methods(rb.class_source("AdminUiTest"))[0]]):
            t.time.side_effect = lambda: clock["t"]
            t.sleep.side_effect = lambda s: clock.__setitem__("t", clock["t"] + s)
            t.strftime.return_value = "00:00:00"
            rb.main()
        self.assertEqual(len(calls), 2, "one retry")
        self.assertEqual(len(rb.stepups), 3, "first attempt + refused send + retry are all remembered")


class Selection(Fresh):
    def setUp(self):
        super().setUp()
        self.ev = tempfile.mkdtemp()

    def test_method_subset_and_props(self):
        seen = []
        with mock.patch.object(rb, "run", side_effect=lambda sel, dry: (seen.append((sel, list(rb.EXTRA))), (0, [case(sel)]))[1]), \
                mock.patch.object(rb, "keep"), \
                mock.patch.object(rb.subprocess, "run", return_value=mock.Mock(returncode=0, stdout="", stderr="")), \
                mock.patch.object(sys, "argv", ["x", "--evidence", self.ev, "--skip-canary", "--prop", "a.b=c",
                                                "--classes", "PartnerReadOnlyUiTest#riderHistoryDateCanBeCleared"]):
            rb.main()
        self.assertEqual(seen, [("PartnerReadOnlyUiTest#riderHistoryDateCanBeCleared", ["-Da.b=c"])])

    def test_unknown_method_refused(self):
        with mock.patch.object(sys, "argv", ["x", "--evidence", self.ev, "--dry-run", "--classes", "PartnerReadOnlyUiTest#nope"]):
            with self.assertRaises(SystemExit) as e:
                rb.main()
        self.assertIn("no test methods", str(e.exception))


class ExcludeTags(Fresh):
    def test_exclude_tags_extend_the_default_excluded_groups(self):
        cmds = []
        with mock.patch.object(rb, "keep"), mock.patch.object(rb.shutil, "rmtree"), \
                mock.patch.object(rb.subprocess, "run", side_effect=lambda cmd, **k: (cmds.append(cmd), mock.Mock(returncode=0, stdout="", stderr=""))[1]), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--skip-canary",
                                                "--exclude-tags", "slow,auto-cancel", "--classes", "CustomerCartTest"]):
            rb.main()
        test_cmd = next(c for c in cmds if c[-1] == "test")
        groups = [a for a in test_cmd if a.startswith("-DexcludedGroups=")]
        self.assertEqual(groups, ["-DexcludedGroups=auth-rate-limit,parked,measurement,slow,auto-cancel"], test_cmd)
        for kept in ("-Dsurefire.failIfNoSpecifiedTests=false", "-Dsurefire.rerunFailingTestsCount=0"):
            self.assertIn(kept, test_cmd, "the other fixed Maven flags must survive")


class BrowserSettings(Fresh):
    """TestConfig defaults are fast (no slow-mo, headless, no video); only --debug overrides them (2026-10-08)."""
    KEYS = ("-Dslow.mo", "-Dheadless", "-Drecord.video")

    def maven_commands(self, *args):
        cmds = []
        with mock.patch.object(rb, "keep"), mock.patch.object(rb.shutil, "rmtree"), \
                mock.patch.object(rb.subprocess, "run", side_effect=lambda cmd, **k: (cmds.append(cmd), mock.Mock(returncode=0, stdout="", stderr=""))[1]), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--skip-canary", "--classes", "CustomerCartTest", *args]):
            rb.main()
        return [c for c in cmds if c[-1] == "test"]  # canaries use the same run(), so they get the same flags

    def test_default_run_uses_fast_settings(self):
        cmds = self.maven_commands()
        self.assertEqual(len(cmds), 1)
        self.assertEqual([a for c in cmds for a in c if a.startswith(self.KEYS)], [], "TestConfig's fast defaults must apply")

    def test_debug_restores_watchable_settings(self):
        cmds = self.maven_commands("--debug")
        self.assertEqual(len(cmds), 1)
        for c in cmds:
            self.assertEqual([a for a in c if a.startswith(self.KEYS)],
                             ["-Dslow.mo=400", "-Dheadless=false", "-Drecord.video=true"], c)


class FeatureSelection(Fresh):
    """--features/--changed run only the tests carrying the touched features' tags (owner request 2026-10-08)."""

    def planned(self, *args):
        seen = []
        with mock.patch.object(rb, "run", side_effect=lambda sel, dry: (seen.append(sel), (0, []))[1]), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--dry-run", "--skip-canary", *args]):
            rb.main()
        return seen

    def test_features_select_only_tagged_classes(self):
        seen = self.planned("--features", "chat")
        classes = {s.split("#")[0] for s in seen}
        self.assertEqual(classes, {"AdminSupportChatIsolationRoutedUiTest", "ChatHistoryPagingTest",
                                   "ChatSupportWindowClosedTest", "ChatWindowRoutedUiTest"}, seen)

    def test_slow_needs_include_slow(self):
        self.assertNotIn("HappyDeliveryFlowTest", {s.split("#")[0] for s in self.planned("--features", "chat")})
        with_slow = self.planned("--features", "chat", "--include-slow")
        self.assertIn("HappyDeliveryFlowTest#completeOrderLifecycle", with_slow)

    def test_changed_maps_git_paths(self):
        sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(rb.__file__)), "..", "..", "feature-tags"))
        import select_tests
        with mock.patch.object(select_tests, "git_changes",
                               return_value=["ReviewsService/src/main/java/com/fooddelivery/reviews/service/X.java"]):
            seen = self.planned("--changed")
        self.assertIn("OrderReviewsFlowTest", {s.split("#")[0] for s in seen})
        self.assertNotIn("ChatHistoryPagingTest", {s.split("#")[0] for s in seen})

    def test_unknown_feature_refused(self):
        with self.assertRaises(SystemExit) as e:
            self.planned("--features", "nope")
        self.assertIn("unknown feature", str(e.exception))


class AdminDetection(unittest.TestCase):
    def test_parameterized_admin_account_is_paced(self):
        # SessionUiTest signs in Account.ADMIN with testAdminPhone; unpaced, it hit 429 (checkpoint134).
        self.assertTrue(rb.uses_admin(rb.class_source("SessionUiTest")))
        self.assertTrue(rb.uses_admin("login.login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, x)"))
        self.assertFalse(rb.uses_admin(rb.class_source("CustomerCartTest")))


class HistoryAcrossInvocations(Fresh):
    def invoke(self, clock, order, *classes):
        """One runner invocation in a fresh process: the in-memory history starts empty."""
        rb.stepups.clear()
        with mock.patch.object(rb, "keep"), mock.patch.object(rb, "time") as t, \
                mock.patch.object(rb.subprocess, "run", return_value=mock.Mock(returncode=0, stdout="", stderr="")), \
                mock.patch.object(rb, "run", side_effect=lambda sel, dry: (order.append((sel, clock["t"])), (0, [case(sel)]))[1]), \
                mock.patch.object(sys, "argv", ["x", "--evidence", tempfile.mkdtemp(), "--skip-canary",
                                                "--classes", ",".join(classes)]):
            t.time.side_effect = lambda: clock["t"]
            t.sleep.side_effect = lambda s: clock.__setitem__("t", clock["t"] + s)
            t.strftime.return_value = "00:00:00"
            rb.main()

    def test_second_invocation_inherits_the_spent_budget(self):
        clock, order = {"t": 3_000_000.0}, []
        first = rb.test_methods(rb.class_source("AdminUiTest"))[0]
        self.invoke(clock, order, *[f"AdminUiTest#{first}"] * 4)  # four admin step-ups: budget spent
        self.assertEqual(len(json.load(open(rb.STEPUP_HISTORY))), 4, "each step-up is written as it happens")
        started = clock["t"]
        self.invoke(clock, order, f"AdminUiTest#{first}")
        self.assertGreaterEqual(order[-1][1] - started, 240,
                                "the 5th step-up inside 5 minutes must wait, even in a new invocation")

    def test_expired_entries_are_dropped_and_dry_run_never_writes(self):
        json.dump([1_000.0, 2_999_990.0], open(rb.STEPUP_HISTORY, "w"))
        rb.load_stepups(3_000_000.0)
        self.assertEqual(rb.stepups, [2_999_990.0], "entries older than the longest window are forgotten")
        rb.SIM["now"] = 3_000_000.0
        rb.record_stepup(True)
        self.assertEqual(json.load(open(rb.STEPUP_HISTORY)), [1_000.0, 2_999_990.0], "a dry run must not spend budget")

    def test_unreadable_history_refuses(self):
        open(rb.STEPUP_HISTORY, "w").write("{half")
        with self.assertRaises(SystemExit) as e:
            rb.load_stepups(3_000_000.0)
        self.assertIn("REFUSED", str(e.exception))


if __name__ == "__main__":
    unittest.main(verbosity=2)
