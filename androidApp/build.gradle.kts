import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "net.coolshell.poite"
    compileSdk = 36
    defaultConfig {
        applicationId = "net.coolshell.poite"
        minSdk = 24
        targetSdk = 36
        // Minor and patch must stay in 0..999 to keep versionCode monotonically increasing.
        versionCode = 1 * 1_000_000 + // x-release-please-major
            1 * 1_000 + // x-release-please-minor
            0 // x-release-please-patch
        versionName = "1.1.0" // x-release-please-version
    }
    buildTypes {
        release {
            isMinifyEnabled = false

            val keystorePath = System.getenv("POITE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                val releaseSigning = signingConfigs.create("release") {
                    storeFile = file(keystorePath)
                    storePassword = System.getenv("POITE_KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("POITE_KEY_ALIAS")
                    keyPassword = System.getenv("POITE_KEY_PASSWORD")
                }
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation.layout)
}
