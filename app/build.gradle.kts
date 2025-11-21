import java.io.FileInputStream
import java.util.Properties
// import org.gradle.api.GradleException
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.oss.licenses)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    (this as ApplicationExtension).apply {
        compileSdk = 36
        namespace = "com.jeeldobariya.passcodes"

        defaultConfig {
            applicationId = "com.jeeldobariya.passcodes"
            minSdk = 26
            targetSdk = 34
            versionCode = 2
            versionName = "v1.1.2-rc.2"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        signingConfigs {
            create("release") {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                    storeFile = file(keystoreProperties.getProperty("storeFile"))
                    storePassword = keystoreProperties.getProperty("storePassword")
                } else {
                    logger.warn("WARNING: keystore.properties not found for release signing config.")
                    // throw GradleException("keystore.properties not found!")
                }
            }

            create("staging") {
                val keystorePropertiesFile = rootProject.file("staging-keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                    keyAlias = keystoreProperties.getProperty("stagingKeyAlias")
                    keyPassword = keystoreProperties.getProperty("stagingKeyPassword")
                    storeFile = file(keystoreProperties.getProperty("stagingStoreFile"))
                    storePassword = keystoreProperties.getProperty("stagingStorePassword")
                } else {
                    logger.warn("WARNING: keystore.properties not found for release signing config.")
                    // throw GradleException("keystore.properties not found!")
                }
            }
        }

        splits {
            abi {
                isEnable = true
                reset()
                include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
                isUniversalApk = true
            }
        }

        lint {
            // baseline = rootProject.file("lint-baseline.xml") // If you use a baseline
            lintConfig = rootProject.file("lint.xml")
        }

        buildTypes {
            getByName("release") {
                if (rootProject.file("keystore.properties").exists()) {
                    signingConfig = signingConfigs.getByName("release")
                } else {
                    logger.warn("WARNING: Release build will not be signed as keystore.properties is missing.")
                    // throw GradleException("Can't Sign Release Build")
                }

                isDebuggable = false
                isShrinkResources = true
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

                // Use manifestPlaceholders.put() for key-value pairs
                manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
                manifestPlaceholders["appLabel"] = "@string/app_name"
            }

            getByName("debug") {
                applicationIdSuffix = ".dev"
                versionNameSuffix = "-Dev"

                isDebuggable = true
                isShrinkResources = false
                isMinifyEnabled = false

                manifestPlaceholders["appIcon"] = "@drawable/dev_ic_launcher_foreground"
                manifestPlaceholders["appLabel"] = "Passcodes-Dev"
            }

            create("staging") {
                if (rootProject.file("staging-keystore.properties").exists()) {
                    signingConfig = signingConfigs.getByName("staging")
                } else {
                    logger.warn("WARNING: Staging build will not be signed as staging-keystore.properties is missing.")
                    // throw GradleException("Can't Sign Staging Build")
                }

                applicationIdSuffix = ".staging"
                versionNameSuffix = "-Staging"

                isDebuggable = false
                isShrinkResources = true
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

                manifestPlaceholders["appIcon"] = "@drawable/dev_ic_launcher_foreground"
                manifestPlaceholders["appLabel"] = "Passcodes-Staging"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    ksp {
        val location = "$projectDir/schemas"
        arg("room.schemaLocation", location)
    }
}

dependencies {
    // Jetpack Compose
    implementation(libs.bundles.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.bundles.compose.debug)

    // Navigation 3
    implementation(libs.bundles.navigation3)

    // Standard Kotlin Libraries
    implementation(libs.kotlin.stdlib)

    // UI/Google Services
    implementation(libs.material)
    implementation(libs.oss.license)
    implementation(libs.appcompat)

    // Data/Persistence (Room Bundle)
    implementation(libs.bundles.room)
    debugImplementation(libs.androidx.ui.test.manifest)
    ksp(libs.room.compiler)

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

    // Local Unit Testing (Unit Test Bundle)
    testImplementation(libs.bundles.unit.test)

    // Android Instrumented Testing (Android Test Bundle)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.bundles.coroutines.test)
    androidTestImplementation(libs.truth) // Keeping truth explicit for androidTest, though it's in both bundles.
}
