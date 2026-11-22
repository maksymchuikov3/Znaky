package com.znaky.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.znaky.core.database.dao.ContentDao
import com.znaky.core.database.dao.ProgressDao
import com.znaky.core.database.entity.AnswerLogEntity
import com.znaky.core.database.entity.ContentMetaEntity
import com.znaky.core.database.entity.ExamResultEntity
import com.znaky.core.database.entity.QuestionEntity
import com.znaky.core.database.entity.ReviewCardEntity
import com.znaky.core.database.entity.TopicEntity

@Database(
    entities = [
        TopicEntity::class,
        QuestionEntity::class,
        AnswerLogEntity::class,
        ReviewCardEntity::class,
        ExamResultEntity::class,
        ContentMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ZnakyDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun progressDao(): ProgressDao
}
