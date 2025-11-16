plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.oss.licenses) apply false
}

// Allprojects block is common for setting up common repositories for all subprojects.
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Task to clean the build directory, now registered using tasks.register
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// You might put common dependency versions here using 'ext' for simpler projects,
// but Version Catalogs (libs.versions.toml) are the modern way for multi-module projects.
// For example:
// ext {
//     set("androidx_appcompat_version", "1.7.0")
//     set("material_version", "1.12.0")
//     // ... and so on
// }
