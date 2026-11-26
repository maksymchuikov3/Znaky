plugins {
    alias(libs.plugins.znaky.android.library)
    alias(libs.plugins.znaky.android.compose)
}

dependencies {
    api(projects.core.model)
    api(projects.core.art)
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
}
