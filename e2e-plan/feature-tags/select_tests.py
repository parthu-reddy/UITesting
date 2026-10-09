#!/usr/bin/env python3
"""Pick the E2E tests a change needs (owner request 2026-10-08: "only run tests for the features we made changes for").

    python3 select_tests.py --git                      uncommitted changes in every mapped repo + UITesting
    python3 select_tests.py --git --since origin/main  ... plus commits since REF in each repo that has REF
    python3 select_tests.py --files CustomerApplication/src/main/.../RefundService.java [...]
    python3 select_tests.py --features chat,reviews
    add --include-slow to keep slow/auto-cancel tests; --out FILE writes the runner selection
    then: python3 ../_handoff/tools/run_e2e_batch.py --selection FILE --evidence DIR

How a change becomes tests:
  - product source -> features via features.json (feature_map.resolve); tests carrying @Tag("feature-<f>") are picked
  - an edited E2E test class selects itself; an edited page object / util selects the tests that use it;
    TestBase or TestConfig selects everything
  - no-e2e paths (notifications, test infrastructure, parked ONDC) and out-of-scope paths (unit tests, docs) are
    reported by name, never silently dropped
"""
import argparse
import pathlib
import re
import subprocess
import sys

import feature_map
import tag_scan

UI_TESTING = pathlib.Path(__file__).resolve().parents[2]
HARNESS = UI_TESTING / "src/test/java/com/fooddelivery/e2e"
EXCLUDED_BY_DEFAULT = {"slow", "auto-cancel"}
NEVER = {"auth-rate-limit", "measurement", "parked"}  # parked: SSE on the tunnel; measurement: latency measurements


def git_changes(since=None):
    """Workspace-relative changed paths in every mapped repo and UITesting (status + optional diff since REF)."""
    cfg = feature_map.load()
    paths = set()
    for name in list(cfg["repos"]) + ["UITesting"]:
        root = feature_map.WORKSPACE / name
        if not (root / ".git").exists():
            continue
        st = subprocess.run(["git", "-C", str(root), "status", "--porcelain", "--untracked-files=all"],
                            capture_output=True, text=True).stdout
        for line in st.splitlines():
            p = line[3:].split(" -> ")[-1].strip('"')
            paths.add(f"{name}/{p}")
        if since and subprocess.run(["git", "-C", str(root), "rev-parse", "--verify", "-q", since],
                                    capture_output=True).returncode == 0:
            diff = subprocess.run(["git", "-C", str(root), "diff", "--name-only", since],
                                  capture_output=True, text=True).stdout
            paths |= {f"{name}/{p}" for p in diff.splitlines() if p}
    return sorted(paths)


def harness_selection(path, rows):
    """Test classes selected by a changed UITesting file, or None when the file is not E2E test code."""
    rel = pathlib.PurePosixPath(path)
    try:
        sub = pathlib.Path(feature_map.WORKSPACE / rel).resolve().relative_to(HARNESS)
    except ValueError:
        return None
    if sub.suffix != ".java":
        return None
    name = sub.stem
    if sub.parts[0] == "tests":
        return {name}
    if name in ("TestBase", "TestConfig"):
        return {r["cls"] for r in rows}
    users = set()
    for f in {r["file"] for r in rows}:
        if re.search(rf"\b{re.escape(name)}\b", pathlib.Path(f).read_text()):
            users.add(pathlib.Path(f).stem)
    return users


