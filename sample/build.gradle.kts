// import org.jetbrains.dokka.gradle.DokkaTask
import cz.eman.swagger.codegen.SwaggerCodeGenConfig
import cz.eman.swagger.codegen.SwaggerCodeGenTaskConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm")
    id("swagger-codegen")
    id("kotlin-kapt")
}

dependencies {
    // Kotlin
    implementation(Dependencies.Kotlin.kotlinStbLib)
    implementation(Dependencies.Retrofit.retrofit)
    implementation(Dependencies.Tools.moshiKotlin) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(Dependencies.Tools.moshiAdapters) {
        exclude(group = "org.jetbrains.kotlin")
    }
    kapt(Dependencies.Tools.moshiCodegen)
}

/**
 * Swagger/OpenApi generator config
 */
configure<SwaggerCodeGenConfig> {
    sourcePath = "${project.projectDir.absolutePath}/openapi"
    outputPath = "${buildDir.absolutePath}/openapi"
    setLibrary("jvm-retrofit2")
    setGeneratorName("cz.eman.swagger.codegen.generator.kotlin.KotlinClientCodegen")

    setAdditionalProperties(
        mutableMapOf(
            "templateEngine" to "mustache",
            "dateLibrary" to "string",
            "enumPropertyNaming" to "original",
            "modelNameSuffix" to "Dto",
            "apiNameSuffix" to "",
            "generateInfrastructure" to false,
            "removeMinusTextInHeaderProperty" to true,
            "ignoreEndpointStartingSlash" to true,
            "serializationLibrary" to "moshi",
            "moshiCodeGen" to true,
            "removeOperationParams" to arrayOf(
                "Accept-Language",
                "ETag",
                "X-Access-Token",
                "X-Call-Chain-Id",
                "X-Execution-TimeStamp",
                "X-Release-Version"
            )
        )
    )

    configs =
        listOf(
            SwaggerCodeGenTaskConfig(
                inputFileName = "petstore.yaml",
                outputFolderName = "one-of",
                additionalProperties = mapOf(
                    "apiPackage" to "cz.eman.swagger.api.petstore",
                    "modelPackage" to "cz.eman.swagger.api.petstore.model"
                )
            )
        )
}

val generatedOneOfSrcDir = File(buildDir, "openapi/one-of/src/main/kotlin")

sourceSets {
    getByName("main").java.srcDirs(
        "src/main/kotlin",
            generatedOneOfSrcDir
    )
    getByName("test").java.srcDirs("src/test/kotlin")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
