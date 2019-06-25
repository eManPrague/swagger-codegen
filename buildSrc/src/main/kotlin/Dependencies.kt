import org.gradle.api.JavaVersion
import kotlin.String


/* =============================  VERSIONS ======================= */

private object Versions {

    val kotlin = "1.3.30"
    val dokka = "0.9.17"

    val gradle = "5.2.1"
    //val gradleBuildTools = "3.5.0-alpha07"
    val gradleBuildTools = "3.4.0"

    val swaggerCodegen = "3.0.8"
    val swaggerGenerators = "1.0.8"

    val mavenPublish = "3.6.2"
    val mavenGradleGithub = "1.5"
    val bintrayGradle = "1.8.4"

    val timber = "4.7.1"
    val timberKtx = "0.1.0"
    val junit = "4.12"
    val kotlinTest = "3.3.0"

}


/* =============================  BUILD-PLUGINS ======================= */

object GradlePlugins {
    val encoding = "UTF-8"
    val gradle = Versions.gradle

    val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
    val bintrayGradle = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintrayGradle}"
}

/* =============================  ARTIFACTS ============================= */

object Artifact {
    val groupId = "cz.eman.swagger"
    val artifactId = "swagger-codegen"

    val sourceCompatibilityJava = JavaVersion.VERSION_1_8
    val targetCompatibilityJava = JavaVersion.VERSION_1_8
}

object Dependencies {
    /* =============================  KOTLIN ============================== */

    object Kotlin {
        val kotlinStbLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    }

    /* =============================  LIBS ================================ */

    object Libs {
        val swaggerCodegen = "io.swagger.codegen.v3:swagger-codegen:${Versions.swaggerCodegen}"
        val swaggerGenerators = "io.swagger.codegen.v3:swagger-codegen-generators:${Versions.swaggerGenerators}"
        val timber = "com.jakewharton.timber:timber:${Versions.timber}"
        val timberKtx = "cz.eman.logger:timber-ktx:${Versions.timberKtx}"
    }

    /* =============================  TEST-LIBS =========================== */

    object TestLibs {
        val junit = "junit:junit:${Versions.junit}"
        val kotlinTest = "io.kotlintest:kotlintest-runner-junit5:${Versions.kotlinTest}"
    }
}