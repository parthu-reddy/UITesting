#!/usr/bin/env python3
"""The ONLY way to run more than one E2E class against Dev. Written after P0-2 batch 1 (2026-10-07)
lost ~80 of 89 minutes to two repeated setup failures that nobody saw until the owner asked.

Guarantees:
  1. Refuses to start while a deploy is in progress (`_handoff/DEPLOY-IN-PROGRESS` exists; create it
     when the owner says they are deploying, delete it when they say it is deployed).
  2. Compiles first, then runs a CANARY per role (customer, restaurant, rider, admin). Any canary
     failure stops the batch before the real work starts.
  3. Runs one class per Maven invocation and prints progress + ETA after each.
  4. Paces admin step-up: Identity allows 5 verifies / 5 min and 10 code sends / 10 min per admin
     phone (IdentityService AuthService.sendOtp/verifyOtp), so step-ups rotate over the four seeded admin phones.
     A class that signs in only through TestBase.loginAsAdmin() shares one admin session (util/ClassAdminSession)
     and runs as ONE unit costing one step-up; any other admin class runs one METHOD at a time,
     and the runner waits until the rolling windows allow another step-up. A method that still hits
     ADMIN_STEP_UP_RATE_LIMITED is retried once after a full window. The step-up history persists in
     `_handoff/ADMIN-STEP-UPS.json`, so a new invocation inherits the budget the last one spent.
  5. Fails fast: a failure signature (first suite frame + first message line) seen in a SECOND class
     stops the batch. One setup defect never burns an hour again.
  6. Copies surefire XML with mtimes preserved into --evidence, plus summary.json, plus each
     invocation's console output (OTP/token lines removed) as logs/<n>-<selector>.log: the
     [BROWSER NETWORK ERROR] lines are often the only record of WHY a step failed.

    python3 run_e2e_batch.py --classes RestaurantUiTest,AdminUiTest --evidence DIR
    python3 run_e2e_batch.py --selection FILE --evidence DIR        (comma/newline separated)
    python3 run_e2e_batch.py --changed [--since REF] --evidence DIR  (only the tests whose features the change touches)
    python3 run_e2e_batch.py --features chat,reviews --evidence DIR    (feature tags; slow/auto-cancel need --include-slow)
    python3 run_e2e_batch.py ... --dry-run                          (print the plan and the pacing only)
    python3 run_e2e_batch.py ... --debug                            (watchable: 400 ms slow-mo, visible browser, video)
    Entries may be `Class` or `Class#m1+m2`; fixture properties go through --prop key=value (repeatable).
"""
from __future__ import annotations

import argparse
import collections
import glob
import json
import os
import re
import shutil
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
HANDOFF = os.path.dirname(HERE)
UITESTING = os.path.abspath(os.path.join(HANDOFF, "..", ".."))
SUITE = os.path.join(UITESTING, "src", "test", "java")
REPORTS = os.path.join(UITESTING, "target", "surefire-reports")
LOCK = os.path.join(HANDOFF, "DEPLOY-IN-PROGRESS")
# One live run at a time: every run signs in the same seeded people, and Identity keeps at most 3
# sessions per person (identity.sessions.max-per-user), so two runs evict each other's sessions.
RUN_LOCK = os.path.join(HANDOFF, "RUN-IN-PROGRESS")


def take_run_lock():
    if os.path.exists(RUN_LOCK):
        try:
            pid = int(open(RUN_LOCK).read().split()[0])
            os.kill(pid, 0)
            sys.exit(f"REFUSED: another live run (pid {pid}) holds {RUN_LOCK}; seeded sessions would collide.")
        except (ValueError, IndexError, ProcessLookupError):
            pass  # stale lock from a run that died
    with open(RUN_LOCK, "w") as fh:
        fh.write(f"{os.getpid()} {time.strftime('%Y-%m-%dT%H:%M:%S')}\n")
    import atexit
    atexit.register(lambda: os.path.exists(RUN_LOCK) and open(RUN_LOCK).read().startswith(f"{os.getpid()} ")
                    and os.remove(RUN_LOCK))
