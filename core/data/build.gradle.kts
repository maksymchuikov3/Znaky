plugins {
    alias(libs.plugins.znaky.android.library)
    alias(libs.plugins.znaky.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.database)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
}
