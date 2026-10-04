#!/usr/bin/env python3
"""Run the O3 browser gates against the public Dev UI.

This runner creates only a local, redacted allocation manifest.  It does not open SSH
connections, read an application database or Redis, call product APIs, alter browser storage,
seed data, or clean up test data.  Each candidate phone is exercised through the normal visible
registration screen; an already-registered candidate is a visible test failure and is retained in
the manifest for investigation.
"""
import argparse
import fcntl
import json
import os
from pathlib import Path
import secrets
import subprocess
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
CASES = {
    "restaurant": "RestaurantApplicationApiTest#restaurantApplicationLifecycle",
    "delivery": "DeliveryApplicationApiTest#deliveryApplicationLifecycle",
    "admin": "AdminPartnerApprovalsUiTest#privateReviewAndDecisions",
    "hidden": "UnapprovedOutletHiddenTest#pendingAndRejectedOutletsAreNotDiscoverableThroughTheCustomerUI",
}
PHONE_NEEDS = {
    "restaurant": (("restaurant.lifecycle", "9999"),),
    "delivery": (("delivery.lifecycle", "7999"),),
    "admin": (("restaurant.admin", "9999"), ("delivery.admin", "7999")),
    "hidden": (),
}


def candidate(prefix: str, used: set[str]) -> str:
    while True:
        phone = prefix + f"{secrets.randbelow(1_000_000):06d}"
        if phone not in used:
            used.add(phone)
            return phone


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--app-url", required=True, help="Public Oracle Dev UI URL")
    parser.add_argument("--only", choices=CASES, help="Run one browser gate")
    args = parser.parse_args()

    url = urlparse(args.app_url)
    if url.scheme != "https" or not url.hostname or not url.hostname.endswith(".trycloudflare.com"):
        raise ValueError("Use the current public Oracle HTTPS tunnel")

    selected = [args.only] if args.only else list(CASES)
    folder = ROOT / "target" / "business-platform" / "o3"
    locks = ROOT / "target" / "registration"
    folder.mkdir(parents=True, exist_ok=True)
    locks.mkdir(parents=True, exist_ok=True)

    with (locks / "runner.lock").open("w") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        phones: dict[str, str] = {}
        used: set[str] = set()
        for case in selected:
            for key, prefix in PHONE_NEEDS[case]:
                phones[key] = candidate(prefix, used)

        run_id = secrets.token_hex(8)
        manifest = folder / f"allocation-{run_id}.json"
        report = {
            "runId": run_id,
            "appUrl": args.app_url,
            "selectedCases": selected,
            "phoneCandidates": phones,
            "allocationPolicy": "local-random-candidates-only; no server lookup",
            "dataPolicy": "retain",
            "cleanupPerformed": False,
            "externalDatabaseOrRedisAccess": False,
        }
        manifest.write_text(json.dumps(report, indent=2) + "\n")

        command = [
            "mvn", "-q", "-Dtest=" + ",".join(CASES[case] for case in selected),
            "-Dapp.url=" + args.app_url,
            "-Dbp.o3.preflight=true",
            "-Dcustomer.phone=8000000001",
            "-Drestaurant.phone=9000000001",
            "-Drider.phone=7000000001",
            "-Dadmin.phone=1000000001",
            "-Dheadless=true",
            "-Dslow.mo=0",
            "-Drecord.video=false",
            "-DexcludedGroups=slow-auth,auth-rate-limit",
        ]
        command.extend(f"-Dbp.o3.phone.{key}={phone}" for key, phone in phones.items())
        command.append("test")
        print("Running browser-only O3 gate; retained local allocation: " + str(manifest), flush=True)
        result = subprocess.run(
            command,
            cwd=ROOT,
            env={**os.environ, "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD": "1"},
        )
        report["exitCode"] = result.returncode
        manifest.write_text(json.dumps(report, indent=2) + "\n")
        print("All O3 applicant data is retained; no cleanup performed", flush=True)
        return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
