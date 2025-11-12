import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.GradleException
import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.oss.licenses)
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
            versionName = "v1.1.1-Alpha"

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

                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

                // Use manifestPlaceholders.put() for key-value pairs
                manifestPlaceholders.put("appIcon", "@mipmap/ic_launcher")
                manifestPlaceholders.put("appLabel", "@string/app_name")
            }

            getByName("debug") {
                applicationIdSuffix = ".dev"
                versionNameSuffix = "-Dev"
                isMinifyEnabled = false

                manifestPlaceholders.put("appIcon", "@mipmap/dev_ic_launcher")
                manifestPlaceholders.put("appLabel", "Passcodes Dev")
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

                isMinifyEnabled = true
                isShrinkResources = true
                isDebuggable = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

                manifestPlaceholders.put("appIcon", "@mipmap/dev_ic_launcher")
                manifestPlaceholders.put("appLabel", "Passcodes Staging")
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }
    }

    ksp {
        val location = "$projectDir/schemas"
        arg("room.schemaLocation", location)
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)

    implementation(libs.material)
    implementation(libs.oss.license)
    implementation(libs.appcompat)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.okhttp)
    implementation(libs.json)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)

    implementation(libs.koin)

    // test
    testImplementation(libs.junit)
    testImplementation(libs.truth)

    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.truth)
}