PHONES = ["-Dcustomer.phone=8000000001", "-Drestaurant.phone=9000000001", "-Drider.phone=7000000001"]
# Admin phones that exist on Dev (identity_db, read-only check). Identity's step-up limits are per phone, so each
# one is a separate budget. Only list a phone once Dev has it: signing in with an unknown phone creates a plain user
# (AuthService.createSession), which the seed's admin collision guard then refuses. The seed provisions 1000000001-004
# (generate_scenario_data.py); 003 and 004 were confirmed ADMIN on Dev after the 2026-10-09 scenario reseed.
ADMIN_PHONES = ["1000000001", "1000000002", "1000000003", "1000000004"]
# Never run: parked rate limits, SSE over the tunnel and latency measurements (tags, not names).
ALWAYS_EXCLUDED = ["auth-rate-limit", "parked", "measurement"]
# COMMON[0] is rewritten by --exclude-tags.
COMMON = ["-DexcludedGroups=" + ",".join(ALWAYS_EXCLUDED), "-Dsurefire.failIfNoSpecifiedTests=false",
          "-Dsurefire.rerunFailingTestsCount=0"]
# Read-only, green on 2026-10-07; one per role. Each exercises that role's sign-in and landing screen.
CANARIES = {
    "customer": "PageReloadRecoveryTest#sessionPersistsAcrossReload",
    "restaurant": "RestaurantUiTest#verifyRestaurantDashboardUI",
    "rider": "RiderOnboardingTest#checkOnboardingWizard",
    "admin": "AdminUserOpsTest#adminUserManagement",
}
# (seconds, max step-ups). Identity allows 5 verifies / 5 min and 10 sends / 10 min; one under each,
# because a test can step up more than once (P0-2 run 2 hit 429 twice at exactly 5 / 5 min).
ADMIN_WINDOWS = [(300, 4), (600, 8)]
RATE_LIMITED = "ADMIN_STEP_UP_RATE_LIMITED"
stepups: dict[str, list[float]] = {p: [] for p in ADMIN_PHONES}
# Identity's windows outlive one invocation, so the step-up history must too: back-to-back batches each
# started with an empty budget and hit ADMIN_STEP_UP_RATE_LIMITED (checkpoint132). Epoch seconds only.
STEPUP_HISTORY = os.path.join(HANDOFF, "ADMIN-STEP-UPS.json")


def load_stepups(now):
    """Merge earlier invocations' step-ups (per admin phone) that still fall inside the longest window."""
    if not os.path.exists(STEPUP_HISTORY):
        return
    try:
        saved = json.load(open(STEPUP_HISTORY))
        if not isinstance(saved, dict) or not all(isinstance(v, list) for v in saved.values()):
            raise ValueError("expected {admin phone: [epoch seconds]}")
    except ValueError as e:
        sys.exit(f"REFUSED: {STEPUP_HISTORY} is unreadable ({e}); without it the admin budget is unknown. "
                 "Delete it only if no admin step-up happened in the last 10 minutes.")
    horizon = max(w for w, _n in ADMIN_WINDOWS)
    # Called once per process, before any step-up; lists, not sets: equal timestamps are separate step-ups.
    for phone in ADMIN_PHONES:
        stepups[phone] = sorted(stepups[phone] + [t for t in saved.get(phone, []) if now - t < horizon])


def record_stepup(dry, phone):
    stepups[phone].append(clock(dry))
    if not dry:
        tmp = STEPUP_HISTORY + ".tmp"
        with open(tmp, "w") as fh:
            json.dump(stepups, fh)
        os.replace(tmp, STEPUP_HISTORY)


def log(msg):
    print(f"[batch {time.strftime('%H:%M:%S')}] {msg}", flush=True)


def class_source(simple):
    hits = glob.glob(os.path.join(SUITE, "**", simple + ".java"), recursive=True)
    if len(hits) != 1:
        sys.exit(f"class {simple}: {len(hits)} source files")
    return open(hits[0], encoding="utf-8").read()


def test_methods(src):
    return re.findall(r"@(?:org\.junit\.jupiter\.(?:api|params)\.)?(?:Test|ParameterizedTest|RepeatedTest)\b"
                      r"(?:\([^)]*\))?[\s\S]*?\bvoid\s+(\w+)\s*\(", src)


# Admin sign-ins that do not go through TestBase.loginAsAdmin(): each one is its own step-up. testAdminPhone /
# Account.ADMIN / ADMIN_PROFILE_NAME are parameterized sign-ins (SessionUiTest hit 429 unpaced, checkpoint134).
OWN_ADMIN_SIGN_IN = r"Portal\.ADMIN|stepUpAdmin|testAdminPhone|Account\.ADMIN|ADMIN_PROFILE_NAME"


