# Архітектура Android-застосунку «Знаки»

## Стек
- **Kotlin 2.2**, **Jetpack Compose** + Material 3
- **Hilt** — DI; **Room** — офлайн-база питань і прогресу; **DataStore** — налаштування
- **Navigation Compose** з типобезпечними маршрутами (`@Serializable`)
- **kotlinx.serialization** — пакет контенту в JSON
- Gradle: version catalog (`gradle/libs.versions.toml`) + convention-плагіни в `build-logic/`
- minSdk 26 (Android 8.0), target/compile SDK 36

## Модулі

```
app                      — Activity, NavHost, Application (Hilt)
feature/home             — головна: готовність, меню
feature/practice         — теми, тренування за темою, повторення (Leitner)
feature/exam             — пробний іспит 20/20/2 і розбір
feature/stats            — статистика і готовність
core/designsystem        — тема, кольори, спільні Compose-компоненти (питання, відповіді)
core/data                — репозиторії, завантаження/валідація контенту, Hilt-біндинги
core/database            — Room: сутності, DAO
core/domain   (JVM)      — правила іспиту, генератор білетів, ExamSession, Leitner, готовність
core/model    (JVM)      — моделі: Question, Topic, ExamResult, ...
```

Залежності йдуть лише вниз: `feature → domain/model/designsystem`, `data → database/domain`,
`app → усе`. Feature-модулі не знають про Room, тож базу чи бекенд можна замінити без змін у UI.

`core/domain` і `core/model` — чисті Kotlin-модулі без Android: уся логіка іспиту
покривається швидкими JVM-тестами (`./gradlew :core:domain:test`).

## Ключові рішення
- **Правила іспиту як дані** (`ExamRules.Default`): 20 питань, 20 хв, до 2 помилок, розподіл
  10/4/4/2; третя помилка завершує іспит; відповідь не можна змінити. Зміна формату в ТСЦ
  = зміна одного об'єкта.
- **Незмінний `ExamSession`**: кожна дія повертає новий стан → легко тестувати і зберігати у `StateFlow`.
- **Офлайн-first**: контент в assets → Room; версія контенту в таблиці `content_meta`.
- **Інтервальне повторення**: кожна відповідь оновлює картку Leitner (5 скриньок, 0/1/3/7/21 днів).
- **Готовність**: 60 % — результати останніх іспитів, 40 % — покриття питань; «Готові» лише
  після 3 поспіль складених іспитів.

## Команди
```bash
./gradlew test            # юніт-тести всіх модулів
./gradlew assembleDebug   # APK: app/build/outputs/apk/debug/
./gradlew lintDebug
```

## CI
`.github/workflows/android.yml`: тести → debug APK (артефакт) → lint на кожен PR.

## Наступні кроки інфраструктури
- Підпис релізу з GitHub Secrets і завантаження AAB у Google Play (internal track) через Gradle Play Publisher або fastlane.
- Crashlytics/Sentry і продуктова аналітика (без персональних даних).
- Google Play Billing для Premium.
- Detekt/ktlint, Compose screenshot-тести, baseline profiles.
- Скрипт імпорту офіційної бази питань (див. `content/README.md`).
