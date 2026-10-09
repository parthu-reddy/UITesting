#!/usr/bin/env python3
"""Gate for E2E feature tags (owner request 2026-10-08). Exit 0 only when every check passes.

  F1  feature map coverage: every in-scope source file resolves to a feature, no dead rule, no unknown feature
  F2  every enabled E2E test method carries at least one vocabulary feature-<name> tag (class or method level)
  F3  every feature-* tag is in the vocabulary (features.json)
  F4  no tag outside vocabulary + behaviour_tags: the superseded one-off tags (cart-ui, bp-o4, admin-liveops, ...) are gone
  F5  every vocabulary feature except the declared uncovered ones is carried by at least one test
  F6  the scan is live: it found more than 300 test methods (a broken scanner must not pass vacuously)

    python3 validate_feature_tags.py
"""
import sys

import feature_map
import tag_scan

PREFIX = "feature-"


def checks(suite=None, cfg_path=None):
    cfg = feature_map.load(cfg_path) if cfg_path else feature_map.load()
    vocab = feature_map.vocabulary(cfg)
    allowed = {PREFIX + f for f in vocab} | set(cfg["behaviour_tags"])
    rows = [r for r in (tag_scan.scan(suite) if suite else tag_scan.scan()) if not r["disabled"]]
    results = []

    scanned, problems = feature_map.coverage(cfg)
    results.append(("F1 feature map covers every in-scope file", not problems and scanned > 1000,
                    f"{scanned} files; problems {problems[:5]}{' ...' if len(problems) > 5 else ''}"))

    untagged = [f"{r['cls']}#{r['method']}" for r in rows
                if not any(t.startswith(PREFIX) and t in allowed for t in r["tags"])]
    results.append(("F2 every enabled test has a vocabulary feature tag", not untagged, f"untagged {len(untagged)}: {untagged[:8]}"))

    unknown = sorted({t for r in rows for t in r["tags"] if t.startswith(PREFIX) and t not in allowed})
    results.append(("F3 feature tags are in the vocabulary", not unknown, f"unknown {unknown}"))

    stray = sorted({t for r in rows for t in r["tags"] if t not in allowed})
    results.append(("F4 only vocabulary + behaviour tags remain", not stray, f"stray {stray}"))

    carried = {t[len(PREFIX):] for r in rows for t in r["tags"] if t.startswith(PREFIX)}
    uncovered = sorted(set(vocab) - carried)
    results.append(("F5 every feature has at least one test", not uncovered, f"no test carries {uncovered}"))

    results.append(("F6 scanner is live", len(rows) > 300, f"{len(rows)} enabled test methods"))
    return results


def main():
    results = checks()
    for name, ok, detail in results:
        print(f"[{'PASS' if ok else 'FAIL'}] {name}: {detail}")
    passed = sum(ok for _, ok, _ in results)
    print(f"FEATURE TAGS: {passed}/{len(results)}")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
