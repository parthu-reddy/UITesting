#!/usr/bin/env python3
"""Source path -> product features, from features.json (owner request 2026-10-08: run only the E2E tests a change needs).

    python3 feature_map.py --coverage            every in-scope file resolves; every rule matches a file (exit 1 if not)
    python3 feature_map.py PATH [PATH...]        print the features of workspace-relative paths

A path is workspace-relative: "<Repo>/<repo-relative path>". Resolution for a file in a repo:
  - excluded (tests, docs, build output) or outside the repo's include patterns -> not in scope (no features)
  - union of the features of every matching rule; '*' = the repo's features, 'ALL' = every vocabulary feature
  - no rule matched: strict repo -> UNMAPPED (a gate failure), otherwise the repo's fallback
"""
import json
import os
import pathlib
import re
import subprocess
import sys

HERE = pathlib.Path(__file__).resolve().parent
WORKSPACE = HERE.parents[2]  # UITesting/e2e-plan/feature-tags -> workspace root
DEFAULT_INCLUDE = [r"(^|/)src/main/", r"(^|/)pom\.xml$", r"^Dockerfile$"]
UNMAPPED = "UNMAPPED"


def load(path=HERE / "features.json"):
    cfg = json.loads(pathlib.Path(path).read_text())
    cfg["_exclude"] = [re.compile(x) for x in cfg["exclude"]]
    for repo in cfg["repos"].values():
        repo["_include"] = [re.compile(x) for x in repo.get("include", DEFAULT_INCLUDE)]
        repo["_rules"] = [(re.compile(rx), feats) for rx, feats in repo["rules"]]
    return cfg


def vocabulary(cfg):
    return list(cfg["features"])


def expand(cfg, repo, feats):
    out = set()
    for f in feats:
        if f == "ALL":
            out |= set(vocabulary(cfg))
        elif f == "*":
            out |= expand(cfg, repo, repo["features"])
        else:
            out.add(f)
    return out


def in_scope(cfg, repo, rel):
    return (any(rx.search(rel) for rx in repo["_include"])
            and not any(rx.search(rel) for rx in cfg["_exclude"]))


def resolve(cfg, ws_path):
    """(repo name, set of features) for a workspace-relative path; features None when out of scope."""
    parts = pathlib.PurePosixPath(ws_path).parts
    if not parts or parts[0] not in cfg["repos"]:
        return None, None
    name, repo = parts[0], cfg["repos"][parts[0]]
    rel = "/".join(parts[1:])
    if not in_scope(cfg, repo, rel):
        return name, None
    feats = set()
    for rx, rule in repo["_rules"]:
        if rx.search(rel):
            feats |= expand(cfg, repo, rule)
    if not feats:
        feats = {UNMAPPED} if repo.get("strict") else expand(cfg, repo, repo.get("fallback", []))
    return name, feats


def repo_files(name):
    """Files of a repo: git ls-files plus untracked, so a new uncommitted file is checked too."""
    root = WORKSPACE / name
    if (root / ".git").exists():
        out = subprocess.run(["git", "-C", str(root), "ls-files", "--cached", "--others", "--exclude-standard"],
                             capture_output=True, text=True, check=True).stdout.split("\n")
        return [p for p in out if p and (root / p).is_file()]
    return [str(p.relative_to(root)) for p in root.rglob("*") if p.is_file()]


def coverage(cfg):
    problems, scanned, rule_hits = [], 0, {}
    vocab = set(vocabulary(cfg)) | {"no-e2e"}
    for name, repo in cfg["repos"].items():
        for f in repo["features"] + repo.get("fallback", []):
            if f not in vocab | {"ALL", "*"}:
                problems.append(f"{name}: unknown feature {f!r} in features/fallback")
        for rx, feats in repo["rules"]:
            for f in feats:
                if f not in vocab | {"ALL", "*"}:
                    problems.append(f"{name}: unknown feature {f!r} in rule {rx}")
        if not (WORKSPACE / name).is_dir():
            problems.append(f"{name}: repo directory missing")
            continue
        for rel in repo_files(name):
            if not in_scope(cfg, repo, rel):
                continue
            scanned += 1
            for rx, _ in repo["_rules"]:
                if rx.search(rel):
                    rule_hits[(name, rx.pattern)] = rule_hits.get((name, rx.pattern), 0) + 1
            _, feats = resolve(cfg, f"{name}/{rel}")
            if UNMAPPED in feats:
                problems.append(f"UNMAPPED {name}/{rel}")
        for rx, _ in repo["_rules"]:
            if (name, rx.pattern) not in rule_hits:
                problems.append(f"DEAD RULE {name}: {rx.pattern}")
    return scanned, problems


def main(argv):
    cfg = load()
    if argv[1:] == ["--coverage"]:
        scanned, problems = coverage(cfg)
        for p in problems:
            print(p)
        if scanned < 1000:
            print(f"STALE: only {scanned} in-scope files found (expected > 1000)")
            return 2
        print(f"{'FAIL' if problems else 'PASS'}: {scanned} in-scope files, {len(problems)} problems")
        return 1 if problems else 0
    for path in argv[1:]:
        repo, feats = resolve(cfg, path)
        print(f"{path}: {'out of scope' if feats is None else ', '.join(sorted(feats))}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
