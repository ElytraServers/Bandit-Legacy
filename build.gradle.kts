plugins {
    java
    kotlin("jvm") version "2.2.0"
    id("xyz.wagyourtail.unimined") version "1.4.1"
    // id("xyz.wagyourtail.jvmdowngrader") version "1.3.3"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
    maven {
        url = uri("https://maven.wagyourtail.xyz/releases")
    }
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        url = uri("https://raw.githubusercontent.com/Taskeren/elytra-mcp/master/publishing/")
    }
}

dependencies {

}

unimined.minecraft {
    version = property("mcVersion").toString()

    mappings {
        searge()
        mcp("stable", property("mcpMappingVersion").toString())
    }

    minecraftForge {
        loader(property("forgeVersion").toString())
        mixinConfig("mixins.bandit.json")
    }

    mods {
        modImplementation {
            catchAWNamespaceAssertion()
        }
    }

    runs {
        all {
            args("-Dmixin.debug.countInjections=true", "-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
    }
}

if(file("deps.gradle").exists()) apply(from = "deps.gradle")

kotlin {
    // we use JvmDowngrader to transform JDK 17 artifact to JDK 8 compatible.
    jvmToolchain(8)
}

stonecutter {
    replacements.string {
        direction = eval(current.version, ">1.7.10")
        from = "cpw.mods.fml"
        to = "net.minecraftforge.fml"
    }
}

/*jvmdg {
    downgradeTo = JavaVersion.VERSION_1_8
}

tasks.downgradeJar {
    downgradeTo = JavaVersion.VERSION_1_8
}

tasks.jar {
    finalizedBy("downgradeJar")
}*/

tasks.register("processIdeaSettings") {
}

sourceSets.main.configure {
    java.srcDir("src/main/api")
    kotlin.srcDir("src/main/api")
}
