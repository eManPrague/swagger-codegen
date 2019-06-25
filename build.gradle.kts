import org.gradle.kotlin.dsl.repositories

buildscript {

    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    dependencies {
        // Kotlin Grade plugin
        classpath(GradlePlugins.kotlin)

        // Build Tool to generate Kotlin KDoc documentation
        classpath(GradlePlugins.dokka)

        //classpath(GradlePlugins.mavenPublish)

        classpath(GradlePlugins.bintrayGradle)
    }
}

allprojects {

    repositories {
        google()
        jcenter()
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}