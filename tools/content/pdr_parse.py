#!/usr/bin/env python3
"""Parse the official ПДР text (Постанова КМУ № 1306) into point-by-point JSON.

Input: plain text of https://zakon.rada.gov.ua/laws/show/1306-2001-п (page text,
saved from a browser). Output: JSON with sections 1–34, each split into its
numbered points (1.1, 2.3, 33 → signs, 34 → markings), in the official order.

Editorial notes in {curly braces} (amendment history) are removed; points the
rules mark as deleted («виключено») are kept with "excluded": true so the
numbering stays complete.

    python3 tools/content/pdr_parse.py pdr-1306.txt feature/learn/src/main/assets/pdr/pdr.json
"""
import json
import re
import sys

SECTION_RE = re.compile(r"^(\d{1,2})\. ([А-ЯІЇЄҐ'’ ,\-–]+?)\*?$")
POINT_RE = re.compile(r"^(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)(?:\.\s*|\s+)(.*)$")
EXCL_RE = re.compile(r"^\{Пункти? (\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)(?:\s*(?:,|-|–|і)\s*(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?))? виключено")
# Sections 33/34: items start with a sign / marking code without a trailing dot:
# "1.1 “…”", "1.3.1, 1.3.2 “…”", "1.4.1-1.4.7 “…”", "1.1 (вузька суцільна лінія) - …", "2.1 - …"
CODE_RE = re.compile(r"^(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)((?:\s*(?:,|-|–|і)\s*\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)*)\.?(\s.*|)$")
GROUP_RE = re.compile(r"^(\d{1,2})\. ([А-ЯІЇЄҐ][^.]{2,80})$")
SUBITEM_RE = re.compile(r"^([а-яієїґ]|\d{1,2})\)\s")
NOTE_RE = re.compile(r"\{[^{}]*\}")


def quotes(s: str) -> str:
    s = s.replace("“", "«").replace("”", "»").replace("„", "«")
    out, open_ = [], True
    for ch in s:
        if ch == '"':
            out.append("«" if open_ else "»")
            open_ = not open_
        else:
            out.append(ch)
    s = "".join(out)
    s = s.replace("« ", "«").replace(" »", "»")
    return s


def clean(line: str) -> str:
    line = line.replace(" ", " ").replace("\t", " ")
    return re.sub(r"\s+", " ", line).strip()


def code_key(code: str):
    return tuple(int(x) for x in code.split("."))


def _note(m):
    note = m.group(0)
    e = EXCL_RE.match(note)
    if e:  # «{Пункт 24.6 виключено …}» — keep the number as an excluded point
        return "\n" + "\n".join(f"§EXCL {c}§" for c in e.groups() if c) + "\n"
    if "виключено" in note and len(note) < 400 and note.startswith("{Пункт виключено"):
        return "\n§EXCL§\n"
    return ""


def body_lines(text: str):
    start = text.index("\n1. ЗАГАЛЬНІ ПОЛОЖЕННЯ")
    end = text.index("Додаток 1\nдо Правил дорожнього руху")
    body = text[start:end]
    # remove editorial notes (they never nest; may span lines)
    prev = None
    while prev != body:
        prev = body
        body = NOTE_RE.sub(_note, body)
    # tables: page text puts each cell on its own line, cells split by a lone TAB line,
    # rows split by several blank lines → one line per row, cells joined by " ¦ "
    body = re.sub(r"[ \u00a0]*\n+\t[ \t]*\n+", " ¦ ", body)
    body = re.sub(r"¦ (?=¦)", "¦  ", body)
    return [clean(l) for l in body.split("\n")]


