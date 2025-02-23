plugins {
    java
    id("com.gradleup.shadow") version ("9.0.0-beta6")
}

group = "ron.thewizard"
version = "1.4.0"
description = "Gameplay alterations for wizard event roleplay."

repositories {
    mavenCentral()

    maven("https://ci.pluginwiki.us/plugin/repository/everything/") {
        name = "configmaster-repo"
    }

    maven("https://repo.codemc.io/repository/maven-releases/") {
        name = "codemc-repo"
    }

    maven("https://repo.purpurmc.org/snapshots") {
        name = "purpurmc-repo"
    }

    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }

    maven("https://mvn-repo.arim.space/lesser-gpl3/") {
        name = "arim-mvn-lgpl3"
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.24.3")
    compileOnly("net.luckperms:api:5.4")

    implementation("com.github.thatsmusic99:ConfigurationMaster-API:v2.0.0-rc.3")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.reflections:reflections:0.10.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    build.configure {
        dependsOn("shadowJar")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "name" to project.name,
                    "version" to project.version,
                    "description" to project.description!!.replace('"'.toString(), "\\\""),
                    "url" to "https://github.com/xGinko"
                )
            )
        }
    }

    shadowJar {
        archiveFileName.set("RoleplayExtras-${version}.jar")

        relocate("com.github.benmanes.caffeine", "ron.thewizard.roleplayextras.libs.caffeine")
        relocate("io.github.thatsmusic99.configurationmaster", "ron.thewizard.roleplayextras.libs.configmaster")
        relocate("org.reflections", "ron.thewizard.roleplayextras.libs.reflections")
    }
}