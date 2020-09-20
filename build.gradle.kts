buildscript {
    repositories {
        jcenter()
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
        jcenter()

        // For OpenApi generator snapshot
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    group = Artifact.groupId
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
