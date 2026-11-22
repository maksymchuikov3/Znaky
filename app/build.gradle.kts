plugins {
    alias(libs.plugins.znaky.android.application)
    alias(libs.plugins.znaky.android.compose)
    alias(libs.plugins.znaky.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.znaky.app"

    defaultConfig {
        applicationId = "com.znaky.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Release signing is configured in CI from secrets; see docs/architecture.md.
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.feature.home)
    implementation(projects.feature.practice)
    implementation(projects.feature.exam)
    implementation(projects.feature.stats)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
}
