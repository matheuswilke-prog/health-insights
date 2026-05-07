plugins {
    id("healthinsights.android.library.compose")
    id("healthinsights.android.hilt")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
}
