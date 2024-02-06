plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "the.grid.smp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("net.bytebuddy:byte-buddy-net.bytebuddy.agent:1.14.10")
    compileOnly("org.ow2.asm:asm:9.5")
}

tasks.withType<ProcessResources> {
    val props = mapOf("version" to version)
    inputs.properties(props)

    filesMatching("plugin.yml") {
        expand(props)
    }
}
