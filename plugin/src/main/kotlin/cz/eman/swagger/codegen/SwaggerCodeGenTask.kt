package cz.eman.swagger.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.openapitools.codegen.DefaultGenerator
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.Context
import java.io.File

/**
 * @author eMan a.s. (info@eman.cz)
 * @since 1.0.0
 */
abstract class SwaggerCodeGenTask : DefaultTask() {
    init {
        group = "Swagger"
    }

    /**
     * The input specification file path.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputSpec: Property<File>

    /**
     * The generator name.
     */
    @get:Input
    abstract val generatorName: Property<String>

    /**
     * The library to use for generation.
     */
    @get:Input
    @get:Optional
    abstract val library: Property<String>

    /**
     * Additional properties for code generation.
     */
    @get:Input
    @get:Optional
    abstract val additionalProperties: MapProperty<String, Any>

    /**
     * The output directory for generated files.
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * Project directory to protect from deletion.
     */
    @get:Internal
    abstract val projectDir: DirectoryProperty

    /**
     * Root project directory to protect from deletion.
     */
    @get:Internal
    abstract val rootProjectDir: DirectoryProperty

    /**
     * Configuration object constructed from properties at task execution time.
     * This is not serialized and is reconstructed for each execution.
     */
    @get:Internal
    internal val configuration: CodegenConfigurator
        get() = CodegenConfigurator().apply {
            setInputSpec(inputSpec.get().absolutePath)
            setOutputDir(outputDir.get().asFile.absolutePath)
            setGeneratorName(generatorName.get())
            if (library.isPresent) {
                setLibrary(library.get())
            }
            if (additionalProperties.isPresent && additionalProperties.get().isNotEmpty()) {
                setAdditionalProperties(additionalProperties.get().toMutableMap())
            }
        }

    /**
     * Context class that holds settings set using [CodegenConfigurator].
     */
    @get:Internal
    internal val context: Context<*>
        get() = configuration.toContext()

    /**
     * Guard against deleting the directory being passed.
     * I accidentally did this to the root project directory.
     */
    private fun validateDeleteOutputDir(againstDir: File) {
        val output = outputDir.get().asFile
        if (output == againstDir) {
            throw GradleException("You probably don't want to delete this directory: $againstDir")
        }
    }

    @TaskAction
    fun swaggerCodeGen() {
        validateDeleteOutputDir(projectDir.get().asFile)
        validateDeleteOutputDir(rootProjectDir.get().asFile)

        // If the spec has changed then this file will have changed.
        outputDir.get().asFile.deleteRecursively()

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
