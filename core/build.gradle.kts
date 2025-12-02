import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    // Standard Kotlin Libraries
    implementation(libs.kotlin.stdlib)

    // UI/Google Services
    implementation(libs.material)
    implementation(libs.oss.license)
    implementation(libs.appcompat)

    // Networking/Parsing
    implementation(libs.okhttp)
    implementation(libs.json)

    // Concurrency (Coroutines Bundle)
    implementation(libs.bundles.coroutines)

    // Android Architecture Components (Lifecycle Bundle)
    implementation(libs.bundles.lifecycle)

    // Dependency Injection
    implementation(libs.bundles.koin)

    // Datastore Preferences
    implementation(libs.bundles.datastore.preferences)


    // --- Testing ---
    debugImplementation(libs.androidx.ui.test.manifest)

    // Local Unit Testing (Unit Test Bundle)
    testImplementation(libs.bundles.unit.test)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
