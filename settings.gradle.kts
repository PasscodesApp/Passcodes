pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // Recommended for fetching Gradle plugins
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
                useModule("com.google.android.gms:oss-licenses-plugin:0.10.9")
            }
        }
    }
}

// Defines all modules in your project
include(":app")

// If you have other modules like ':data', ':domain', etc., list them here:
// include(":app", ":data", ":domain", ":feature:home")

// Define root project name
rootProject.name = "Passcodes"