def uses_admin(src):
    # Over-matching only adds pacing; under-matching costs a rate-limited failure.
    return bool(re.search(OWN_ADMIN_SIGN_IN + r"|loginAsAdmin|adminPage\.navigate", src))


def shares_class_admin_session(src):
    """Signs in only through loginAsAdmin(): one step-up for the whole class (util/ClassAdminSession)."""
    return "loginAsAdmin" in src and not re.search(OWN_ADMIN_SIGN_IN, src)


SIM = {"now": None, "waited": 0.0}  # dry-run clock: simulated waits advance it, so the printed duration is real
EST_ADMIN_TEST_S = 40  # observed: an admin test is ~30-60 s on the Dev tunnel


def clock(dry):
    return SIM["now"] if dry else time.time()


def phone_wait(dry, phone):
    """Seconds until another step-up on this admin phone fits every rolling window (0 = now)."""
    if dry and SIM["now"] is None:
        SIM["now"] = time.time()
    now = clock(dry)
    waits = [0.0]
    for window, limit in ADMIN_WINDOWS:
        recent = sorted(t for t in stepups[phone] if now - t < window)
        if len(recent) >= limit:
            waits.append(recent[len(recent) - limit] + window - now + 2)
    return max(waits)


def pick_phone(dry):
    """The admin phone that can step up soonest (the first one on a tie), and its wait."""
    return min(((phone_wait(dry, p), p) for p in ADMIN_PHONES), key=lambda wp: wp[0])[::-1]


def admin_wait(dry):
    """Seconds until any admin phone can step up (0 = now)."""
    return pick_phone(dry)[1]


def wait_for_admin_budget(dry, est_s=None):
    """Block until some admin phone fits every rolling window, record the step-up on it and return the phone."""
    while True:
        phone, pause = pick_phone(dry)
        if pause <= 0:
            break
        used = sum(len(v) for v in stepups.values())
        log(f"admin step-up budget used on all {len(ADMIN_PHONES)} admin phones ({used} so far): waiting {pause:.0f}s")
        if dry:
            SIM["now"] += pause
            SIM["waited"] += pause
        else:
            time.sleep(pause)
    record_stepup(dry, phone)
    if dry:
        SIM["now"] += EST_ADMIN_TEST_S if est_s is None else est_s
    return phone


EXTRA: list[str] = []  # -Dkey=value fixture properties from --prop
# TestConfig defaults to fast runs (no slow-mo, headless, no video); --debug brings back the watchable settings.
DEBUG_FLAGS = ["-Dslow.mo=400", "-Dheadless=false", "-Drecord.video=true"]
DEBUG: list[str] = []
LOGS = {"dir": None, "n": 0}
SECRET = re.compile(r"otp|token=|Bearer |password", re.I)


def run(selector, dry, admin_phone=ADMIN_PHONES[0]):
    """One Maven invocation; returns (exit, list of testcase dicts from the reports it wrote)."""
    if dry:
        log(f"would run -Dtest={selector} as admin {admin_phone}")
        return 0, []
    shutil.rmtree(REPORTS, ignore_errors=True)
    cmd = ["mvn", "-q", f"-Dtest={selector}", *PHONES, f"-Dadmin.phone={admin_phone}", *COMMON, *EXTRA, *DEBUG, "test"]
    env = {**os.environ, "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD": "1"}
    proc = subprocess.run(cmd, cwd=UITESTING, env=env, capture_output=True, text=True)
    if LOGS["dir"]:
        LOGS["n"] += 1
        safe = re.sub(r"[^\w#+.-]", "_", selector)[:150]
        with open(os.path.join(LOGS["dir"], f"{LOGS['n']:03d}-{safe}.log"), "w", encoding="utf-8") as fh:
            fh.write("\n".join(l for l in (proc.stdout + proc.stderr).splitlines() if not SECRET.search(l)))
    cases = []
    for f in glob.glob(os.path.join(REPORTS, "TEST-*.xml")):
        for tc in ET.parse(f).getroot().iter("testcase"):
            outcome, sig, text = "pass", None, ""
            for tag in ("failure", "error", "skipped"):
                el = tc.find(tag)
                if el is not None:
                    outcome, text = tag, (el.text or "") + (el.get("message") or "")
                    frame = re.search(r"at (com\.fooddelivery\.e2e\.[\w.$]+)\((\w+\.java):(\d+)\)", text)
                    first = (el.get("message") or el.get("type") or "").strip().splitlines()[:1]
                    sig = f"{frame.group(2)}:{frame.group(3)} {first[0][:80] if first else ''}" if frame else (first[0][:100] if first else tag)
                    break
            cases.append(dict(cls=tc.get("classname"), name=tc.get("name"), outcome=outcome, signature=sig,
                              rate_limited=RATE_LIMITED in text, report=f))
    return proc.returncode, cases


