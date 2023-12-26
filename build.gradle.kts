buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://nexus.eman.cz/repository/maven-public")
    }

    group = "cz.eman.swagger"
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
