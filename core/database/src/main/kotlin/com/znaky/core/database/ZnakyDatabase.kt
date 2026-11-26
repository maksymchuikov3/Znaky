package com.znaky.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.znaky.core.database.dao.ContentDao
import com.znaky.core.database.dao.ProgressDao
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.znaky.core.database.entity.AnswerLogEntity
import com.znaky.core.database.entity.BookmarkEntity
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
        BookmarkEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class ZnakyDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun progressDao(): ProgressDao
}

/** Version 2 adds bookmarks; answers and review cards are kept. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `bookmarks` (`questionId` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL, PRIMARY KEY(`questionId`))",
        )
    }
}
