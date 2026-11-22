# Знаки — тренажер ПДР для Android

Офлайн-застосунок для підготовки до теоретичного іспиту з ПДР у сервісних центрах МВС:
тренування за темами, пробний іспит у форматі ТСЦ (20 питань / 20 хвилин / до 2 помилок),
інтервальне повторення помилок і показник готовності.

> Незалежний навчальний проєкт. Не є офіційним продуктом МВС чи ГСЦ МВС.

- [Аналіз конкурентів](docs/competitor-analysis.md)
- [Архітектура](docs/architecture.md)
- [Формат контенту](content/README.md)

## Швидкий старт
Потрібні JDK 17 і Android SDK (Android Studio Narwhal або новіша).

```bash
./gradlew assembleDebug
./gradlew test
```

## Releases

Every push to `main` builds a release APK and publishes it on the
[Releases](../../releases) page as `v0.1.<run>`. Pushing a tag such as `v0.2.0`
publishes that exact version, and "Run workflow" on the Actions tab does it by hand.
Workflow: `.github/workflows/release.yml`.

Signing uses these repository secrets: `ZNAKY_KEYSTORE_BASE64` (base64 of the `.jks`),
`ZNAKY_KEYSTORE_PASSWORD`, `ZNAKY_KEY_ALIAS` and `ZNAKY_KEY_PASSWORD`. Until they are set,
the APK is signed with the debug key. It installs fine, but you can't upgrade from it to a
properly signed build without uninstalling first.
