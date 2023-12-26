buildscript {
    repositories {
        mavenCentral()
        maven("https://nexus.eman.cz/repository/maven-public")
    }

    dependencies {
        classpath("cz.eman.swagger:swagger-codegen:${libs.versions.swaggerCodegen.get()}")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
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
