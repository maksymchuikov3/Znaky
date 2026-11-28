package com.znaky.feature.home

import android.content.Context
import androidx.compose.runtime.Immutable
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * «Мій підручник»: the study path read like a ПДР book, chapter after chapter.
 * A chapter is read first ([read]), then its questions are answered ([completion]).
 */
@Immutable
data class BookChapter(
    /** What to open to read it: «theory:pdr-8», «course:moto:gear», or null when there is nothing to read. */
    val readTarget: String?,
    val num: String,
    val title: String,
    val part: String,
    val read: Boolean,
    /** Question topic practised after reading, if any. */
    val topicId: String?,
    val questions: Int,
    val completion: Float,
) {
    val hasQuestions: Boolean get() = topicId != null && questions > 0
    val questionsDone: Boolean get() = !hasQuestions || completion >= LEARNED
    val done: Boolean get() = (read || readTarget == null) && questionsDone
}

/** A chapter before progress is applied. */
internal data class ChapterSpec(val readTarget: String?, val num: String, val title: String, val part: String, val topicId: String? = null)

private val json = Json { ignoreUnknownKeys = true }

/** Official ПДР sections (Постанова КМУ № 1306), the fallback contents when no theory file is bundled. */
private val PdrSections = listOf(
    "Загальні положення", "Обов'язки і права водіїв механічних транспортних засобів",
    "Рух транспортних засобів із спеціальними сигналами", "Обов'язки і права пішоходів", "Обов'язки і права пасажирів",
    "Вимоги до велосипедистів", "Вимоги до осіб, які керують гужовим транспортом, і погоничам тварин",
    "Регулювання дорожнього руху", "Попереджувальні сигнали", "Початок руху та зміна його напрямку",
    "Розташування транспортних засобів на дорозі", "Швидкість руху", "Дистанція, інтервал, зустрічний роз'їзд", "Обгін",
    "Зупинка і стоянка", "Проїзд перехресть", "Переваги маршрутних транспортних засобів",
    "Проїзд пішохідних переходів і зупинок транспортних засобів", "Користування зовнішніми світловими приладами",
    "Рух через залізничні переїзди", "Перевезення пасажирів", "Перевезення вантажу",
    "Буксирування та експлуатація транспортних составів", "Навчальна їзда", "Рух транспортних засобів у колонах",
    "Рух у житловій та пішохідній зоні", "Рух по автомагістралях і дорогах для автомобілів",
    "Рух по гірських дорогах і на крутих спусках", "Міжнародний рух", "Номерні, розпізнавальні знаки, написи і позначення",
    "Технічний стан транспортних засобів та їх обладнання", "Окремі питання дорожнього руху, що вимагають узгодження",
    "Дорожні знаки", "Дорожня розмітка",
)

internal const val PART_RULES = "Правила дорожнього руху"

/**
 * The contents of the book for [category]. The road book comes from the bundled theory file
 * (assets/theory/theory.json) when there is one, otherwise from the ПДР section list; moto learners
 * get the motorcycle course appended, boat learners read only the boat course.
 */
internal fun loadBook(context: Context, category: LicenseCategory): List<ChapterSpec> {
    if (category.isWater) return courseChapters(context, "boat", "Судноводій")
    val road = theoryChapters(context) ?: PdrSections.mapIndexed { i, t ->
        // No bundled texts yet: nothing to read, the chapter is its questions.
        ChapterSpec(null, "${i + 1}", t, PART_RULES)
    }
    return if (category.isMoto) road + courseChapters(context, "moto", "Мотоцикл") else road
}

