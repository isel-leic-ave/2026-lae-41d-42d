plugins {
    alias(libs.plugins.kotlin.jvm)
    id("me.champeau.jmh") version "0.7.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lesson12-domain-model"))
    implementation(project(":lesson30-logger"))

    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}
