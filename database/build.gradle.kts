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

        withHostTest {
            isIncludeAndroidResources = true
        }

        // Opt-in to enable and configure device-side (instrumented) tests
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.bundles.unit.test)
            implementation(libs.androidx.junit)
            implementation(libs.androidx.junit.ktx)
            implementation(libs.androidx.runner)
            implementation(libs.coroutines.test)
            implementation(libs.androidx.room.testing)
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

        commonTest.dependencies {
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotations.common)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}