private fun theoryChapters(context: Context): List<ChapterSpec>? = runCatching {
    val root = context.assets.open("theory/theory.json").bufferedReader().use { json.parseToJsonElement(it.readText()) }.jsonObject
    root.getValue("groups").jsonArray.flatMap { g ->
        val group = g.jsonObject
        val part = group.text("title") ?: group.text("name") ?: ""
        group.getValue("sections").jsonArray.map { s ->
            val sec = s.jsonObject
            val id = sec.text("id").orEmpty()
            ChapterSpec("theory:$id", sec.text("num").orEmpty(), sec.text("title").orEmpty(), part)
        }
    }.takeIf { it.isNotEmpty() }
}.getOrNull()

private fun courseChapters(context: Context, course: String, part: String): List<ChapterSpec> = runCatching {
    val root = context.assets.open("courses/$course.json").bufferedReader().use { json.parseToJsonElement(it.readText()) }.jsonObject
    root.getValue("chapters").jsonArray.mapIndexed { i, c ->
        val ch = c.jsonObject
        ChapterSpec("course:$course:${ch.text("id")}", "${i + 1}", ch.text("title").orEmpty(), part, ch.text("topicId"))
    }
}.getOrDefault(emptyList())

private fun JsonObject.text(key: String): String? = this[key]?.jsonPrimitive?.content

/** Chapters already read: theory sections (shared with the theory screens) and course chapters. */
internal fun readChapters(context: Context): Set<String> {
    val theory = context.getSharedPreferences("theory_read", Context.MODE_PRIVATE).getStringSet("read", emptySet()).orEmpty()
    val courses = context.getSharedPreferences("course_progress", Context.MODE_PRIVATE)
    val moto = courses.getStringSet("moto", emptySet()).orEmpty().map { "course:moto:$it" }
    val boat = courses.getStringSet("boat", emptySet()).orEmpty().map { "course:boat:$it" }
    return theory.map { "theory:$it" }.toSet() + moto + boat
}

/**
 * Applies progress to [specs]. Road chapters are matched to question topics by section number
 * («pdr-12» ↔ topic number «12»); the safety, vehicle and first-aid topics, which have no ПДР number,
 * become their own chapters at the end, as the last parts of the book.
 */
internal fun buildBook(specs: List<ChapterSpec>, route: List<PlanTopic>, read: Set<String>, category: LicenseCategory): List<BookChapter> {
    val byId = route.associateBy { it.id }
    val byNumber = route.filter { it.number.toIntOrNull() != null }.associateBy { it.number }
    val chapters = specs.map { s ->
        val topic = s.topicId?.let(byId::get) ?: if (s.part == PART_RULES || s.readTarget?.startsWith("theory:pdr-") == true) byNumber[s.num] else null
        BookChapter(s.readTarget, s.num, s.title, s.part, s.readTarget in read, topic?.id, topic?.questions ?: 0, topic?.completion ?: 0f)
    }
    if (category.isWater) return chapters
    val used = chapters.mapNotNull { it.topicId }.toMutableSet()
    val result = chapters.toMutableList()
    val extra = ArrayList<BookChapter>()
    route.filter { it.id !in used && it.number.toIntOrNull() == null && !it.id.startsWith("mo-") && it.block != ExamBlock.TRAFFIC_RULES }
        .forEach { topic ->
            // A theory group for the block (safe-…, car-…, aid-…) takes the questions on its last chapter.
            val prefix = "theory:${topic.block.theoryPrefix()}-"
            val at = result.indexOfLast { it.readTarget?.startsWith(prefix) == true && it.topicId == null }
            if (at >= 0) {
                result[at] = result[at].copy(topicId = topic.id, questions = topic.questions, completion = topic.completion)
            } else {
                extra += BookChapter(null, topic.number, topic.title, topic.block.label(), true, topic.id, topic.questions, topic.completion)
            }
        }
    return result + extra
}

private fun ExamBlock.theoryPrefix() = when (this) {
    ExamBlock.TRAFFIC_RULES -> "pdr"
    ExamBlock.ROAD_SAFETY -> "safe"
    ExamBlock.VEHICLE -> "car"
    ExamBlock.FIRST_AID -> "aid"
}
