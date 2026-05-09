plugins {
    id("healthinsights.android.feature")
}

android {
    namespace = "com.healthinsights.feature.dashboard"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
