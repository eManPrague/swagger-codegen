package cz.eman.swagger.codegen.gradle

import io.swagger.codegen.v3.config.CodegenConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * eMan s.r.o.
 */
open class SwaggerCodeGenPlugin : Plugin<Project> {
    companion object TaskNames {
        const val SWAGGER_TASK = "swagger"
    }

    override fun apply(project: Project) {
        // I would probably change this so that it allows for multiple codegen runs to be configured within one project.
        project.extensions.create("swagger", CodegenConfigurator::class.java)
        //val swaggerTask = project.taskHelper<SwaggerCodeGenTask>(SWAGGER_TASK)

        val swaggerTask = project.tasks.create(SWAGGER_TASK, SwaggerCodeGenTask::class.java) {

        }

        project.getTasksByName("compileJava", false).forEach {
            it.dependsOn(swaggerTask)
        }
    }
}