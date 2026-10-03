#!/usr/bin/env python3
"""Every role an authorization rule names must be a role someone can actually hold.

A rule naming a role that does not exist passes compilation, passes EndpointAuthorizationCoverage
(an annotation is present) and fails closed for everyone it was written for. PayeeWalletController
was hasAnyRole('ADVERTISER','ADMIN') while RoleName has no ADVERTISER, so no advertiser could read
their own wallet.

Valid roles: the constants of CommonLibrary's RoleName enum, plus SERVICE (the service identity the
Feign/propagation interceptors sign, read from their SERVICE_ROLE constants). Both sets are read from
source, not hard-coded.

Scans main sources of every service for hasRole/hasAnyRole/hasAuthority/hasAnyAuthority string
arguments, @RolesAllowed and @Secured, with comments blanked first. Exit 1 on any unknown role.

    python3 validate_role_names.py <workspace root>
"""
import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
SKIP = ("/target/", "/test/", "/node_modules/", "/.git/")


def strip_comments(src):
    out, i, n = [], 0, len(src)
    while i < n:
        if src.startswith("//", i):
            j = src.find("\n", i)
            j = n if j < 0 else j
            out.append(" " * (j - i)); i = j
        elif src.startswith("/*", i):
            j = src.find("*/", i + 2)
            j = n if j < 0 else j + 2
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif src[i] == '"':
            j = i + 1
            while j < n and src[j] != '"':
                j += 2 if src[j] == "\\" else 1
            out.append(src[i:j + 1]); i = j + 1
        else:
            out.append(src[i]); i += 1
    return "".join(out)


def known_roles():
    enum_files = [p for p in ROOT.glob("CommonLibrary/**/enums/RoleName.java") if "/target/" not in str(p)]
    if len(enum_files) != 1:
        sys.exit(f"STALE: expected one CommonLibrary RoleName.java, found {[str(p) for p in enum_files]}")
    body = strip_comments(enum_files[0].read_text())
    start = body.index("{", body.index("enum RoleName"))
    body = body[start + 1:body.index(";", start)]
    roles = set(re.findall(r"\b([A-Z][A-Z_]*)\s*\(", body))
    if not roles:
        sys.exit("STALE: RoleName has no constants this parser recognises")
    service = set()
    for p in ROOT.glob("CommonLibrary/**/security/*.java"):
        if "/target/" in str(p):
            continue
        service |= set(re.findall(r'SERVICE_ROLE\s*=\s*"([A-Z_]+)"', p.read_text()))
    if service != {"SERVICE"}:
        sys.exit(f"STALE: expected the interceptors' SERVICE_ROLE to be SERVICE, found {service}")
    return roles | service


EXPR = re.compile(r"\b(hasRole|hasAnyRole|hasAuthority|hasAnyAuthority)\s*\(([^)]*)\)")
ANNO = re.compile(r"@(?:jakarta\.annotation\.security\.)?(RolesAllowed|Secured)\s*\(([^)]*)\)")


def main():
    roles = known_roles()
    findings, checked = [], 0
    for path in sorted(ROOT.rglob("*.java")):
        s = str(path)
        if any(x in s for x in SKIP) or "/src/main/" not in s:
            continue
        src = strip_comments(path.read_text(errors="replace"))
        for m in list(EXPR.finditer(src)) + list(ANNO.finditer(src)):
            names = re.findall(r"'([A-Za-z_]+)'|\"([A-Za-z_]+)\"", m.group(2))
            for single, double in names:
                name = single or double
                if name.startswith("ROLE_"):
                    name = name[len("ROLE_"):]
                checked += 1
                if name not in roles:
                    line = src.count("\n", 0, m.start()) + 1
                    findings.append(f"{path.relative_to(ROOT)}:{line}: {m.group(1)} names '{name}', not a role")
    if checked < 100:
        sys.exit(f"STALE: only {checked} role names found; the scan is not seeing the rules")
    for f in findings:
        print("FAIL", f)
    print(f"{'FAIL' if findings else 'PASS'}: {checked} role names checked against {sorted(roles)}; {len(findings)} unknown")
    sys.exit(1 if findings else 0)


if __name__ == "__main__":
    main()
