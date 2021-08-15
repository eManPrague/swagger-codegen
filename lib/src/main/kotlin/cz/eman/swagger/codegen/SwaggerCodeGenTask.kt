package cz.eman.swagger.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.openapitools.codegen.DefaultGenerator
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.Context
import java.io.File

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @since 1.0.0
 */
open class SwaggerCodeGenTask : DefaultTask() {
    init {
        group = "Swagger"
    }

    /**
     * Ideally this would be marked as an input to this task however I need to fix some things around how it is implemented.
     */
    var configuration = CodegenConfigurator()

    /**
     * Context class that holds settings set using [CodegenConfigurator].
     */
    private val context: Context<*> by lazy { configuration.toContext() }

    @get:OutputDirectory
    val outputDir: File by lazy { project.file(context.workflowSettings.outputDir) }

    /**
     * Guard against deleting the directory being passed.
     * I accidentally did this to the root project directory.
     */
    private fun validateDeleteOutputDir(againstDir: File) {
        if (outputDir == againstDir) {
            throw GradleException("You probably don't want to delete this directory: $againstDir")
        }
    }

    @TaskAction
    fun swaggerCodeGen() {
        validateDeleteOutputDir(project.projectDir)
        validateDeleteOutputDir(project.rootProject.projectDir)

        // If the spec has changed then this file will have have changed.
        outputDir.deleteRecursively()

        /*
         * Since the generator sets system properties we need to ensure that two tasks don't try
         * to have system properties set in the same JVM.
         * https://github.com/swagger-api/swagger-codegen/issues/4788
         */
        synchronized(this::class) {
            val config = configuration
            val ctx = context
            // Verbose logging prints thw whole file with converted parameters so do not use this if
            // you do not want to get 100+ thousands of lines. :)
            //config.isVerbose = true

            DefaultGenerator()
                .opts(config.toClientOptInput())
                .generate()

            // Clean up the system environment variables that have been set by the code generator.
            // https://github.com/swagger-api/swagger-codegen/issues/4788
            ctx.workflowSettings.globalProperties.keys.forEach { System.clearProperty(it) }
        }
    }
}