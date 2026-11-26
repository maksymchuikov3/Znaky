package com.znaky.core.model

/**
 * What the learner is preparing for. Driving licence categories in Ukraine, plus [BOAT]:
 * the boat master's certificate (малі судна і водні мотоцикли), which is not a driving
 * category but uses the same study flow. Questions can apply to several of them.
 */
enum class LicenseCategory {
    A1, A, B1, B, C1, C, D1, D, BE, C1E, CE, D1E, DE, T,

    /** Судноводій малого судна / водного мотоцикла (міжнародне посвідчення на прогулянкове судно). */
    BOAT,
    ;

    val isMoto: Boolean get() = this == A || this == A1
    val isWater: Boolean get() = this == BOAT
}
