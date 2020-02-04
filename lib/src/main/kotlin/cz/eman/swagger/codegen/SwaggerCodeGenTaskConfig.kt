package cz.eman.swagger.codegen

/**
 * Configuration of generator task. Contains additional configuration for generator and allows multiple apis to be
 * generated.
 *
 * @param inputFileName define filename containing OpenApi definition
 * @param outputFolderName define name of output folder
 * @param library used to generate files. It is optional and when it is not filled default (global config) will be used.
 * @param additionalProperties to be used for this task (they override default additional properties).
 * @see SwaggerCodeGenConfig
 * @see SwaggerCodeGenPlugin
 * @since 2.0.0
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 */
data class SwaggerCodeGenTaskConfig(
    var inputFileName: String? = null,
    var outputFolderName: String? = null,
    var library: String? = null,
    var additionalProperties: Map<String, Any>? = null
)