def select(paths=(), features=(), include_slow=False, rows=None):
    cfg = feature_map.load()
    rows = rows if rows is not None else [r for r in tag_scan.scan() if not r["disabled"]]
    why, classes, no_e2e, ignored = {}, {}, [], []
    for f in features:
        if f not in cfg["features"]:
            raise SystemExit(f"unknown feature {f!r}; vocabulary: {', '.join(cfg['features'])}")
        why.setdefault(f, []).append("--features")
    for p in paths:
        picked = harness_selection(p, rows) if p.startswith("UITesting/") else None
        if picked is not None:
            for c in picked:
                classes.setdefault(c, []).append(p)
            continue
        repo, feats = feature_map.resolve(cfg, p)
        if feats is None:
            ignored.append(p)
            continue
        for f in sorted(feats):
            if f == "no-e2e":
                reason = cfg["repos"].get(repo, {}).get("no_e2e_reason", "no E2E test can observe this code")
                no_e2e.append(f"{p} ({reason})")
            else:
                why.setdefault(f, []).append(p)
    excluded = NEVER | (set() if include_slow else EXCLUDED_BY_DEFAULT)
    chosen, skipped_slow = {}, []
    for r in rows:
        feats = {t[len("feature-"):] for t in r["tags"] if t.startswith("feature-")}
        if not (feats & set(why) or r["cls"] in classes):
            continue
        if set(r["tags"]) & excluded:
            skipped_slow.append(f"{r['cls']}#{r['method']}")
            continue
        chosen.setdefault(r["cls"], []).append(r["method"])
    return dict(why=why, classes=classes, chosen=chosen, skipped=skipped_slow, no_e2e=no_e2e, ignored=ignored)


def selection_lines(chosen, rows):
    """Runner entries: a whole class when every enabled method is chosen, else Class#m1+m2."""
    all_methods = {}
    for r in rows:
        all_methods.setdefault(r["cls"], []).append(r["method"])
    out = []
    for c in sorted(chosen):
        ms = list(dict.fromkeys(chosen[c]))
        out.append(c if sorted(ms) == sorted(set(all_methods[c])) else f"{c}#{'+'.join(ms)}")
    return out


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--git", action="store_true")
    ap.add_argument("--since")
    ap.add_argument("--files", nargs="*", default=[])
    ap.add_argument("--features", default="")
    ap.add_argument("--include-slow", action="store_true")
    ap.add_argument("--out")
    a = ap.parse_args(argv)
    paths = list(a.files) + (git_changes(a.since) if a.git else [])
    feats = [f.strip() for f in a.features.split(",") if f.strip()]
    if not paths and not feats:
        ap.error("nothing to select from: give --git, --files or --features")
    rows = [r for r in tag_scan.scan() if not r["disabled"]]
    res = select(paths, feats, a.include_slow, rows)
    vocab = set(feature_map.load()["features"])
    by_path = {}
    for f, srcs in res["why"].items():
        for s in srcs:
            by_path.setdefault(s, set()).add(f)
    for s, fs in sorted(by_path.items()):
        print(f"{s} -> {'ALL features (cross-cutting)' if fs == vocab else ', '.join(sorted(fs))}")
    by_harness = {}
    for c, srcs in res["classes"].items():
        for s in srcs:
            by_harness.setdefault(s, set()).add(c)
    for s, cs in sorted(by_harness.items()):
        print(f"{s} -> {len(cs)} test class(es) using it" + (f": {', '.join(sorted(cs))}" if len(cs) <= 5 else ""))
    for n in res["no_e2e"]:
        print(f"no E2E coverage: {n} -> run that repo's unit/contract tests")
    if res["ignored"]:
        print(f"not product code (tests/docs/build), ignored: {len(res['ignored'])}, e.g. {res['ignored'][:3]}")
    if res["skipped"]:
        print(f"slow/auto-cancel left out ({len(res['skipped'])}; --include-slow to run): {res['skipped'][:6]}")
    lines = selection_lines(res["chosen"], rows)
    n = sum(len(set(v)) for v in res["chosen"].values())
    print(f"SELECTED {n} test methods in {len(lines)} classes")
    if a.out:
        pathlib.Path(a.out).write_text("\n".join(lines) + "\n")
        print(f"wrote {a.out}")
    else:
        print("\n".join(lines))
    return 0


if __name__ == "__main__":
    sys.exit(main())
