buildscript {
    repositories {
        mavenCentral()
        mavenLocal() // TODO: remove - dev only
        maven("https://nexus.eman.cz/repository/maven-public")
    }

    dependencies {
        classpath(GradlePlugins.kotlin)
        classpath(GradlePlugins.dokka)
        classpath(Dependencies.GradlePlugins.swaggerCodeGen)
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal() // TODO: remove - dev only
        maven("https://nexus.eman.cz/repository/maven-public")
    }

    group = Artifact.groupId
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
