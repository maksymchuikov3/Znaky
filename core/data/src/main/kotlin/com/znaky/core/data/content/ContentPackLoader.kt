package com.znaky.core.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ContentPackLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadBundled(): ContentPack =
        context.assets.open(BUNDLED_PATH).bufferedReader().use { json.decodeFromString(ContentPack.serializer(), it.readText()) }

    companion object {
        const val BUNDLED_PATH = "content/questions.json"
    }
}
