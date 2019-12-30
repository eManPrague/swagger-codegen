package cz.eman.swagger.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.openapitools.codegen.config.CodegenConfigurator

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @since 1.0.0
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