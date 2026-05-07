plugins {
    id("healthinsights.android.feature")
}

android {
    namespace = "com.healthinsights.feature.onboarding"
}

dependencies {
    implementation(project(":feature:health-connect"))
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core.ktx)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
