// import org.gradle.api.GradleException
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

/**
 * Gets the current system time formatted as 'yy-mm-dd : hh-mm-ss'.
 */
fun getCurrentTimeLabel(): String {
    val formatter = SimpleDateFormat("yy-MM-dd - HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}

// --------------------------------------------------------------------------------
// | 🚀 BUILD CONFIGURATION PROPERTIES: UPDATE VERSIONS AND NAMES HERE ONLY      |
// --------------------------------------------------------------------------------


// Core Versioning
val appVersionCode = 3
val appVersionName = "v1.1.2-Alpha"
val appLabel = "v1.1.2 - Alpha"
val appDevLabel = "v1.1.2 - ${getCurrentTimeLabel()}"

// SDK Versions
val appCompileSdk = 36
val appMinSdk = 26
val appTargetSdk = 34

// Naming & Identification
val appNamespace = "com.jeeldobariya.passcodes"
val appBaseName = "Passcodes"

// Icon Configuration
val mainIcon = "@mipmap/ic_launcher"
val mainRoundIcon = "@mipmap/ic_launcher_round"
val devIcon = "@mipmap/dev_ic_launcher"
val devRoundIcon = "@mipmap/dev_ic_launcher_round"


// --------------------------------------------------------------------------------
// | ⚙️ PLUGINS AND CONFIGURATION                                                  |
// --------------------------------------------------------------------------------


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.oss.licenses)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    (this as ApplicationExtension).apply {
        compileSdk = appCompileSdk
        namespace = appNamespace

        defaultConfig {
            applicationId = appNamespace
            minSdk = appMinSdk
            targetSdk = appTargetSdk
            versionCode = appVersionCode

            // WARN: Keep it consistent with the res value property in build variants below...
            versionName = appVersionName

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
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )

                resValue("string", "app_name", appBaseName)
                resValue("string", "app_version", appLabel)

                manifestPlaceholders["appIcon"] = mainIcon
                manifestPlaceholders["appRoundIcon"] = mainRoundIcon
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
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )

                resValue("string", "app_name", "$appBaseName Staging")
                resValue("string", "app_version", appDevLabel)

                manifestPlaceholders["appIcon"] = devIcon
                manifestPlaceholders["appRoundIcon"] = devRoundIcon
            }

            getByName("debug") {
                applicationIdSuffix = ".dev"
                versionNameSuffix = "-Dev"

                isDebuggable = true
                isShrinkResources = false
                isMinifyEnabled = false

                resValue("string", "app_name", "$appBaseName Debug")
                resValue("string", "app_version", appDevLabel)

                manifestPlaceholders["appIcon"] = devIcon
                manifestPlaceholders["appRoundIcon"] = devRoundIcon
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
}

dependencies {
    // In project library / feature modules
    implementation(project(":core"))
    implementation(project(":database"))
    implementation(project(":password_manager"))
    implementation(project(":autofill"))

    // Android Core
    implementation(libs.appcompat)
    implementation(libs.material)

    // Jetpack Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Navigation 3
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Google Play License Services
    implementation(libs.oss.license)

    // Concurrency (Coroutines Bundle)
    implementation(libs.bundles.coroutines)

    // Dependency Injection
    implementation(libs.koin)
    implementation(libs.koin.compose)

    // Datastore Preferences
    implementation(libs.androidx.datastore.preferences)
}
