import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath(GradlePlugins.kotlin)
        classpath(GradlePlugins.dokka)
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