/* =============================  VERSIONS ======================= */

private object Versions {
    const val kotlin = "1.3.72"
    const val dokka = "0.10.0"
    const val bintrayGradle = "1.8.4"

    const val openApiCodegen = "4.3.0"

    const val junit = "4.12"
    const val kotlinTest = "3.3.0"

    const val retrofit = "2.6.0"
    const val moshi = "1.9.2"
    const val swaggerCodeGen = "2.1.0"
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

    object GradlePlugins {
        const val swaggerCodeGen = "cz.eman.swagger:swagger-codegen:${Versions.swaggerCodeGen}"
    }

    object Kotlin {
        const val kotlinStbLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    }

    object Libs {
        const val openApiCodegen = "org.openapitools:openapi-generator:${Versions.openApiCodegen}"
    }

    object Retrofit {
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val moshiConverter =
                "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    }

    object Tools {
        const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
        const val moshiAdapters = "com.squareup.moshi:moshi-adapters:${Versions.moshi}"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    }

    object TestLibs {
        const val junit = "junit:junit:${Versions.junit}"
        const val kotlinTest = "io.kotlintest:kotlintest-runner-junit5:${Versions.kotlinTest}"
    }

}