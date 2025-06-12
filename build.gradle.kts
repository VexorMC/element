plugins {
    kotlin("jvm") version "2.0.21"
    id("dev.lunasa.patcher") version "1.0.1"
}

group = "dev.lunasa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net/")
}

dependencies {
    mappings("net.legacyfabric:yarn:1.8.9+build.551:mergedv2")

    implementation("com.nothome:javaxdelta:2.0.1")
}

patcher {
    minecraftVersion = "1.8.9"
}

tasks.runClient {
    mainClass.set("com.element.launch.Main")

    jvmArgs(
        "-Delement.environment=dev",
    )
}

kotlin {
    jvmToolchain(17)
}