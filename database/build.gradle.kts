import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.jeeldobariya.passcodes.database"
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

    ksp {
        val location = "$projectDir/schemas"
        arg("room.schemaLocation", location)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Data/Persistence (Room Bundle)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Dependency Injection
    implementation(libs.koin)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.bundles.coroutines.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.truth)
}
