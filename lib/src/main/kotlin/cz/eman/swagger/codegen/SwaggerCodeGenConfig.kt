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
 * @author eMan s.r.o. (info@eman.cz)
 */
open class SwaggerCodeGenConfig : CodegenConfigurator(), Cloneable {

    private lateinit var currentTaskConfig: SwaggerCodeGenTaskConfig

    var sourcePath = "./"
    var outputPath = "./"
    var configs = listOf<SwaggerCodeGenTaskConfig>()
    var autoHook = true
    private var additionalPropertiesCopy: MutableMap<String, Any> = HashMap()

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
     * Sets workflow variables to the generator. Variables set are [setInputSpec] and [setOutputDir].
     */
    private fun setWorkflowVariables() {
        setInputSpec("$sourcePath/${currentTaskConfig.inputFileName}")
        setOutputDir("$outputPath/${currentTaskConfig.outputFolderName}")
    }

    /**
     * Merges additional properties of this config and current task config. Uses copy of [additionalProperties] (since
     * they are private). Every time value is added it is first deleted to make sure previous settings is not kept
     * instead of the current one.
     */
    private fun mergeAdditionalProperties() {
        currentTaskConfig.additionalProperties?.forEach { (key, value) ->
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