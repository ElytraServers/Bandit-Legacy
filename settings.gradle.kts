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
        maven {
            name = "WagYourMaven"
            url = uri("https://maven.wagyourtail.xyz/releases")
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.5"
}

stonecutter {
    create(rootProject) {
        versions("1.7.10", "1.12.2")
        vcsVersion = "1.7.10"
    }
}
