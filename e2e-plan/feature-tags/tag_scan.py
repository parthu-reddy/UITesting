"""E2E test methods with their effective JUnit tags (class tags + method tags), read from UITesting source.

String literals and comments are blanked before matching code structure (a DisplayName holding "postData()" or ';'
once fooled a regex scanner), and the real source is used only to read annotation values.
"""
import pathlib
import re

SUITE = pathlib.Path(__file__).resolve().parents[2] / "src/test/java/com/fooddelivery/e2e/tests"
TEST_ANN = re.compile(r"@(?:org\.junit\.jupiter\.(?:api|params)\.)?(Test|ParameterizedTest|RepeatedTest|TestFactory)\b")
METHOD = re.compile(r"(?:void|[\w<>\[\]]+)\s+(\w+)\s*\(")
TAG = re.compile(r'@Tag\(\s*"([^"]+)"\s*\)')
DISABLED = re.compile(r"@(?:[\w.]+\.)?Disabled\b")
CLASS = re.compile(r"\b(?:public\s+)?(?:abstract\s+)?class\s+(\w+)")


def blank(src: str) -> str:
    """Comments and string contents replaced by spaces; same length, so offsets stay valid."""
    out, i, n = [], 0, len(src)
    while i < n:
        if src.startswith("//", i):
            j = src.find("\n", i)
            j = n if j == -1 else j
            out.append(" " * (j - i)); i = j
        elif src.startswith("/*", i):
            j = src.find("*/", i + 2)
            j = n if j == -1 else j + 2
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif src[i] == '"':
            if src.startswith('"""', i):
                j = src.find('"""', i + 3)
                j = n if j == -1 else j + 3
            else:
                j = i + 1
                while j < n and src[j] != '"':
                    j += 2 if src[j] == "\\" else 1
                j += 1
            out.append('"' + re.sub(r"[^\n]", " ", src[i + 1:j - 1]) + '"'); i = j
        elif src[i] == "'":
            j = i + 1
            while j < n and src[j] != "'":
                j += 2 if src[j] == "\\" else 1
            j += 1
            out.append(" " * (j - i)); i = j
        else:
            out.append(src[i]); i += 1
    return "".join(out)


def annotation_block(code: str, idx: int) -> tuple[int, int]:
    """[start, idx) of the annotations directly above idx (back to the previous ; { or })."""
    start = max(code.rfind(";", 0, idx), code.rfind("{", 0, idx), code.rfind("}", 0, idx)) + 1
    return start, idx


def scan(suite: pathlib.Path = SUITE):
    """[{cls, file, method, line, class_tags, method_tags, tags, disabled}] for every test method."""
    rows = []
    for path in sorted(suite.rglob("*.java")):
        src = path.read_text()
        code = blank(src)
        cm = CLASS.search(code)
        if not cm:
            continue
        cs, ce = annotation_block(code, cm.start())
        class_tags = TAG.findall(src[cs:cm.start()])
        class_disabled = bool(DISABLED.search(code[cs:cm.start()]))
        for m in TEST_ANN.finditer(code):
            mm = METHOD.search(code, m.end())
            if not mm:
                continue
            bs, _ = annotation_block(code, m.start())
            block_src = src[bs:mm.start()]
            method_tags = TAG.findall(block_src)
            rows.append(dict(
                cls=cm.group(1), file=str(path), method=mm.group(1), line=code.count("\n", 0, mm.start()) + 1,
                class_tags=class_tags, method_tags=method_tags, tags=sorted(set(class_tags + method_tags)),
                disabled=class_disabled or bool(DISABLED.search(code[bs:mm.start()])),
            ))
    return rows
