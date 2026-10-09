#!/usr/bin/env python3
"""Every harness print of browser-supplied text must pass through TestBase.redactAuthValues.

Written 2026-10-08 after a WebSocket token reached saved evidence: CustomerDashboardPage registered its own
console listener that printed msg.text() raw (wss://.../ws/chat?token=...). TestBase already redacted the
same messages, so the leak hid behind a duplicate line.

Rule: a System.out/err print whose arguments read browser text -- a console message's .text(), a dialog's
.message(), or any .url() -- must wrap it in redactAuthValues(...) or UrlPaths.path(...).
Exits 1 and names file:line on a violation; exits 2 (STALE) if it stops finding the known redacted sites.

    python3 e2e-plan/_handoff/tools/check_log_redaction.py [UITesting root]
"""
import pathlib
import re
import sys

BROWSER_TEXT = re.compile(r"\b(?:msg|message|consoleMessage)\.text\(\)|\bdialog\.message\(\)|\.url\(\)")
SAFE_WRAP = re.compile(r"(?:redactAuthValues|UrlPaths\.path)\(")
PRINT = re.compile(r"System\.(?:out|err)\.print(?:ln|f)?\(")


def call_args(src: str, open_paren: int) -> str:
    """The text between a call's parentheses, respecting nesting and string literals."""
    depth, i, quote = 0, open_paren, None
    while i < len(src):
        c = src[i]
        if quote:
            if c == "\\":
                i += 2
                continue
            if c == quote:
                quote = None
        elif c in "\"'":
            quote = c
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return src[open_paren + 1:i]
        i += 1
    return src[open_paren + 1:]


def unsafe_reads(args: str) -> list[str]:
    """Browser-text reads in args that are not inside a redacting wrapper."""
    covered = []
    for m in SAFE_WRAP.finditer(args):
        inner = call_args(args, m.end() - 1)
        covered.append((m.end(), m.end() + len(inner)))
    return [m.group(0) for m in BROWSER_TEXT.finditer(args)
            if not any(a <= m.start() < b for a, b in covered)]


def main() -> int:
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".") / "src/test/java"
    files = sorted(root.rglob("*.java"))
    violations, redacted_sites = [], 0
    for f in files:
        src = f.read_text()
        for m in PRINT.finditer(src):
            args = call_args(src, m.end() - 1)
            if SAFE_WRAP.search(args) and BROWSER_TEXT.search(args):
                redacted_sites += 1
            for read in unsafe_reads(args):
                line = src.count("\n", 0, m.start()) + 1
                violations.append(f"{f.relative_to(root.parent.parent.parent)}:{line}: prints {read} without redactAuthValues")
    if not files or redacted_sites < 3:
        print(f"STALE: scanned {len(files)} files, found {redacted_sites} redacted print sites (expected >= 3: "
              "TestBase console, network error, write log)")
        return 2
    for v in violations:
        print("FAIL", v)
    print(f"{'FAIL' if violations else 'PASS'}: {len(files)} files, {redacted_sites} redacted browser-text prints, "
          f"{len(violations)} unredacted")
    return 1 if violations else 0


if __name__ == "__main__":
    sys.exit(main())
