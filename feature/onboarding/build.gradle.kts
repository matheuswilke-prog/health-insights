plugins {
    id("healthinsights.android.feature")
}

android {
    namespace = "com.healthinsights.feature.onboarding"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core.ktx)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}