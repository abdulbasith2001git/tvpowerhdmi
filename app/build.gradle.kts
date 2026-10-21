plugins {
    id("com.android.application")
}

android {
    namespace = "com.tvpower.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvpower.app"
        minSdk = 28
        // 30 = Android 11 (your TV). Keeps background-start rules gentle.
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}
