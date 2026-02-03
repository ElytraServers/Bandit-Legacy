pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // FIXME: wait for GTNHGradle to merge the PR
    // See: https://github.com/GTNewHorizons/GTNHGradle/pull/73
    id("com.gtnewhorizons.gtnhsettingsconvention") version ("99.99.99")
}
