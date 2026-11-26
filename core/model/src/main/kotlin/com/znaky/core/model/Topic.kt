package com.znaky.core.model

/**
 * A study topic, usually a section of the road rules (e.g. "12" for speed).
 * [number] is what the user sees; [id] is a stable key used for storage.
 */
data class Topic(
    val id: String,
    val number: String,
    val title: String,
    val block: ExamBlock,
    val questionCount: Int = 0,
)

/**
 * Whether learners of [category] study this topic. Topic ids carry their audience:
 * «mo-…» are the extra motorcycle topics (A, A1), «bt-…» the boat topics (BOAT),
 * every other topic belongs to the road programme that all driving categories share.
 */
fun Topic.isFor(category: LicenseCategory): Boolean = when {
    id.startsWith(BOAT_TOPIC_PREFIX) -> category.isWater
    id.startsWith(MOTO_TOPIC_PREFIX) -> category.isMoto
    else -> !category.isWater
}

const val MOTO_TOPIC_PREFIX = "mo-"
const val BOAT_TOPIC_PREFIX = "bt-"
