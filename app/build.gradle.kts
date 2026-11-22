plugins {
    alias(libs.plugins.znaky.android.application)
    alias(libs.plugins.znaky.android.compose)
    alias(libs.plugins.znaky.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing comes from CI secrets (see .github/workflows/release.yml).
// Without them the release APK is signed with the shared key in app/debug.keystore.
// That key is committed on purpose: every build has the same signature, so a new
// APK installs over the previous one as an update instead of failing.
val releaseKeystore = System.getenv("ZNAKY_KEYSTORE_FILE")?.let(::file)?.takeIf { it.exists() }

android {
    namespace = "com.znaky.app"

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("ZNAKY_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ZNAKY_KEY_ALIAS")
                keyPassword = System.getenv("ZNAKY_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.znaky.app"
        // CI passes -PversionCode / -PversionName for every release build.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = (findProperty("versionName") as String?) ?: "0.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            // R8 shrinks and optimises code and resources (APK several times smaller, faster start).
            // CI launches the minified APK on an emulator before any release (scripts/smoke_test.py).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources.excludes += listOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/*.version", "DebugProbesKt.bin", "kotlin-tooling-metadata.json")
    }

    dependenciesInfo {
        // Not needed outside Google Play; keeps the APK smaller.
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.feature.home)
    implementation(projects.feature.practice)
    implementation(projects.feature.exam)
    implementation(projects.feature.stats)
    implementation(projects.feature.learn)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
}
