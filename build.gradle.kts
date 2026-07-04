plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.12"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    // slf4j-simple pinned to Ktor 2.3.x's slf4j-api (1.7.36) to avoid a binding-version clash
    implementation("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
}

application {
    // CLI remains the default entry point; the HTTP server runs via the runServer task / Docker.
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(21)
}

// Runs the Ktor HTTP server (used locally by api-tester and in CI before Newman).
tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs the Ktor HTTP server (ServerKt)"
    mainClass.set("ServerKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.test {
    useJUnitPlatform()
}
