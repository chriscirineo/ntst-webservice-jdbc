plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs") version "6.0.19"
}

group = "com.ntst"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Note the double quotes and parentheses
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")
    implementation("com.puppycrawl.tools:checkstyle:10.26.1")
    implementation("net.sourceforge.pmd:pmd:7.15.0")
    implementation("com.github.spotbugs:spotbugs:4.9.3")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
