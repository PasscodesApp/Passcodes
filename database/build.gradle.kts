import org.gradle.kotlin.dsl.provideDelegate

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.ksp)
    alias (libs.plugins.androidx.room)
}

kotlin {
    android {
        compileSdk = 36
        minSdk = 26
        namespace = "com.jeeldobariya.passcodes.database"
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin)
        }

        commonMain.dependencies {
            // Coroutines
            implementation(libs.bundles.coroutines)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            // Dependency Injection
            implementation(libs.koin)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}
