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
