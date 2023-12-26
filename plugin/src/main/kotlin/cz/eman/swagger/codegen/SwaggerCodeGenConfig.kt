package cz.eman.swagger.codegen

import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.Context

/**
 * Wrapper for [CodegenConfigurator] that allows generating files for multiple configurations. Additional configuration
 * options are:
 * - [sourcePath] which contains folder where configuration files are saved.
 * - [outputPath] where should the files be created
 * - [configs] contains list of configurations
 *
 * This configuration is cloned using function [fromTask] to make sure tasks do not share the same instance of
 * configuration. If they did then only last configuration would be generated.
 *
 * Input spec path is generated as follows: [sourcePath]/[SwaggerCodeGenTaskConfig.inputFileName].
 * Output dir is generated as follows: [outputPath]/[SwaggerCodeGenTaskConfig.outputFolderName].
 *
 * Other configuration options:
 * - [autoHook] enables to auto-hook compilation tasks to compileJava or compileKotlin. When compileKotlin is present
 *   compileJava part does not run but it can be forced using [forceJava].
 * - [forceJava] forces java compilation when compileKotlin is present.
 *
 * @author eMan a.s. (info@eman.cz)
 */
open class SwaggerCodeGenConfig : CodegenConfigurator(), Cloneable {

    private lateinit var currentTaskConfig: SwaggerCodeGenTaskConfig

    var sourcePath = "./"
    var outputPath = "./"
    var configs = listOf<SwaggerCodeGenTaskConfig>()
    var autoHook = true
    var forceJava = false
    private var additionalPropertiesCopy: MutableMap<String, Any> = HashMap()
    private var additionalPropertiesAddedKeys: MutableSet<String> = mutableSetOf()

    override fun toContext(): Context<*> {
        setWorkflowVariables()
        mergeAdditionalProperties()
        currentTaskConfig.library?.let { setLibrary(it) }
        return super.toContext()
    }

    public override fun clone(): Any {
        return super.clone()
    }

    /**
     * Clones this configuration and sets task configuration which will be used to modify workflow settings before
     * calling [toContext].
     *
     * @param taskConfig configuration of specific task
     * @return [SwaggerCodeGenConfig] which is a clone of current object.
     */
    internal fun fromTask(taskConfig: SwaggerCodeGenTaskConfig): SwaggerCodeGenConfig =
        (clone() as SwaggerCodeGenConfig).apply {
            currentTaskConfig = taskConfig
        }

    /**
     * Sets workflow variables to the generator. Variables set are [setInputSpec] and [setOutputDir]. Variables are
     * generated using [buildPath].
     */
    private fun setWorkflowVariables() {
        setInputSpec(buildPath(sourcePath, currentTaskConfig.inputFileName))
        setOutputDir(buildPath(outputPath, currentTaskConfig.outputFolderName))
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
     * Merges additional properties of this config and current task config. Uses copy of [additionalProperties] (since
     * they are private). Every time value is added it is first deleted to make sure previous settings is not kept
     * instead of the current one.
     */
    private fun mergeAdditionalProperties() {
        additionalPropertiesAddedKeys.forEach { additionalPropertiesCopy.remove(it) }

        currentTaskConfig.additionalProperties?.forEach { (key, value) ->
            if (!additionalPropertiesCopy.containsKey(key)) {
                additionalPropertiesAddedKeys.add(key)
            }
            additionalPropertiesCopy.remove(key)
            additionalPropertiesCopy[key] = value
        }
        setAdditionalProperties(additionalPropertiesCopy)
    }

    /**
     * Keeps a copy of [additionalProperties] so this config is able to modify them at will.
     *
     * @param additionalProperties to be set to this config
     * @return [CodegenConfigurator] (this)
     */
    override fun setAdditionalProperties(additionalProperties: MutableMap<String, Any>?): CodegenConfigurator {
        additionalProperties?.toMap()?.run {
            additionalPropertiesCopy.clear()
            additionalPropertiesCopy.putAll(this)
        }
        return super.setAdditionalProperties(additionalProperties)
    }
}