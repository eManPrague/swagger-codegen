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
        classpath(Dependencies.GradlePlugins.swaggerCodeGen)
    }
}

allprojects {
    repositories {
        google()
        jcenter()

        // For OpenApi generator snapshot
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}