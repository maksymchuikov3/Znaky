plugins {
    alias(libs.plugins.znaky.android.library)
    alias(libs.plugins.znaky.android.room)
    alias(libs.plugins.znaky.hilt)
}

dependencies {
    implementation(projects.core.model)
}
