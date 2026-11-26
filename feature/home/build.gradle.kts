plugins {
    alias(libs.plugins.znaky.android.feature)
}

dependencies {
    // BackHandler for stepping back through onboarding.
    implementation(libs.androidx.activity.compose)
}
