/* =============================  VERSIONS ======================= */

private object Versions {
    const val kotlin = "1.3.61"
    const val dokka = "0.10.0"
    const val bintrayGradle = "1.8.4"

    const val openApiCodegen = "4.2.3-SNAPSHOT"

    const val junit = "4.12"
    const val kotlinTest = "3.3.0"
}

/* =============================  BUILD-PLUGINS ======================= */

object GradlePlugins {
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
    const val bintrayGradle = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintrayGradle}"
}

/* =============================  ARTIFACTS ============================= */

object Artifact {
    const val groupId = "cz.eman.swagger"
    const val artifactId = "swagger-codegen"
}

/* =============================  DEPENDENCIES ============================= */

object Dependencies {

    /* =============================  KOTLIN ============================== */

    object Kotlin {
        const val kotlinStbLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    }

    /* =============================  LIBS ================================ */

    object Libs {
        const val openApiCodegen = "org.openapitools:openapi-generator:${Versions.openApiCodegen}"
    }

    /* =============================  TEST-LIBS =========================== */

    object TestLibs {
        const val junit = "junit:junit:${Versions.junit}"
        const val kotlinTest = "io.kotlintest:kotlintest-runner-junit5:${Versions.kotlinTest}"
    }

}