def parse(text: str):
    lines = [l for l in body_lines(text)]
    sections, sec, point, pending_heading = [], None, None, None
    group_intro = []
    special = False  # sections 33/34: signs and markings use codes without trailing dot
    for raw in lines:
        if not raw:
            continue
        m = SECTION_RE.match(raw)
        if m and raw.upper() == raw and int(m.group(1)) == (len(sections) + 1):
            sec = {"n": int(m.group(1)), "title": m.group(2).strip().capitalize().replace("'", "’"), "intro": [], "points": []}
            sections.append(sec)
            point, pending_heading = None, None
            special = sec["n"] in (33, 34)
            continue
        if sec is None:
            continue
        if "¦" in raw and len([c for c in raw.split("¦") if c.strip()]) == 1:
            raw = clean(raw.replace("¦", " "))  # a lone cell is just a line of text
        if "¦" in raw and point is not None:
            cells = [quotes(c.strip()) for c in raw.split("¦")]
            while cells and not cells[-1]:
                cells.pop()
            if cells and cells[0].startswith("Примітк"):
                # «Примітки:» are laid out as a two-column table on the site — keep them as text
                point["paras"].extend(c for c in cells if c)
            elif point["paras"] and isinstance(point["paras"][-1], dict):
                point["paras"][-1]["table"].append(cells)
            else:
                point["paras"].append({"table": [cells]})
            continue
        if raw.startswith("§EXCL ") and not special:
            code = raw[6:-1]
            if code.split(".")[0] == str(sec["n"]):
                point = {"n": code, "paras": [], "excluded": True}
                sec["points"].append(point)
            continue
        if raw == "§EXCL§":
            if point is not None and not point["paras"]:
                point["excluded"] = True
            continue
        if not special:
            m = POINT_RE.match(raw)
            if m and m.group(1).split(".")[0] == str(sec["n"]):
                point = {"n": m.group(1), "paras": []}
                if pending_heading:
                    point["heading"] = pending_heading
                    pending_heading = None
                rest = m.group(2).strip()
                if rest:
                    point["paras"].append(quotes(rest))
                sec["points"].append(point)
                continue
            # a short line without final punctuation between points = sub-heading (e.g. «Регульовані перехрестя»)
            if (len(raw) < 90 and not raw.endswith((".", ";", ":", ",")) and raw[0].isupper()
                    and not SUBITEM_RE.match(raw) and " - " not in raw):
                pending_heading = quotes(raw)
                continue
        else:
            g = GROUP_RE.match(raw)
            if g and not CODE_RE.match(raw):
                pending_heading = f"{g.group(1)}. {quotes(g.group(2))}"
                point = None
                continue
            c = CODE_RE.match(raw)
            last = sec["points"][-1]["n"] if sec["points"] else None
            if c and last and code_key(c.group(1)) < code_key(last):
                # «3.1-3.8, 3.11 не поширюються …» — a general note of the group, not a new sign
                point = {"n": last, "note": True, "codes": clean(c.group(1) + (c.group(2) or "")), "paras": [quotes(raw)]}
                sec["points"].append(point)
                continue
            if c:
                codes = c.group(1) + (c.group(2) or "")
                point = {"n": c.group(1), "codes": clean(codes), "paras": [quotes(raw)]}
                if pending_heading:
                    point["heading"] = pending_heading
                    pending_heading = None
                    if group_intro:
                        point["headingIntro"] = group_intro
                        group_intro = []
                sec["points"].append(point)
                continue
        if set(raw) <= {"_"}:
            continue
        if raw.startswith("*") and point is not None:
            point["paras"].append(quotes(raw))
            continue
        if raw.startswith("* "):
            sec.setdefault("footnote", []).append(quotes(raw[2:]))
            continue
        if point is None and special and pending_heading:
            group_intro.append(quotes(raw))
            continue
        target = point["paras"] if point is not None else sec["intro"]
        target.append(quotes(raw))
    for s in sections:
        if not s["intro"]:
            del s["intro"]
        for p in s["points"]:
            if p.get("excluded") and not p["paras"]:
                p["paras"] = ["Пункт виключено."]
    return sections


LI_RE = re.compile(r"^((?:[а-яієїґ]|\d{1,2})\))\s*(.*)$")


def to_blocks(paras):
    """Strings/tables → typed blocks the app renders: p, li, note, table."""
    out = []
    for x in paras:
        if isinstance(x, dict):
            out.append({"t": "table", "rows": x["table"]})
            continue
        m = LI_RE.match(x)
        if m:
            out.append({"t": "li", "mark": m.group(1), "text": m.group(2)})
        elif x.startswith(("Примітка", "Примітки", "*")):
            out.append({"t": "note", "text": x})
        else:
            out.append({"t": "p", "text": x})
    return out


def finish(sections):
    for s in sections:
        for key in ("intro", "footnote"):
            if key in s:
                s[key] = to_blocks(s[key])
        for p in s["points"]:
            p["blocks"] = to_blocks(p.pop("paras"))
            if "headingIntro" in p:
                p["headingIntro"] = to_blocks(p["headingIntro"])
    return sections


def check(sections):
    problems = []
    if [s["n"] for s in sections] != list(range(1, 35)):
        problems.append(f"sections: {[s['n'] for s in sections]}")
    for s in sections:
        if s["n"] in (33, 34):
            continue
        prev = None
        for p in s["points"]:
            k = code_key(p["n"])
            if prev and k <= prev:
                problems.append(f"order {s['n']}: {p['n']} after {'.'.join(map(str, prev))}")
            if prev and len(k) == 2 and len(prev) >= 2 and k[1] - prev[1] > 1:
                problems.append(f"gap {s['n']}: {'.'.join(map(str, prev))} → {p['n']}")
            prev = k
            if not p["blocks"]:
                problems.append(f"empty {p['n']}")
    return problems


def main():
    src, dst = sys.argv[1], sys.argv[2]
    text = open(src, encoding="utf-8").read()
    sections = finish(parse(text))
    probs = check(sections)
    data = {
        "source": "Правила дорожнього руху, затв. постановою КМУ від 10.10.2001 № 1306",
        "sourceUrl": "https://zakon.rada.gov.ua/laws/show/1306-2001-п",
        "edition": "зі змінами згідно з постановою КМУ № 1238 від 07.10.2026",
        "sections": sections,
    }
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    n_points = sum(len(s["points"]) for s in sections)
    chars = sum(len(b.get("text", "")) + len(str(b.get("rows", ""))) for s in sections for pt in s["points"] for b in pt["blocks"])
    print(f"sections={len(sections)} points={n_points} chars={chars}")
    for s in sections:
        print(f"  {s['n']:>2} {len(s['points']):>3}  {s['title']}")
    for p in probs:
        print("CHECK:", p)


if __name__ == "__main__":
    main()
