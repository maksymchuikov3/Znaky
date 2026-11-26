plugins {
    alias(libs.plugins.znaky.android.feature)
}

dependencies {
    // BackHandler: «Назад» from a glossary chapter returns to the book's contents.
    implementation(libs.androidx.activity.compose)
}
