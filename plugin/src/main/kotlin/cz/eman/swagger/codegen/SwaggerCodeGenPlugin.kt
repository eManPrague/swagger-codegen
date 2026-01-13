package cz.eman.swagger.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Swagger codegen plugin used to generate API files from OpenAPI specification. It auto-hooks into "compileJava" and
 * "compileKotlin" process so there is no need to configure dependencies and gradle tasks (they are auto created).
 *
 * You can turn off auto hooking by settings [SwaggerCodeGenConfig.autoHook] to false. Java generation is disabled when
 * Kotlin task exists, you can force generating it using [SwaggerCodeGenConfig.forceJava].
 *
 * @author eMan a.s. (info@eman.cz)
 * @since 1.0.0
 */
open class SwaggerCodeGenPlugin : Plugin<Project> {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(SwaggerCodeGenPlugin::class.java)

        const val SWAGGER_TASK = "swagger"
        const val COMPILE_KOTLIN = "compileKotlin"
        const val COMPILE_KOTLIN_JVM = "compileKotlinJvm"
        const val COMPILE_JAVA = "compileJava"
        const val LANGUAGE_KOTLIN = "kotlin"
        const val LANGUAGE_JAVA = "java"
    }

    override fun apply(project: Project) {
        val configsExt = project.extensions.create(SWAGGER_TASK, SwaggerCodeGenConfig::class.java)
        autoHookJava(project, configsExt)
        autoHookKotlin(project, configsExt)
    }

    /**
     * Auto-hooks openApi generation to project that contain compile task [COMPILE_JAVA] and do not contain
     * [COMPILE_KOTLIN] (only if java is not forced). Finds the task and crates java generator task for it with
     * dependency.
     *
     * @param project used to search for compile tasks
     * @param configsExt swagger gen config
     */
    private fun autoHookJava(project: Project, configsExt: SwaggerCodeGenConfig) {
        var hooked = false
        project.afterEvaluate {
            if (configsExt.autoHook) {
                val compileKotlinTask = project.getTasksByName(COMPILE_KOTLIN, false).firstOrNull()
                if (compileKotlinTask == null || configsExt.forceJava) {
                    project.getTasksByName(COMPILE_JAVA, false).firstOrNull()?.let { compileJava ->
                        configsExt.configs.forEach { taskConfig ->
                            createGenerator(
                                project,
                                LANGUAGE_JAVA,
                                configsExt,
                                taskConfig
                            )?.let { task ->
                                compileJava.dependsOn(task)
                            }
                        }
                        hooked = true
                    }
                }
            }
        }
        logger.info("Java auto-hooked: $hooked")
    }

    /**
     * Auto-hooks openApi generation to project that contain Kotlin compile task.
     * Finds the task and creates java generator task for it with dependency.
     *
     * @param project used to search for compile tasks
     * @param configsExt swagger gen config
     */
    private fun autoHookKotlin(project: Project, configsExt: SwaggerCodeGenConfig) {
        project.afterEvaluate {
            if (configsExt.autoHook) {
                val generators = configsExt.configs.mapNotNull { taskConfig ->
                    createGenerator(
                        project,
                        LANGUAGE_KOTLIN,
                        configsExt,
                        taskConfig
                    )
                }

                val compileTasks = project.kotlinCompileTasks()
                logger.info("Kotlin auto-hooked: ${compileTasks.isNotEmpty()}")
                compileTasks.all { task ->
                    task.dependsOn(generators)
                }
            }
        }
    }

    /**
     * Creates a generator based on global config and task config.
     *
     * @param project which is running the generator
     * @param language project language (this is only to distinguish gradle tasks from each other)
     * @param config global config which is a child of [SwaggerCodeGenConfig]
     * @param taskConfig specific task config which allows multiple apis to be generated
     * @return [Task] if it was created or null
     */
    private fun createGenerator(
        project: Project,
        language: String,
        config: SwaggerCodeGenConfig,
        taskConfig: SwaggerCodeGenTaskConfig
    ): Task? {
        val taskName = buildTaskName(language, taskConfig.outputFolderName)

        return if (project.tasks.findByName(taskName) == null) {
            project.tasks.register(taskName, SwaggerCodeGenTask::class.java) { task ->
                // Configure input specification
                val inputSpecPath = buildPath(config.sourcePath, taskConfig.inputFileName)
                task.inputSpec.set(project.file(inputSpecPath))

                // Configure output directory
                val outputDirPath = buildPath(config.outputPath, taskConfig.outputFolderName)
                task.outputDir.set(project.layout.projectDirectory.dir(outputDirPath))

                // Set project directories for validation (avoids accessing project at execution time)
                task.projectDir.set(project.layout.projectDirectory)
                task.rootProjectDir.set(project.rootProject.layout.projectDirectory)

                // Configure generator name
                val generatorName = config.getGeneratorName() ?: "kotlin"
                task.generatorName.set(generatorName)

                // Configure library
                val libraryValue = taskConfig.library ?: config.getLibrary()
                if (libraryValue != null) {
                    task.library.set(libraryValue)
                }

                // Merge additional properties
                task.additionalProperties.putAll(config.getAdditionalPropertiesMap())
                taskConfig.additionalProperties?.let { task.additionalProperties.putAll(it) }
            }.get()
        } else {
            null
        }
    }

    /**
     * Builds path based on parameters. Path format is "[configPath]/[taskConfigPath]".
     *
     * @param configPath general config path
     * @param taskConfigPath task config path (optional)
     * @return [String] with path
     */
    private fun buildPath(configPath: String, taskConfigPath: String?) = buildString {
        append(configPath)
        taskConfigPath?.let { append("/$it") }
    }

    /**
     * Builds task name based on parameters. Task name format is "swagger-[language]-[taskOutputFolder]".
     *
     * @param language project language (this is only to distinguish gradle tasks from each other)
     * @param taskOutputFolder output folder for this task (optional)
     * @return [String] with task name
     */
    private fun buildTaskName(language: String, taskOutputFolder: String?) = buildString {
        append(SWAGGER_TASK)
        append("-$language")
        taskOutputFolder?.let { append("-$it") }
    }
}
