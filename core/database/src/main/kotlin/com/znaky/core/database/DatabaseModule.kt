package com.znaky.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ZnakyDatabase =
        Room.databaseBuilder(context, ZnakyDatabase::class.java, "znaky.db").build()

    @Provides
    fun provideContentDao(db: ZnakyDatabase) = db.contentDao()

    @Provides
    fun provideProgressDao(db: ZnakyDatabase) = db.progressDao()
}
