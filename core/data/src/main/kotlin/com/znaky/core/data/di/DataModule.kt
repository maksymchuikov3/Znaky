package com.znaky.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.znaky.core.data.repository.DataStoreSettingsRepository
import com.znaky.core.data.repository.OfflineProgressRepository
import com.znaky.core.data.repository.OfflineQuestionRepository
import com.znaky.core.domain.StartExamUseCase
import com.znaky.core.domain.readiness.ReadinessCalculator
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.domain.review.LeitnerScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindQuestionRepository(impl: OfflineQuestionRepository): QuestionRepository

    @Binds
    abstract fun bindProgressRepository(impl: OfflineProgressRepository): ProgressRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}

/** Domain classes are plain Kotlin (no DI annotations), so they are provided here. */
@Module
@InstallIn(SingletonComponent::class)
internal object DomainModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.settingsDataStore

    @Provides
    fun provideLeitnerScheduler() = LeitnerScheduler()

    @Provides
    fun provideReadinessCalculator() = ReadinessCalculator()

    @Provides
    fun provideStartExamUseCase(questions: QuestionRepository, settings: SettingsRepository) =
        StartExamUseCase(questions, settings)
}
