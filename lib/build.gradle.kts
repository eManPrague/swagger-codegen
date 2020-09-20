import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("com.jfrog.bintray")
}

dependencies {
//    implementation(fileTree(args = *arrayOf(Pair("dir", "libs"), Pair("include", arrayOf("*.jar")))))
    implementation(gradleApi())

    // Kotlin
    implementation(Dependencies.Kotlin.kotlinStbLib)

    // OpenAPI
    implementation(Dependencies.Libs.openApiCodegen)

    // Tests
    testImplementation(Dependencies.TestLibs.junit)
    testImplementation(Dependencies.TestLibs.kotlinTest)
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html" // html, md, javadoc,
    outputDirectory = "$buildDir/dokka/html"
    configuration {
        moduleName = "lib"
    }
}

tasks.create<Jar>("sourcesJar") {
    from(files("src/main/kotlin"))
    archiveClassifier.set("sources")
}

tasks.create<Jar>("dokkaHtmlJar") {
    archiveClassifier.set("kdoc-html")
    from("$buildDir/dokka/html")
    dependsOn(dokka)
}

gradlePlugin {
    plugins {
        register("swagger-codegen-plugin") {
            id = "swagger-codegen"
            implementationClass = "cz.eman.swagger.codegen.SwaggerCodeGenPlugin"
        }
    }
}

group = Artifact.groupId
version = "${project.version}"

val productionPublicName = "production"

bintray {
    user = findPropertyOrNull("bintray.user")
    key = findPropertyOrNull("bintray.apikey")
    publish = true
    setPublications(productionPublicName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "cz.eman.swagger.codegen"
        userOrg = "emanprague"
        override = true
        websiteUrl = "https://www.emanprague.com/en/"
        githubRepo = "eManPrague/swagger-codegen"
        vcsUrl = "https://github.com/eManPrague/swagger-codegen"
        description = "A fork of the swagger-codegen by eMan"
        setLabels(
                "kotlin",
                "swagger",
                "codegen",
                "retrofit",
                "room",
                "swagger-codegen",
                "openapi"
        )
        setLicenses("MIT")
        desc = description
    })
}

publishing {
    publications {
        register("production", MavenPublication::class) {
            artifactId = Artifact.artifactId
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaHtmlJar"])
            //artifact(sourcesJar.get())
        }
    }

    repositories {
        maven(url = "http://dl.bintray.com/emanprague/maven")
    }
}

repositories {
    mavenCentral()
}