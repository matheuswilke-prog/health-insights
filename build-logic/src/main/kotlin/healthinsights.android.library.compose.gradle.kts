plugins {
    id("healthinsights.android.library")
}

apply(plugin = "org.jetbrains.kotlin.plugin.compose")

android {
    buildFeatures {
        compose = true
    }
}

val libs = versionCatalogs.named("libs")

dependencies {
    val bom = platform(libs.findLibrary("androidx-compose-bom").get())
    implementation(bom)
    implementation(libs.findLibrary("androidx-compose-material3").get())
    implementation(libs.findLibrary("androidx-compose-material-icons-core").get())
    implementation(libs.findLibrary("androidx-compose-ui").get())
    implementation(libs.findLibrary("androidx-compose-ui-graphics").get())
    implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    debugImplementation(libs.findLibrary("androidx-compose-ui-tooling").get())
}
