import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.jeeldobariya.passcodes.core"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }

        create("staging") {
            isMinifyEnabled = false
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Navigation 3
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Networking/Parsing
    implementation(libs.okhttp)
    implementation(libs.json)

    // Concurrency (Coroutines Bundle)
    implementation(libs.bundles.coroutines)

    // Android Architecture Components (Lifecycle Bundle)
    implementation(libs.lifecycle.runtime)

    // Dependency Injection
    implementation(libs.koin)

    // Datastore Preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.unit.test)
}
