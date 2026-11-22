package com.znaky.core.data.repository

import com.znaky.core.data.content.ContentPackLoader
import com.znaky.core.data.content.ContentPackValidator
import com.znaky.core.database.dao.ContentDao
import com.znaky.core.database.entity.ContentMetaEntity
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.LicenseCategory
import com.znaky.core.model.Question
import com.znaky.core.model.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Questions live in Room so the app works fully offline. Content ships in assets. */
@Singleton
class OfflineQuestionRepository @Inject constructor(
    private val dao: ContentDao,
    private val loader: ContentPackLoader,
) : QuestionRepository {
    private val loadMutex = Mutex()

    @Volatile
    private var contentReady = false

    override fun observeTopics(): Flow<List<Topic>> =
        dao.observeTopics().map { rows -> rows.map { it.toModel() } }

    override suspend fun getTopic(topicId: String): Topic? = dao.getTopic(topicId)?.toModel()

    override suspend fun getQuestionsForTopic(topicId: String): List<Question> =
        dao.getQuestionsForTopic(topicId).map { it.toModel() }

    override suspend fun getQuestions(ids: Collection<String>): List<Question> {
        val byId = dao.getQuestions(ids).associateBy { it.id }
        return ids.mapNotNull { byId[it]?.toModel() }
    }

    override suspend fun getQuestionPool(category: LicenseCategory): List<Question> =
        dao.getQuestionsForCategory(category.name).map { it.toModel() }

    override suspend fun ensureContentLoaded() = loadMutex.withLock {
        if (contentReady) return@withLock
        withContext(Dispatchers.IO) {
            val pack = loader.loadBundled()
            val stored = dao.getMeta()
            if (stored != null && stored.version >= pack.version) return@withContext

            val problems = ContentPackValidator.validate(pack)
            check(problems.isEmpty()) { "Bundled content is invalid:\n" + problems.joinToString("\n") }

            val blockByTopic = pack.topics.associate { it.id to it.block }
            dao.replaceContent(
                topics = pack.topics.mapIndexed { i, t -> t.toEntity(sortOrder = i) },
                questions = pack.questions.map { it.toEntity(blockByTopic.getValue(it.topicId)) },
                meta = ContentMetaEntity(version = pack.version, rulesEdition = pack.rulesEdition),
            )
        }
        contentReady = true
    }
}
