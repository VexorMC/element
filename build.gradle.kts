import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm") version "2.0.21"
    id("dev.lunasa.patcher") version "1.0.1"
}

group = "dev.lunasa"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.6"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            "natives-windows"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net/")
}

dependencies {
    mappings("net.legacyfabric:yarn:1.8.9+build.551:mergedv2")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-freetype")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-freetype", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    implementation("com.nothome:javaxdelta:2.0.1")
    implementation("org.joml:joml:1.10.8")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("com.mojang:datafixerupper:8.0.16")
}

patcher {
    minecraftVersion = "1.8.9"
}

configurations.all {
    exclude(group = "org.lwjgl.lwjgl")
}

tasks.runClient {
    mainClass.set("com.element.launch.Main")

    jvmArgs(
        "-Delement.environment=dev",
    )
}

sourceSets {
    main {
        java {
            srcDir("src/main/modern")
            srcDir("src/main/stub")
        }
    }
}

kotlin {
    jvmToolchain(17)
}