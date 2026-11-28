#!/usr/bin/env python3
"""Parse the official rules for small vessels into the same JSON the app reads for ПДР.

Input: the .docx of «Типова форма місцевих правил плавання для малих, спортивних суден
і водних мотоциклів та використання засобів для розваг на воді» (наказ Мінінфраструктури
№ 493 від 07.07.2022, зареєстровано в Мін'юсті 12.08.2022 за № 913/38249), as published on
mininfra.gov.ua. Output: sections I–VII split into numbered points; unnumbered lines that
follow a point ending with «:» become list items.

Local rules of each oblast are made from this form, so blanks for the oblast name
(«________» with «(назва області …)» under them) are replaced by a neutral wording.

    python3 tools/content/boat_parse.py typova-forma-493.docx feature/learn/src/main/assets/pdr/boat.json
"""
import json
import re
import sys
import zipfile

ROMAN = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "VIII": 8}
SECTION_RE = re.compile(r"^([IVXІ]+)\.\s+(.+)$")
POINT_RE = re.compile(r"^(\d{1,2})\.\s+(.*)$")
ANNOT_RE = re.compile(r"^\((назва|найменування|посилання)[^)]*\)$")


def paragraphs(path):
    x = zipfile.ZipFile(path).read("word/document.xml").decode("utf-8")
    out = []
    for p in re.findall(r"<w:p[ >].*?</w:p>", x, re.S):
        t = "".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", p))
        for a, b in (("&quot;", '"'), ("&lt;", "<"), ("&gt;", ">"), ("&apos;", "'"), ("&amp;", "&")):
            t = t.replace(a, b)
        out.append(re.sub(r"\s+", " ", t.replace(" ", " ")).strip())
    return out


def tidy(s: str) -> str:
    s = re.sub(r"\s*\((?:посилання|назва|найменування)[^)]*\)", "", s)
    s = re.sub(r"вебсайті\s*_{3,}", "вебсайті місцевої державної адміністрації", s)
    s = re.sub(r"У межах\s*_{3,}\s*", "У межах області ", s)
    s = re.sub(r"\s*(?:в|у)\s+_{3,}", " у вашій області", s)
    s = re.sub(r"_{3,}", "…", s)
    s = s.replace("“", "«").replace("”", "»").replace("'", "’")
    out, open_ = [], True
    for ch in s:
        if ch == '"':
            out.append("«" if open_ else "»")
            open_ = not open_
        else:
            out.append(ch)
    return re.sub(r"\s+([,.;:])", r"\1", "".join(out)).strip()


def roman(s: str) -> int:
    return ROMAN[s.replace("І", "I")]


def parse(paras):
    sections, sec, point, listing = [], None, None, False
    for raw in paras:
        if not raw or ANNOT_RE.match(raw):
            continue
        m = SECTION_RE.match(raw)
        if m and set(m.group(1)) <= set("IVXІ"):
            title = tidy(m.group(2)).rstrip(".").replace(" у вашій області", " у вашій області")
            sec = {"n": roman(m.group(1)), "title": title, "intro": [], "points": []}
            sections.append(sec)
            point, listing = None, False
            continue
        if sec is None:
            continue
        if raw.startswith("Начальник ") or raw.startswith("Директор "):
            break
        m = POINT_RE.match(raw)
        if m:
            text = tidy(m.group(2))
            point = {"n": m.group(1), "blocks": [{"t": "p", "text": text}]}
            sec["points"].append(point)
            listing = text.endswith(":")
            continue
        text = tidy(raw)
        target = point["blocks"] if point else sec["intro"]
        if target and not target[-1]["text"].endswith((".", ";", ":")):
            # the document breaks a sentence around a blank for the oblast name: join it back
            target[-1]["text"] = tidy(target[-1]["text"] + " " + text)
            continue
        if listing and (text.endswith((";", ".", ":")) or text[:1].islower()):
            target.append({"t": "li", "mark": "–", "text": text})
            if text.endswith(":"):
                listing = True
        elif text.startswith("Примітк"):
            target.append({"t": "note", "text": text})
        else:
            target.append({"t": "p", "text": text})
            listing = text.endswith(":")
    for s in sections:
        if not s["intro"]:
            del s["intro"]
    return sections


def main():
    src, dst = sys.argv[1], sys.argv[2]
    sections = parse(paragraphs(src))
    nums = [s["n"] for s in sections]
    assert nums == list(range(1, len(nums) + 1)), nums
    for s in sections:
        got = [int(p["n"]) for p in s["points"]]
        assert got == list(range(1, len(got) + 1)), (s["n"], got)
    data = {
        "title": "Правила плавання малих суден",
        "subtitle": "Малі, спортивні судна і водні мотоцикли: повний текст по розділах і пунктах",
        "source": "Типова форма місцевих правил плавання для малих, спортивних суден і водних мотоциклів "
                  "та використання засобів для розваг на воді, затв. наказом Мінінфраструктури від 07.07.2022 № 493",
        "sourceUrl": "https://zakon.rada.gov.ua/laws/show/z0913-22",
        "edition": "зареєстровано в Мін’юсті 12.08.2022 за № 913/38249; місцеві правила області складаються за цією формою",
        "romanSections": True,
        "sections": sections,
    }
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    for s in sections:
        print(f"  {s['n']} {len(s['points']):>3}  {s['title']}")


if __name__ == "__main__":
    main()
