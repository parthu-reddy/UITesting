#!/usr/bin/env python3
"""Find the rollback-only trap: inside a transactional context (a @Transactional method or class, or a
transactionTemplate.execute* lambda), a try block calls a method of another bean that is
@Transactional, and a catch clause swallows the exception (no throw). When the callee's proxy sees a
RuntimeException leave it, it marks the shared transaction rollback-only, so the caller's commit
throws UnexpectedRollbackException even though it caught the exception.

Heuristic and text-based: prints candidates for reading, it does not decide. Comments and string
literals are blanked before matching so javadoc cannot produce hits.
"""
import re, sys
from pathlib import Path

ROOT = Path(sys.argv[1])
SKIP = ("/target/", "/test/", "/node_modules/", "/.git/")

def strip(src):
    out, i, n = [], 0, len(src)
    while i < n:
        if src.startswith("//", i):
            j = src.find("\n", i); j = n if j < 0 else j
            out.append(" " * (j - i)); i = j
        elif src.startswith("/*", i):
            j = src.find("*/", i + 2); j = n if j < 0 else j + 2
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif src.startswith('"""', i):
            j = src.find('"""', i + 3); j = n if j < 0 else j + 3
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif src[i] == '"':
            j = i + 1
            while j < n and src[j] != '"':
                j += 2 if src[j] == "\\" else 1
            out.append('"' + " " * (j - i - 1) + '"'); i = j + 1
        else:
            out.append(src[i]); i += 1
    return "".join(out)

def block_end(s, open_idx):
    depth = 0
    for k in range(open_idx, len(s)):
        if s[k] == "{": depth += 1
        elif s[k] == "}":
            depth -= 1
            if depth == 0: return k
    return len(s) - 1

files = [p for p in ROOT.rglob("*.java") if "/src/main/" in str(p) and not any(x in str(p) for x in SKIP)]
text = {p: strip(p.read_text(errors="ignore")) for p in files}

# Which simple class names declare @Transactional anywhere (class or method)? Name -> set(method names)|{"*"}
tx_methods = {}
for p, s in text.items():
    m = re.search(r"\b(class|interface)\s+(\w+)", s)
    if not m: continue
    name = m.group(2); methods = set()
    head = s[:m.start()]
    if "@Transactional" in head: methods.add("*")
    for t in re.finditer(r"@Transactional[^\n]*\n(?:\s*@\w+[^\n]*\n)*\s*(?:public|protected)?[^(=;{]*?\b(\w+)\s*\(", s):
        methods.add(t.group(1))
    if re.search(r"interface\s+\w+\s+extends\s+[^{]*(JpaRepository|CrudRepository|Repository)\b", s):
        methods.add("*")  # Spring Data methods run in (read-only) transactions
    if methods: tx_methods.setdefault(name, set()).update(methods)

def tx_spans(s):
    spans = []
    for m in re.finditer(r"\btransactionTemplate\s*\.\s*execute\w*\s*\(", s):
        brace = s.find("{", m.end())
        if brace > 0: spans.append((brace, block_end(s, brace), "transactionTemplate"))
    class_tx = re.search(r"@Transactional[^\n]*\n(?:\s*@[^\n]*\n)*\s*(public\s+)?(final\s+)?class\b", s) is not None
    for m in re.finditer(r"(@Transactional[^\n]*\n(?:\s*@[^\n]*\n)*)?\s*(public|protected|private)\s+[\w<>\[\], ?.]+\s+(\w+)\s*\([^)]*\)\s*(throws[^{]*)?\{", s):
        if m.group(1) or class_tx:
            if m.group(1) and "readOnly" in m.group(1) and False: continue
            brace = s.find("{", m.start(3))
            spans.append((brace, block_end(s, brace), "@Transactional " + m.group(3)))
    return spans

hits = 0
for p, s in sorted(text.items()):
    fields = dict((n, t) for t, n in re.findall(r"private\s+final\s+([\w.]+(?:<[^>]*>)?)\s+(\w+)\s*;", s))
    fields = {n: t.split(".")[-1].split("<")[0] for n, t in fields.items()}
    for start, end, kind in tx_spans(s):
        body = s[start:end]
        for t in re.finditer(r"\btry\s*\{", body):
            t_open = t.end() - 1; t_close = block_end(body, t_open)
            tried = body[t_open:t_close]
            rest = body[t_close + 1:]
            calls = [(f, meth) for f, meth in re.findall(r"\b(\w+)\s*\.\s*(\w+)\s*\(", tried)
                     if f in fields and fields[f] in tx_methods and ("*" in tx_methods[fields[f]] or meth in tx_methods[fields[f]])]
            if not calls: continue
            for c in re.finditer(r"^\s*catch\s*\(([^)]*)\)\s*\{", rest, re.M):
                c_open = c.end() - 1; c_close = block_end(rest, c_open)
                if re.search(r"\bthrow\b", rest[c_open:c_close]): continue
                caught = c.group(1)
                if not re.search(r"\b(Exception|RuntimeException|IllegalArgumentException|IllegalStateException|\w+Exception)\b", caught): continue
                if re.search(r"\b(JsonProcessingException|IOException|InterruptedException)\b", caught) and "|" not in caught: continue
                line = s[:start + t_open].count("\n") + 1
                hits += 1
                callees = sorted({f"{fields[f]}.{m}" for f, m in calls})
                print(f"{p.relative_to(ROOT)}:{line}  [{kind}]  catch({caught.strip()})  calls {', '.join(callees)}")
                break
            # nested catch blocks after a non-matching first catch are covered by the loop above
print(f"\n{hits} candidate(s)")
