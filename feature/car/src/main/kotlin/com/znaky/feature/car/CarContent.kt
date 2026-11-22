package com.znaky.feature.car

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal enum class SceneKind { BRAKES, ABS, STOPPING, REGEN, CHARGING, HIGH_VOLTAGE }

@Immutable
internal data class TopicStep(
    val title: String,
    val text: String,
    val scene: SceneKind,
    val note: String? = null,
    val warning: String? = null,
    /** Two coloured facts shown under the text, as in the regen card. */
    val facts: List<Pair<String, String>> = emptyList(),
)

@Immutable
internal data class CarTopic(
    val id: String,
    val title: String,
    val group: String,
    val accent: Color,
    val steps: List<TopicStep>,
    /** Exam topic to practise after reading, if any. */
    val examTopicId: String? = "vh-01",
)

@Immutable
internal data class TopicEntry(val title: String, val topicId: String?, val meta: String, val step: Int = 0)

@Immutable
internal data class TopicGroup(val name: String, val color: Color, val entries: List<TopicEntry>)

private val Red = Color(0xFFC93A3F)
private val Green = Color(0xFF1E7A3A)

internal val carTopics: Map<String, CarTopic> = listOf(
    CarTopic(
        id = "brakes", title = "Гальма і ABS", group = "ГАЛЬМА І ABS", accent = Red,
        steps = listOf(
            TopicStep(
                "Що відбувається, коли ви тиснете на педаль",
                "Педаль тисне на поршень, і гальмівна рідина передає зусилля до кожного колеса. Там колодки стискають диск, тертя перетворює рух на тепло, і авто сповільнюється.",
                SceneKind.BRAKES,
                warning = "Педаль м’яка або провалюється: далі не їдьте. Це несправність, з якою рух заборонено.",
            ),
            TopicStep(
                "ABS не дає колесам заблокуватись",
                "Коли колесо от-от піде в юз, ABS на мить послаблює тиск і знову підтискає колодки, багато разів за секунду. Колесо продовжує обертатись, і ви можете кермувати під час гальмування.",
                SceneKind.ABS,
                note = "Гальмуєте екстрено: тисніть педаль сильно й не відпускайте. Вібрація і тріск під ногою означають, що ABS працює.",
            ),
            TopicStep(
                "Гальмівний шлях залежить від дороги",
                "На мокрому асфальті авто зупиняється помітно довше, ніж на сухому, а на льоду шлях зростає в рази. ABS допомагає зберегти керованість, але не скорочує шлях на льоду.",
                SceneKind.STOPPING,
                note = "Тримайте більшу дистанцію в дощ і мороз.",
            ),
        ),
    ),
    CarTopic(
        id = "regen", title = "Електромотор і рекуперація", group = "ЕЛЕКТРОМОБІЛЬ", accent = Green,
        steps = listOf(
            TopicStep(
                "Рекуперація: мотор стає генератором",
                "Відпустіть педаль «газу», і мотор почне працювати як генератор: він сповільнює колеса та повертає частину енергії в батарею.",
                SceneKind.REGEN,
                note = "На слизькій дорозі сильна рекуперація може зірвати ведучі колеса в ковзання. Узимку обирайте слабший рівень.",
                facts = listOf("РОЗГІН" to "Батарея → мотор", "ВІДПУСКАННЯ ПЕДАЛІ" to "Мотор → батарея"),
            ),
            TopicStep(
                "Зарядка: повільна і швидка",
                "Удома та на звичайних станціях заряджають змінним струмом через роз’єм Type 2, на це йде кілька годин. Швидкі станції дають постійний струм через CCS2: від 20 до 80% приблизно за пів години.",
                SceneKind.CHARGING,
                facts = listOf("AC · TYPE 2" to "Удома, 4–8 год", "DC · CCS2" to "Траса, ~30 хв"),
            ),
            TopicStep(
                "Висока напруга",
                "Помаранчеві кабелі й батарея працюють під високою напругою. Обслуговувати їх може лише фахівець.",
                SceneKind.HIGH_VOLTAGE,
                warning = "Після ДТП не торкайтесь помаранчевих кабелів і пошкодженої батареї. Скажіть рятувальникам, що це електромобіль.",
            ),
        ),
    ),
).associateBy { it.id }

internal fun topicGroups(kind: CarKind): List<TopicGroup> {
    val brakes = TopicEntry("Гальма і ABS", "brakes", "3 кроки")
    val safety = TopicGroup(
        "КЕРУВАННЯ І БЕЗПЕКА", Red,
        listOf(brakes, TopicEntry("Кермо", null, "Скоро"), TopicEntry("Підвіска і шини", null, "Скоро")),
    )
    return when (kind) {
        CarKind.ENGINE -> listOf(
            TopicGroup(
                "ДВИГУН", EngineColor,
                listOf(TopicEntry("Як працює двигун", null, "Скоро"), TopicEntry("Охолодження", null, "Скоро"), TopicEntry("Пальне і вихлоп", null, "Скоро")),
            ),
            TopicGroup("ТРАНСМІСІЯ", GearboxColor, listOf(TopicEntry("Коробка передач", null, "Скоро"), TopicEntry("Диференціал", null, "Скоро"))),
            safety,
        )
        CarKind.ELECTRIC -> listOf(
            TopicGroup(
                "ЕЛЕКТРОМОБІЛЬ", Green,
                listOf(
                    TopicEntry("Електромотор і рекуперація", "regen", "3 кроки"),
                    TopicEntry("Зарядка: роз’єми і швидкість", "regen", "1 крок", step = 1),
                    TopicEntry("Батарея і запас ходу", null, "Скоро"),
                ),
            ),
            safety,
        )
    }
}
