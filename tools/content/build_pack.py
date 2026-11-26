"""Merges the extra courses (boat, moto) into core/data/src/main/assets/content/questions.json.

Run from the repo root: python3 tools/content/build_pack.py
Topics and questions with the «bt-»/«mo-» prefixes are replaced; everything else is kept as is.
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).parent))
import boat_questions  # noqa: E402

PACK = pathlib.Path("core/data/src/main/assets/content/questions.json")
EXTRA = [("bt", boat_questions, ["BOAT"])]
try:
    import moto_questions  # noqa: E402

    EXTRA.append(("mo", moto_questions, ["A", "A1"]))
except ImportError:
    pass

pack = json.loads(PACK.read_text())
prefixes = tuple(f"{p}-" for p, _, _ in EXTRA)
pack["topics"] = [t for t in pack["topics"] if not t["id"].startswith(prefixes)]
pack["questions"] = [q for q in pack["questions"] if not q["topicId"].startswith(prefixes)]
for prefix, mod, cats in EXTRA:
    pack["rules"].update(mod.RULES)
    pack["topics"] += [{"id": i, "number": n, "title": t, "block": b} for i, n, t, b in mod.TOPICS]
    for k, (topic, text, answers, correct, refs, expl, *rest) in enumerate(mod.Q, start=1):
        q = {
            "id": f"{prefix}-q-{k:03d}",
            "topicId": topic,
            "text": text,
            "answers": [{"id": "abcdef"[i], "text": a} for i, a in enumerate(answers)],
            "correctAnswerId": "abcdef"[correct],
            "ruleRefs": refs,
            "explanation": expl,
            "categories": rest[0] if rest else cats,
        }
        pack["questions"].append(q)
pack["version"] = max(pack["version"], 3)
PACK.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n")
print(len(pack["topics"]), "topics,", len(pack["questions"]), "questions")
