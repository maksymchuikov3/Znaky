package com.znaky.feature.learn

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class SignGroup(
    val number: String,
    val name: String,
    val color: Color,
    val note: String,
    /** A typical sign of the group, shown on its card. */
    val sample: String,
)

@Immutable
internal data class RoadSignInfo(
    val code: String,
    val name: String,
    val description: String,
    val ruleRef: String = "33",
) {
    val group: String get() = code.substringBefore('.')
}

/**
 * All road signs of section 33 of the Rules (ДСТУ 4100 shapes, drawn in core:art).
 * Names follow the Rules; descriptions are our own short wording, see [allSigns].
 */
internal object SignCatalog {
    val groups = listOf(
        SignGroup("1", "Попереджувальні", Color(0xFFC9822B), "Попереджають про небезпечну ділянку, де треба знизити швидкість і бути уважним. Поза населеним пунктом стоять за 150–300 м, у ньому — за 50–100 м до небезпеки.", "1.1"),
        SignGroup("2", "Пріоритету", Color(0xFF3B6EA5), "Встановлюють черговість проїзду перехресть і вузьких ділянок дороги.", "2.1"),
        SignGroup("3", "Заборонні", Color(0xFFC8514A), "Запроваджують або скасовують обмеження. Більшість діє від знака до найближчого перехрестя, а в населеному пункті без перехресть — до його кінця.", "3.29"),
        SignGroup("4", "Наказові", Color(0xFF3F74C4), "Наказують, куди можна їхати і хто може рухатися ділянкою. На маршрутний транспорт знаки 4.1–4.6 не діють.", "4.1"),
        SignGroup("5", "Інформаційно-вказівні", Color(0xFF3E8E63), "Запроваджують або скасовують режим руху (автомагістраль, житлова зона, односторонній рух) та інформують про населені пункти й об’єкти.", "5.42.1"),
        SignGroup("6", "Сервісу", Color(0xFF5B7DB8), "Підказують, де поруч заправка, СТО, лікарня, кафе чи місце відпочинку.", "6.7.1"),
        SignGroup("7", "Таблички", Color(0xFF6E7681), "Ставляться під знаком і уточнюють його: відстань, зону дії, час, вид транспорту.", "7.1.1"),
    )

    /** All signs in the order of the Rules (1.1, 1.2 … 1.10 …), not in source order. */
    val signs: List<RoadSignInfo> = allSigns.sortedWith(
        compareBy({ it.code.split('.')[0].toInt() }, { it.code.split('.')[1].toInt() }, { it.code.split('.').getOrNull(2)?.toInt() ?: 0 }),
    )

    private val byGroup = signs.groupBy { it.group }

    fun inGroup(group: String): List<RoadSignInfo> = byGroup[group].orEmpty()

    fun byCode(code: String) = signs.firstOrNull { it.code == code }
}
