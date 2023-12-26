import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    id("maven-publish")
}

dependencies {
    implementation(libs.openapiGenerator)

    testImplementation(libs.jUnit)
    testImplementation(libs.kotest)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
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

val identifier = "swagger-codegen"
gradlePlugin {
    plugins {
        register("swagger-codegen-plugin") {
            id = identifier
            implementationClass = "cz.eman.swagger.codegen.SwaggerCodeGenPlugin"
        }
    }
    isAutomatedPublishing = false
}

publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = identifier
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
                username = findProperty("nexus.username") as String?
                password = findProperty("nexus.password") as String?
            }
        }
    }
}
