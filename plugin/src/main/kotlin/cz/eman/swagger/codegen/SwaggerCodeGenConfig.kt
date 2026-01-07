package cz.eman.swagger.codegen

import org.openapitools.codegen.config.CodegenConfigurator

/**
 * Wrapper for [CodegenConfigurator] that allows generating files for multiple configurations. Additional configuration
 * options are:
 * - [sourcePath] which contains folder where configuration files are saved.
 * - [outputPath] where should the files be created
 * - [configs] contains list of configurations
 *
 * Input spec path is generated as follows: [sourcePath]/[SwaggerCodeGenTaskConfig.inputFileName].
 * Output dir is generated as follows: [outputPath]/[SwaggerCodeGenTaskConfig.outputFolderName].
 *
 * Other configuration options:
 * - [autoHook] enables to auto-hook compilation tasks to compileJava or compileKotlin. When compileKotlin is present
 *   compileJava part does not run, but it can be forced using [forceJava].
 * - [forceJava] forces java compilation when compileKotlin is present.
 *
 * @author eMan a.s. (info@eman.cz)
 */
open class SwaggerCodeGenConfig : CodegenConfigurator() {

    var sourcePath = "./"
    var outputPath = "./"
    var configs = listOf<SwaggerCodeGenTaskConfig>()
    var autoHook = true
    var forceJava = false
    private var additionalPropertiesValue: MutableMap<String, Any> = HashMap()
    private var generatorNameValue: String? = null
    private var libraryValue: String? = null

    /**
     * Gets the generator name from the local variable.
     */
    fun getGeneratorName(): String? = generatorNameValue

    /**
     * Gets the library from the local variable.
     */
    fun getLibrary(): String? = libraryValue

    /**
     * Gets additional properties as a read-only map.
     */
    fun getAdditionalPropertiesMap(): Map<String, Any> = additionalPropertiesValue.toMap()

    override fun setGeneratorName(generatorName: String?): CodegenConfigurator {
        generatorNameValue = generatorName
        return super.setGeneratorName(generatorName)
    }

    override fun setLibrary(library: String?): CodegenConfigurator {
        libraryValue = library
        return super.setLibrary(library)
    }

    /**
     * Keeps a copy of [additionalProperties] so this config is able to modify them at will.
     *
     * @param additionalProperties to be set to this config
     * @return [CodegenConfigurator] (this)
     */
    override fun setAdditionalProperties(additionalProperties: MutableMap<String, Any>?): CodegenConfigurator {
        additionalProperties?.toMap()?.run {
            additionalPropertiesValue.clear()
            additionalPropertiesValue.putAll(this)
        }
        return super.setAdditionalProperties(additionalProperties)
    }
}
