package cz.eman.swagger.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
    companion object TaskNames {
        const val SWAGGER_TASK = "swagger"
    }

    override fun apply(project: Project) {
        val configsExt = project.extensions.create(SWAGGER_TASK, SwaggerCodeGenConfig::class.java)

        project.afterEvaluate {
            if (configsExt.autoHook) {
                val compileKotlinTask = project.getTasksByName("compileKotlin", false).first()
                if (compileKotlinTask == null || configsExt.forceJava) {
                    val compileJavaTask = project.getTasksByName("compileJava", false).first()
                    configsExt.configs.forEach { taskConfig ->
                        createGenerator(project, "java", configsExt, taskConfig)?.let { task ->
                            compileJavaTask.dependsOn(task)
                        }
                    }
                }
            }
        }

        project.afterEvaluate {
            if (configsExt.autoHook) {
                val compileKotlinTask = project.getTasksByName("compileKotlin", false).first()
                configsExt.configs.forEach { taskConfig ->
                    createGenerator(project, "kotlin", configsExt, taskConfig)?.let { task ->
                        compileKotlinTask.dependsOn(task)
                    }
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
        append("swagger")
        append("-$language")
        taskOutputFolder?.let { append("-$it") }
    }
}