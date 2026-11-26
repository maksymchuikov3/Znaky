#!/usr/bin/env python3
"""Installs the APK on a running emulator, opens the main screens and fails on any crash.

Usage: smoke_test.py <apk> <package>
Catches problems unit tests can't see, such as R8 removing a class the app needs.
"""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

APK, PACKAGE = sys.argv[1], sys.argv[2]


def adb(*args, check=True):
    out = subprocess.run(["adb", *args], capture_output=True, text=True)
    if check and out.returncode != 0:
        raise SystemExit(f"adb {' '.join(args)} failed: {out.stderr}")
    return out.stdout


def alive():
    return adb("shell", "pidof", PACKAGE, check=False).strip() != ""


def crash_log():
    log = adb("logcat", "-d", "-b", "crash", check=False)
    return log if PACKAGE in log else ""


def screen_size():
    out = adb("shell", "wm", "size", check=False)
    m = re.findall(r"(\d+)x(\d+)", out)
    return tuple(map(int, m[-1])) if m else (1080, 1920)


def tap_text(text, last=False):
    """Taps the first on-screen element whose text or description equals `text`.

    With `last`, taps the last match instead: tab bar labels come after a screen's
    own headings with the same words (for example "Вчити").
    """
    root = ET.Element("hierarchy")
    for _ in range(10):
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False)
        xml = adb("shell", "cat", "/sdcard/ui.xml", check=False)
        try:
            root = ET.fromstring(xml[xml.find("<?xml"):] if "<?xml" in xml else xml)
        except ET.ParseError:
            time.sleep(1)
            continue
        matches = [n for n in root.iter("node") if text in (n.get("text"), n.get("content-desc"))]
        if matches:
            node = matches[-1] if last else matches[0]
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
            adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
            time.sleep(2)
            return
        # Not visible yet: the target may be further down a scrolling screen.
        w, h = screen_size()
        adb("shell", "input", "swipe", str(w // 2), str(h * 3 // 4), str(w // 2), str(h * 2 // 5), "400", check=False)
        time.sleep(1)
    seen = [n.get("text") or n.get("content-desc") for n in root.iter("node") if n.get("text") or n.get("content-desc")]
    raise SystemExit(f"'{text}' not found on screen. Visible: {seen}")


def check(step):
    log = crash_log()
    if log or not alive():
        print(log)
        raise SystemExit(f"App crashed after: {step}")
    print(f"ok: {step}")


adb("install", "-r", APK)
adb("logcat", "-c", check=False)
adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/com.znaky.app.MainActivity")
time.sleep(6)
check("launch")
DEFAULT_STEPS = [
    # First launch: onboarding (welcome, category, exam date, reminder).
    "Почати", "Далі", "Далі", "Не зараз",
    "Поїхали", "BACK",
    "Іспит як у ТСЦ", "BACK",
    "TAB:Вчити", "Будова й експлуатація авто", "Електро", "Мото", "Човен", "BACK",
    "Регулювальник", "BACK",
    "Словник", "Терміни Правил", "BACK", "BACK",
    "TAB:Тренування", "20 випадкових", "BACK",
    "Повторення", "BACK",
    "Закладки", "BACK",
    "За темами", "BACK",
    "TAB:Прогрес",
    "TAB:Тренування", "Пробний іспит", "Почати іспит",
]
for step in sys.argv[3:] or DEFAULT_STEPS:
    if step == "BACK":
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(2)
    elif step.startswith("TAB:"):
        tap_text(step[4:], last=True)
    else:
        tap_text(step)
    check(step)
print("Smoke test passed")
