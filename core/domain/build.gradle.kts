plugins {
    id("healthinsights.kotlin.library")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    // Standard DI annotations (@Inject) — no Android dependency
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
