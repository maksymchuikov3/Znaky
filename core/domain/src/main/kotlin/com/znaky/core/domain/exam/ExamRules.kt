package com.znaky.core.domain.exam

import com.znaky.core.model.ExamBlock

/**
 * Format of the theory exam. Kept as data so a rule change at the service centre
 * means changing one value here, not hunting through the UI.
 */
data class ExamRules(
    val questionCount: Int,
    val durationSeconds: Int,
    /** Highest number of mistakes that still passes. One more ends the exam. */
    val maxMistakes: Int,
    /** How many questions each block contributes to a ticket. */
    val blockDistribution: Map<ExamBlock, Int>,
) {
    init {
        require(blockDistribution.values.sum() == questionCount) {
            "Block distribution must add up to $questionCount questions"
        }
    }

    val minCorrectToPass: Int get() = questionCount - maxMistakes

    companion object {
        /** Theory exam at a service centre of the MIA: 20 questions, 20 minutes, up to 2 mistakes. */
        val Default = ExamRules(
            questionCount = 20,
            durationSeconds = 20 * 60,
            maxMistakes = 2,
            blockDistribution = mapOf(
                ExamBlock.TRAFFIC_RULES to 10,
                ExamBlock.ROAD_SAFETY to 4,
                ExamBlock.VEHICLE to 4,
                ExamBlock.FIRST_AID to 2,
            ),
        )
    }
}
