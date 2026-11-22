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


def tap_text(text):
    """Taps the first on-screen element whose text or description equals `text`."""
    for _ in range(10):
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False)
        xml = adb("shell", "cat", "/sdcard/ui.xml", check=False)
        try:
            root = ET.fromstring(xml[xml.find("<?xml"):] if "<?xml" in xml else xml)
        except ET.ParseError:
            time.sleep(1)
            continue
        for node in root.iter("node"):
            if text in (node.get("text"), node.get("content-desc")):
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
                adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
                time.sleep(2)
                return
        time.sleep(1)
    raise SystemExit(f"'{text}' not found on screen")


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
    "Знаки", "BACK",
    "Пояснення", "BACK",
    "3D-машина", "Рентген", "Схема", "Електромобіль", "Гальма і ABS", "Далі", "Далі", "BACK", "BACK",
    "Тести", "Прогрес", "Іспит", "Почати іспит",
]
for step in sys.argv[3:] or DEFAULT_STEPS:
    if step == "BACK":
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(2)
    else:
        tap_text(step)
    check(step)
print("Smoke test passed")
