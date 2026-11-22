package com.znaky.core.model

/**
 * The four blocks the theory exam ticket is assembled from.
 * The exam engine (core:domain) decides how many questions each block contributes.
 */
enum class ExamBlock {
    /** Правила дорожнього руху. */
    TRAFFIC_RULES,

    /** Безпека руху. */
    ROAD_SAFETY,

    /** Будова й експлуатація транспортного засобу. */
    VEHICLE,

    /** Домедична допомога. */
    FIRST_AID,
}
