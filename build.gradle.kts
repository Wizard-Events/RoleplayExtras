plugins {
    java
    id("com.gradleup.shadow") version ("8.3.2")
}

group = "ron.thewizard"
version = "1.3.0"
description = "Gameplay alterations for wizard event rp."

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

    maven("https://maven.maxhenkel.de/repository/public") {
        name = "henkelmax-public"
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("com.github.thatsmusic99:ConfigurationMaster-API:v2.0.0-rc.3")
    implementation("space.arim.morepaperlib:morepaperlib:0.4.3")
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
        relocate("io.github.thatsmusic99.configurationmaster", "ron.thewizard.roleplayextras.libs.configmaster")
        relocate("space.arim.morepaperlib", "ron.thewizard.roleplayextras.libs.morepaperlib")
        relocate("org.reflections", "ron.thewizard.roleplayextras.libs.reflections")
    }
}