def keep(cases, evidence):
    for path in {c["report"] for c in cases}:
        shutil.copy2(path, os.path.join(evidence, os.path.basename(path).replace("TEST-", f"TEST-{int(time.time())}-", 1)))


def feature_selection(a):
    """Runner entries for --changed/--features, via the feature map (UITesting/e2e-plan/feature-tags)."""
    sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "feature-tags"))
    import select_tests
    import tag_scan
    rows = [r for r in tag_scan.scan() if not r["disabled"]]
    paths = select_tests.git_changes(a.since) if a.changed else []
    feats = [f.strip() for f in a.features.split(",") if f.strip()]
    res = select_tests.select(paths, feats, a.include_slow, rows)
    lines = select_tests.selection_lines(res["chosen"], rows)
    log(f"feature selection: {', '.join(sorted(res['why'])) or '-'}; changed harness classes {sorted(res['classes'])}; "
        f"{len(res['skipped'])} slow/auto-cancel left out; no-e2e {len(res['no_e2e'])}")
    for n in res["no_e2e"]:
        log(f"no E2E coverage: {n}")
    if not lines:
        sys.exit("nothing selected: no changed path maps to a feature with runnable tests")
    return "\n".join(lines)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--classes")
    ap.add_argument("--selection")
    ap.add_argument("--changed", action="store_true", help="select from git changes (feature-tags/select_tests.py --git)")
    ap.add_argument("--since", help="with --changed: also commits since this ref in each repo")
    ap.add_argument("--features", default="", help="comma-separated feature tags without the feature- prefix")
    ap.add_argument("--include-slow", action="store_true", help="with --changed/--features: keep slow and auto-cancel tests")
    ap.add_argument("--evidence", required=True)
    ap.add_argument("--skip-canary", action="store_true", help="only when the canaries passed minutes ago in this session")
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--debug", action="store_true", help="400 ms slow-mo, visible browser and video, to watch a run")
    ap.add_argument("--prop", action="append", default=[], help="fixture property key=value, passed as -Dkey=value")
    ap.add_argument("--exclude-tags", default="", help="extra JUnit tags to exclude, e.g. slow,auto-cancel (added to "
                    "the always-excluded auth-rate-limit,parked,measurement)")
    a = ap.parse_args()
    if a.changed or a.features:
        raw = feature_selection(a)
    else:
        raw = a.classes or open(a.selection).read()
    classes = [c.strip() for c in re.split(r"[,\s]+", raw) if c.strip()]
    EXTRA[:] = [f"-D{p}" for p in a.prop]
    DEBUG[:] = DEBUG_FLAGS if a.debug else []
    extra_tags = [t.strip() for t in a.exclude_tags.split(",") if t.strip()]
    COMMON[0] = "-DexcludedGroups=" + ",".join([*ALWAYS_EXCLUDED, *extra_tags])
    if os.path.exists(LOCK):
        sys.exit(f"REFUSED: {LOCK} exists -- the owner reported a deploy in progress. Wait for 'deployed'.")
    os.makedirs(a.evidence, exist_ok=True)
    if not a.dry_run:
        take_run_lock()
        LOGS["dir"] = os.path.join(a.evidence, "logs")
        os.makedirs(LOGS["dir"], exist_ok=True)
    load_stepups(time.time())
    plan = []
    for entry in classes:
        c, _, only = entry.partition("#")
        src = class_source(c)
        methods = [m for m in only.split("+") if m] or test_methods(src)
        unknown = set(methods) - set(test_methods(src))
        if unknown:
            sys.exit(f"{c}: no test methods {sorted(unknown)}")
        admin = uses_admin(src)
        shared = admin and shares_class_admin_session(src)
        # Shared-session admin classes are one unit (one step-up); other admin classes run method by method
        # (each its own step-up); a method subset of a non-admin class runs as one invocation.
        plan.append((c, admin, shared, methods if (admin or only) else None))
    stepups_planned = sum(1 if shared else len(m) for _c, adm, shared, m in plan if adm)
    log(f"{len(plan)} classes, {stepups_planned} admin step-ups paced at "
        + ", ".join(f"{n} per {w // 60} min" for w, n in ADMIN_WINDOWS) + f" on each of {len(ADMIN_PHONES)} admin phones")
    if not a.dry_run:
        rc = subprocess.run(["mvn", "-q", "test-compile"], cwd=UITESTING, capture_output=True, text=True,
                            env={**os.environ, "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD": "1"})
        if rc.returncode:
            sys.exit(f"STOPPED: test-compile failed\n{rc.stdout[-2000:]}{rc.stderr[-2000:]}")
    results, signatures, started, done = [], collections.defaultdict(set), time.time(), 0
    if not a.skip_canary:
        for role, sel in CANARIES.items():
            phone = wait_for_admin_budget(a.dry_run) if role == "admin" else ADMIN_PHONES[0]
            _rc, cases = run(sel, a.dry_run, phone)
            if not a.dry_run:
                keep(cases, a.evidence)
                bad = [c for c in cases if c["outcome"] != "pass"]
                if not cases or bad:
                    sys.exit(f"STOPPED at {role} canary {sel}: {[(c['name'], c['signature']) for c in bad] or 'no report'}")
                log(f"canary {role} ok")
    # Work units: shared-session admin classes whole (one step-up), other admin classes per METHOD (each is one
    # step-up), others per class. While every admin phone's budget is used up, the next non-admin unit runs
    # instead of the runner sleeping.
    admin_q, other_q, remaining = collections.deque(), collections.deque(), collections.Counter()
    for c, admin, shared, methods in plan:
        if admin and not shared:
            units = [(f"{c}#{m}", 1) for m in methods]
        else:
            units = [(f"{c}#{'+'.join(methods)}" if methods else c, len(methods) if methods else 1)]
        (admin_q if admin else other_q).extend((c, u, n) for u, n in units)
        remaining[c] += len(units)
    by_class = collections.defaultdict(list)
    while admin_q or other_q:
        if admin_q and (admin_wait(a.dry_run) == 0 or not other_q):
            c, sel, n = admin_q.popleft()
            phone = wait_for_admin_budget(a.dry_run, EST_ADMIN_TEST_S * n)
            _rc, got = run(sel, a.dry_run, phone)
            if any(x["rate_limited"] for x in got):
                # The refused attempt still cost Identity a send; keep the history (clearing it let
                # the retry run into the 10-minute send window) and let the windows decide the wait.
                log(f"{sel}: admin step-up still rate-limited on {phone}; one retry once the windows allow it")
                record_stepup(a.dry_run, phone)
                if not a.dry_run:
                    time.sleep(ADMIN_WINDOWS[0][0] + 5)
                phone = wait_for_admin_budget(a.dry_run, EST_ADMIN_TEST_S * n)
                _rc, got = run(sel, a.dry_run, phone)
        else:
            c, sel, _n = other_q.popleft()
            _rc, got = run(sel, a.dry_run)
        if not a.dry_run:
            keep(got, a.evidence)
        by_class[c] += got
        results += got
        remaining[c] -= 1
        for x in got:
            if x["signature"] and not x["rate_limited"]:
                signatures[x["signature"]].add(c)
        if remaining[c] == 0:
            done += 1
            counts = collections.Counter(x["outcome"] for x in by_class[c])
            eta = (time.time() - started) / done * (len(plan) - done)
            log(f"[{done}/{len(plan)}] {c}: {dict(counts)}; ETA {eta / 60:.0f} min")
        repeated = {s: sorted(cs) for s, cs in signatures.items() if len(cs) >= 2}
        if repeated:
            sig, where = next(iter(repeated.items()))
            json.dump(dict(stopped=True, signature=sig, classes=where, results=results),
                      open(os.path.join(a.evidence, "summary.json"), "w"), indent=1)
            sys.exit(f"STOPPED (fail-fast): the same failure in {len(where)} classes {where}:\n  {sig}\n"
                     "Diagnose it before running anything else.")
    if a.dry_run and SIM["now"]:
        log(f"dry run: ~{SIM['waited'] / 60:.0f} min waiting for admin step-up budget; admin units span "
            f"~{(SIM['now'] - started) / 60:.0f} min with ~{EST_ADMIN_TEST_S} s per admin method (non-admin classes add their own time)")
    json.dump(dict(stopped=False, results=results), open(os.path.join(a.evidence, "summary.json"), "w"), indent=1)
    log(f"done: {dict(collections.Counter(x['outcome'] for x in results))}")


if __name__ == "__main__":
    main()
