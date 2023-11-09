/* =============================  VERSIONS ======================= */

private object Versions {
    const val kotlin = "1.9.10"
    const val dokka = "1.9.10"

    const val openApiCodegen = "5.2.1"

    const val junit = "5.7.2"
    const val kotest = "4.6.1"

    const val retrofit = "2.9.0"
    const val moshi = "1.15.0"
    const val swaggerCodeGen = "2.3.0"
}

/* =============================  BUILD-PLUGINS ======================= */

object GradlePlugins {
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
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
        const val junit = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
        const val kotest = "io.kotest:kotest-runner-junit5:${Versions.kotest}"
    }

}
