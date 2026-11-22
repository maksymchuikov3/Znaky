package com.znaky.core.domain

import com.znaky.core.domain.exam.ExamRules
import com.znaky.core.domain.exam.ExamSession
import com.znaky.core.domain.exam.TicketGenerator
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class StartExamUseCase(
    private val questions: QuestionRepository,
    private val settings: SettingsRepository,
    private val rules: ExamRules = ExamRules.Default,
    private val generator: TicketGenerator = TicketGenerator(rules),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(): ExamSession {
        val pool = questions.getQuestionPool(settings.licenseCategory.first())
        return ExamSession.start(rules, generator.generate(pool), clock())
    }
}
