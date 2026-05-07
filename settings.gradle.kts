pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Health Insights"
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:network")
include(":core:ui")
include(":feature:dashboard")
include(":feature:health-connect")
include(":feature:insights")
include(":feature:onboarding")
include(":feature:settings")
include(":feature:sleep")
include(":feature:workouts")
