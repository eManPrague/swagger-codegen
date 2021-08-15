import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.Kotlin.kotlinStbLib)
    implementation(Dependencies.Libs.openApiCodegen)

    testImplementation(Dependencies.TestLibs.junit)
    testImplementation(Dependencies.TestLibs.kotest)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka/html"
    configuration {
        moduleName = Artifact.artifactId
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(files("src/main/kotlin"))
}

val dokkaHtmlJar by tasks.creating(Jar::class) {
    archiveClassifier.set("kdoc-html")
    from(dokka.outputDirectory)
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

val releasePublication = "release"
publishing {
    publications {
        create<MavenPublication>(releasePublication) {
            artifactId = Artifact.artifactId
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaHtmlJar)

            pom {
                name.set("Swagger Codegen")
                description.set("A fork of the swagger-codegen by eMan")
                url.set("https://github.com/eManPrague/swagger-codegen")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        name.set("eMan a.s.")
                        email.set("info@eman.cz")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/eManPrague/swagger-codegen.git")
                    developerConnection.set("scm:git:ssh://git@github.com/eManPrague/swagger-codegen.git")
                    url.set("https://github.com/eManPrague/swagger-codegen")
                }
            }
        }
    }

    repositories {
        maven(url = "https://dl.bintray.com/emanprague/maven") { name = "bintray" }
    }
}

bintray {
    user = findPropertyOrNull("bintray.user")
    key = findPropertyOrNull("bintray.apikey")
    publish = true
    setPublications(releasePublication)
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
