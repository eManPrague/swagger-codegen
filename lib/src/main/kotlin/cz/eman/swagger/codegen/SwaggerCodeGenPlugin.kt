package cz.eman.swagger.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Swagger codegen plugin used to generate API files from OpenAPI specification. It auto-hooks into "compileJava" and
 * "compileKotlin" process so there is no need to configure dependencies and gradle tasks (they are auto created).
 *
 * You can turn off auto hooking by settings [SwaggerCodeGenConfig.autoHook] to false.
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
                configsExt.configs.forEach {
                    project.getTasksByName("compileJava", false)
                        .first()
                        .dependsOn(createGenerator(project, "java", configsExt, it))
                }
            }
        }

        project.afterEvaluate {
            if (configsExt.autoHook) {
                configsExt.configs.forEach {
                    project.getTasksByName("compileKotlin", false)
                        .first()
                        .dependsOn(createGenerator(project, "kotlin", configsExt, it))
                }
            }
        }
    }

    /**
     * Creates a generator based on global config and task config.
     *
     * @param project which is running the generator
     * @param language used in the generator (this is only to distinguish gradle tasks from each other)
     * @param config global config which is a child of [SwaggerCodeGenConfig]
     * @param taskConfig specific task config which allows multiple apis to be generated
     */
    private fun createGenerator(
        project: Project,
        language: String,
        config: SwaggerCodeGenConfig,
        taskConfig: SwaggerCodeGenTaskConfig
    ): Task {
        val taskName = "swagger-${language}-${taskConfig.outputFolderName}"

        return project.tasks.register(taskName, SwaggerCodeGenTask::class.java) {
            it.configuration = config.fromTask(taskConfig)
        }.get()
    }
}