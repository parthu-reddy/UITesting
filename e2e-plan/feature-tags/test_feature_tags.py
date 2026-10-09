#!/usr/bin/env python3
"""Break tests for the feature map, the tag gate and the selector: each guard must be seen red.

    python3 test_feature_tags.py
"""
import json
import pathlib
import shutil
import tempfile
import unittest

import feature_map
import select_tests
import tag_scan
import validate_feature_tags as gate

HERE = pathlib.Path(__file__).resolve().parent
CHAT_TEST = "features/exceptions/ChatHistoryPagingTest.java"


def mutated_config(edit):
    cfg = json.loads((HERE / "features.json").read_text())
    edit(cfg)
    path = pathlib.Path(tempfile.mkdtemp()) / "features.json"
    path.write_text(json.dumps(cfg))
    return path


def mutated_suite(rel, old, new):
    root = pathlib.Path(tempfile.mkdtemp()) / "tests"
    shutil.copytree(tag_scan.SUITE, root)
    f = root / rel
    src = f.read_text()
    assert old in src, (rel, old)
    f.write_text(src.replace(old, new, 1))
    return root


def failing(results):
    return [name.split()[0] for name, ok, _ in results if not ok]


class FeatureMapCoverage(unittest.TestCase):
    def test_real_map_passes(self):
        scanned, problems = feature_map.coverage(feature_map.load())
        self.assertEqual(problems, [])
        self.assertGreater(scanned, 1000)

    def test_unmapped_strict_file_fails(self):
        def drop_override(cfg):
            rules = cfg["repos"]["RestaurantApplication"]["rules"]
            for r in rules:
                r[0] = r[0].replace("|Override|", "|")
        _, problems = feature_map.coverage(feature_map.load(mutated_config(drop_override)))
        self.assertTrue(any(p.startswith("UNMAPPED RestaurantApplication/") and "OverrideItemDto" in p for p in problems), problems)

    def test_dead_rule_fails(self):
        cfg = feature_map.load(mutated_config(
            lambda c: c["repos"]["ReviewsService"]["rules"].append(["NoSuchFileAnywhere", ["reviews"]])))
        self.assertIn("DEAD RULE ReviewsService: NoSuchFileAnywhere", feature_map.coverage(cfg)[1])

    def test_unknown_feature_fails(self):
        cfg = feature_map.load(mutated_config(
            lambda c: c["repos"]["ReviewsService"].__setitem__("fallback", ["reviewz"])))
        self.assertTrue(any("unknown feature 'reviewz'" in p for p in feature_map.coverage(cfg)[1]))

    def test_resolution(self):
        cfg = feature_map.load()
        self.assertEqual(feature_map.resolve(cfg, "FoodDeliveryAppUI/src/features/communication/models/useChatSession.ts")[1], {"chat"})
        self.assertIsNone(feature_map.resolve(cfg, "FoodDeliveryAppUI/src/features/communication/models/useChatSession.test.tsx")[1])
        self.assertEqual(feature_map.resolve(cfg, "CommunicationIntegration/src/main/java/a/B.java")[1], {"no-e2e"})
        everything = feature_map.resolve(cfg, "CommonLibrary/common-core/src/main/java/com/fooddelivery/common/event/OutboxEvent.java")[1]
        self.assertEqual(everything, set(cfg["features"]))


class TagGate(unittest.TestCase):
    def test_real_suite_passes(self):
        self.assertEqual(failing(gate.checks()), [])

    def test_untagged_method_fails(self):
        suite = mutated_suite(CHAT_TEST, '@Tag("feature-chat")\n', "")
        self.assertIn("F2", failing(gate.checks(suite)))

    def test_unknown_feature_tag_fails(self):
        suite = mutated_suite(CHAT_TEST, '@Tag("feature-chat")', '@Tag("feature-chats")')
        self.assertEqual({"F2", "F3", "F4"} & set(failing(gate.checks(suite))), {"F2", "F3", "F4"})

    def test_one_off_tag_fails(self):
        suite = mutated_suite(CHAT_TEST, '@Tag("feature-chat")', '@Tag("feature-chat") @Tag("cart-ui")')
        self.assertEqual(failing(gate.checks(suite)), ["F4"])

    def test_empty_suite_is_not_a_pass(self):
        suite = pathlib.Path(tempfile.mkdtemp())
        self.assertIn("F6", failing(gate.checks(suite)))


def row(cls, method, *tags):
    return dict(cls=cls, method=method, tags=sorted(tags), file=str(tag_scan.SUITE / f"{cls}.java"), disabled=False)


ROWS = [
    row("ChatA", "a", "feature-chat"),
    row("ChatB", "b", "feature-chat", "slow"),
    row("Sse", "s", "feature-chat", "parked"),
    row("Reviews", "r", "feature-reviews"),
    row("Reviews", "r2", "feature-reviews", "feature-chat"),
]


class Selector(unittest.TestCase):
    def test_feature_selects_tagged_methods_without_slow_or_parked(self):
        res = select_tests.select(features=["chat"], rows=ROWS)
        self.assertEqual(res["chosen"], {"ChatA": ["a"], "Reviews": ["r2"]})
        self.assertEqual(res["skipped"], ["ChatB#b", "Sse#s"])
        self.assertEqual(select_tests.selection_lines(res["chosen"], ROWS), ["ChatA", "Reviews#r2"])

    def test_include_slow_never_includes_parked(self):
        res = select_tests.select(features=["chat"], include_slow=True, rows=ROWS)
        self.assertIn("ChatB", res["chosen"])
        self.assertNotIn("Sse", res["chosen"])

    def test_source_path_maps_to_feature(self):
        res = select_tests.select(["ReviewsService/src/main/java/com/fooddelivery/reviews/service/X.java"], rows=ROWS)
        self.assertEqual(res["chosen"], {"Reviews": ["r", "r2"]})

    def test_no_e2e_and_out_of_scope_are_reported_not_selected(self):
        res = select_tests.select(["CommunicationIntegration/src/main/java/a/B.java", "CustomerApplication/README.md"], rows=ROWS)
        self.assertEqual(res["chosen"], {})
        self.assertEqual(len(res["no_e2e"]), 1)
        self.assertEqual(res["ignored"], ["CustomerApplication/README.md"])

    def test_changed_test_class_selects_itself(self):
        real = [r for r in tag_scan.scan() if not r["disabled"]]
        res = select_tests.select([f"UITesting/src/test/java/com/fooddelivery/e2e/tests/{CHAT_TEST}"], rows=real)
        self.assertEqual(set(res["chosen"]), {"ChatHistoryPagingTest"})

    def test_changed_page_object_selects_its_users(self):
        real = [r for r in tag_scan.scan() if not r["disabled"]]
        res = select_tests.select(["UITesting/src/test/java/com/fooddelivery/e2e/pages/customer/CustomerOrderHistoryPage.java"], rows=real)
        self.assertIn("RetainedOrderStateUiTest", res["chosen"])
        # ChatHistoryPagingTest pages to its order through this page object since 2026-10-08, so it is a user now.
        self.assertIn("ChatHistoryPagingTest", res["chosen"])
        self.assertNotIn("LoginValidationTest", res["chosen"])

    def test_unknown_feature_refused(self):
        with self.assertRaises(SystemExit):
            select_tests.select(features=["nope"], rows=ROWS)


if __name__ == "__main__":
    unittest.main(verbosity=1)
