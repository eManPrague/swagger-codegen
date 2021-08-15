import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(files("src/main/kotlin"))
}

val dokka by tasks.dokkaHtml
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

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
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

                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/eManPrague/swagger-codegen/issues")
                    }
                }
            }
        }

        repositories {
            maven(url = "https://nexus.eman.cz/repository/maven-public") {
                name = "Nexus"

                credentials {
                    username = findPropertyOrNull("nexus.username")
                    password = findPropertyOrNull("nexus.password")
                }
            }
        }
    }
}