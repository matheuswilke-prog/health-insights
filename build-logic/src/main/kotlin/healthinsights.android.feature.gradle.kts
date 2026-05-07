plugins {
    id("healthinsights.android.library.compose")
    id("healthinsights.android.hilt")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.findLibrary("androidx-hilt-navigation-compose").get())
    implementation(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
}
