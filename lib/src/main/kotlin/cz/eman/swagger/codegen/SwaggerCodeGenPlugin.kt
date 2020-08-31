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
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
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
        autoHookKotlin(project, configsExt, true)
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
                            createGenerator(project, LANGUAGE_JAVA, configsExt, taskConfig)?.let { task ->
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
     * Auto-hooks openApi generation to project that contain compile task [COMPILE_KOTLIN] or [COMPILE_KOTLIN_JVM].
     * Finds the task and crates java generator task for it with dependency.
     *
     * @param project used to search for compile tasks
     * @param configsExt swagger gen config
     */
    private fun autoHookKotlin(project: Project, configsExt: SwaggerCodeGenConfig, isMpp: Boolean = false) {
        var hooked = false
        project.afterEvaluate {
            if (configsExt.autoHook) {
                val compileTaskName = if (isMpp) {
                    COMPILE_KOTLIN_JVM
                } else {
                    COMPILE_KOTLIN
                }
                project.getTasksByName(compileTaskName, false).firstOrNull()?.let { compileKotlin ->
                    configsExt.configs.forEach { taskConfig ->
                        createGenerator(project, LANGUAGE_KOTLIN, configsExt, taskConfig)?.let { task ->
                            compileKotlin.dependsOn(task)
                        }
                    }
                    hooked = true
                }
            }
        }
        logger.info("Kotlin (isMpp: $isMpp) auto-hooked: $hooked")
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
            project.tasks.register(taskName, SwaggerCodeGenTask::class.java) {
                it.configuration = config.fromTask(taskConfig)
            }.get()
        } else {
            null
        }
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