plugins {
    id("healthinsights.android.library")
    id("healthinsights.android.hilt")
}

android {
    namespace = "com.healthinsights.feature.healthconnect"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.health.connect.client)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
