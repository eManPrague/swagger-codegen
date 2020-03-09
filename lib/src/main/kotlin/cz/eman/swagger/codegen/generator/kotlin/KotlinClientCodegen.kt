package cz.eman.swagger.codegen.generator.kotlin

import com.google.common.collect.ImmutableMap
import com.samskivert.mustache.Mustache
import cz.eman.swagger.codegen.language.*
import cz.eman.swagger.codegen.templating.mustache.IgnoreStartingSlashLambda
import cz.eman.swagger.codegen.templating.mustache.RemoveMinusTextFromNameLambda
import io.swagger.v3.oas.models.media.*
import org.openapitools.codegen.*
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Kotlin client generator based on [AbstractKotlinCodegen]. Contains libraries and options that are not supported in
 * default OpenAPI generator (https://github.com/OpenAPITools/openapi-generator).
 *
 * Supported libraries:
 * - `Multiplatform (multiplatform)` - generates api containing multiplatform functions and required model classes.
 * - `Okhttp (jvm-okhttp)` - generates api containing okhttp functions and required model classes.
 * - `Retrofit2 (jvm-retrofit2)` - generates api containing retrofit functions and required model classes.
 * - `Room v1 (room)` - generated model classes supporting Room up to version 1.1.1.
 * - `Room v2 (room2)` - generates model classes supporting Room from version 2.0.0 (androidx).
 *
 * Additional generator options:
 * - `dateLibrary` - By this property you can set date library used to serialize dates and times.
 * - `generateInfrastructure` - By this property you can enable to generate API infrastructure.
 * - `collectionType` - By this property cou can change collection type.
 * - `emptyDataClasses` - By this property you can enable empty data classes being generated. (Note: it should not pass Kotlin compilation.)
 * - `composedArrayAsAny` - By this property array of composed is changed to array of object (kotlin.Any).
 * - `generatePrimitiveTypeAlias` - By this property aliases to primitive are also generated.
 * - `removeMinusTextInHeaderProperty` - By this property you can enable to generate name of header property without text minus if it is present.
 * - `removeOperationParams` - By this property you can remove specific parameters from API operations.
 * - `ignoreEndpointStartingSlash` - By this property you can ignore a starting slash from an endpoint definition if it is present.
 *
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @author eMan s.r.o. (david.sucharda@eman.cz)
 * @since 2.0.0
 */
open class KotlinClientCodegen : org.openapitools.codegen.languages.KotlinClientCodegen() {

    private var emptyDataClasses = false
    private var composedArrayAsAny = true
    private var generatePrimitiveTypeAlias = false
    private var removeOperationParams: List<String> = emptyList()
    private val numberDataTypes = arrayOf("kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double")

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KotlinClientCodegen::class.java)

        const val ROOM = "room"
        const val ROOM2 = "room2"

        const val VENDOR_EXTENSION_BASE_NAME_LITERAL = "x-base-name-literal"
        const val VENDOR_EXTENSION_IS_ALIAS = "x-is-alias"
    }

    enum class GenerateApiType constructor(val value: String) {
        INFRASTRUCTURE("infrastructure"),
        API("api")
    }

    enum class HeadersCommands constructor(val value: String) {
        REMOVE_MINUS_WORD_FROM_PROPERTY(REMOVE_MINUS_TEXT_FROM_HEADER)
    }

    enum class EndpointsCommands constructor(val value: String) {
        INGORE_ENDPOINT_STARTING_SLASH(REMOVE_ENDPOINT_STARTING_SLASH)
    }

    /**
     * Constructs an instance of `KotlinClientCodegen`.
     */
    init {
        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase
        initArtifact()
        initTemplates()
        initSettings()
        initHeaders()
        addLibraries()
    }

    override fun addMustacheLambdas(): ImmutableMap.Builder<String, Mustache.Lambda> {
        val lambdas = super.addMustacheLambdas()
        lambdas.put(RemoveMinusTextFromNameLambda.LAMBDA_NAME, RemoveMinusTextFromNameLambda(this))
        lambdas.put(IgnoreStartingSlashLambda.LAMBDA_NAME, IgnoreStartingSlashLambda(this))

        return lambdas
    }

    override fun setLibrary(library: String?) {
        super.setLibrary(library)
        logger.info("Setting library: $library")
        supportedLibraries.keys.forEach { additionalProperties[it] = it == library }
        initArtifact()
        initTemplates()
    }

    override fun getTag(): CodegenType {
        return CodegenType.OTHER
    }

    override fun getName(): String {
        return "kotlin-client-v2"
    }

    override fun getHelp(): String {
        return "Generates a Kotlin classes for specific library."
    }

    override fun toModelFilename(name: String): String {
        return toModelName(name)
    }

    override fun toApiName(name: String?): String {
        return super.toApiName(name) + apiNameSuffix
    }

    /**
     * Processes and adds generator options.
     *
     * @since 2.0.0
     */
    override fun processOpts() {
        super.processOpts()
        processOptsInfrastructure()
        processOptsAdditionalSupportingFiles()
        processOptsAdditional()
    }

    /**
     * Modifies schema before the actual [fromModel] function is called. Modifies model based on [emptyDataClasses] and
     * [composedArrayAsAny] options.
     *
     * @since 2.0.0
     */
    override fun fromModel(name: String?, schema: Schema<*>?): CodegenModel {
        emptyDataClassAsString(name, schema)
        composedArrayAsAny(name, schema)
        return super.fromModel(name, schema)
    }

    /**
     * Enum number names are unified with Java code generation.
     *
     * @since 2.0.0
     */
    override fun toEnumVarName(value: String?, datatype: String?): String {
        var name = super.toEnumVarName(value, datatype)
        if (datatype in numberDataTypes) {
            name = "NUMBER$name"
            name = name.replace("-".toRegex(), "MINUS_")
            name = name.replace("\\+".toRegex(), "PLUS_")
            name = name.replace("\\.".toRegex(), "_DOT_")
        }
        return name
    }

    /**
     * Post process models and adds vendor extensions.
     *
     * @since 2.0.0
     */
    override fun postProcessModels(objs: Map<String?, Any?>): Map<String?, Any?>? {
        val objects = super.postProcessModels(objs)
        val models = objs["models"] as List<*>? ?: emptyList<Any>()
        for (model in models) {
            val mo = model as Map<*, *>
            (mo["model"] as CodegenModel?)?.let {
                setModelVendorExtensions(it)
            }
        }
        return objects
    }

    /**
     * Post process operations with models to check if the operation is multipart ot not.
     *
     * @since 2.0.0
     */
    @Suppress("UNCHECKED_CAST")
    override fun postProcessOperationsWithModels(
            objs: MutableMap<String?, Any?>,
            allModels: List<Any?>?
    ): Map<String, Any>? {
        super.postProcessOperationsWithModels(objs, allModels)
        val operations = objs["operations"] as? Map<String, Any>?
        if (operations != null) {
            (operations["operation"] as List<*>?)?.forEach { operation ->
                if (operation is CodegenOperation) {
                    filterOperationParams(operation)
                    if (operation.hasConsumes) {
                        if (isMultipartType(operation.consumes)) {
                            operation.isMultipart = true
                            objs["isMultipart"] = true
                        }
                    }

                    // modify the data type of binary form parameters to a more friendly type for multiplatform builds
                    if (MULTIPLATFORM == getLibrary() && operation.allParams != null) {
                        for (param in operation.allParams) {
                            if (param.dataFormat != null && param.dataFormat == "binary") {
                                param.dataType = "io.ktor.client.request.forms.InputProvider"
                                param.baseType = param.dataType
                            }
                        }
                    }
                }
            }
        }
        return operations
    }

    /**
     * Initializes artifact settings such as id, output folder or package name.
     *
     * @since 2.0.0
     */
    private fun initArtifact() {
        val libraryArtifact = if (library != null) "-$library" else ""
        artifactId = "kotlin${libraryArtifact}-client"
        packageName = "cz.eman.swagger"
        apiPackage = "$packageName.api"
        modelPackage = "$packageName.model"
        outputFolder = "generated-code" + File.separator + artifactId

        // cliOptions default redefinition need to be updated
        updateOption(CodegenConstants.ARTIFACT_ID, artifactId)
        updateOption(CodegenConstants.PACKAGE_NAME, packageName)
    }

    /**
     * Initializes template files for generator.
     *
     * @since 2.0.0
     */
    private fun initTemplates() {
        modelTemplateFiles["model.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"

        if (library != ROOM && library != ROOM2) {
            logger.info("Adding API template files")
            apiTemplateFiles["api.mustache"] = ".kt"
            apiDocTemplateFiles["api_doc.mustache"] = ".md"
        } else {
            logger.info("Removing API template files")
            apiTemplateFiles.clear()
            apiDocTemplateFiles.clear()
        }
    }

    /**
     * Adds all settings options to this generator.
     *
     * @since 2.0.0
     */
    private fun initSettings() {
        initSettingsInfrastructure()
        initSettingsEmptyDataClass()
        initSettingsComposedArrayAny()
        initSettingsGeneratePrimitiveTypeAlias()
        initSettingsRemoveOperationParams()
    }

    /**
     * Settings used to generate api only or api with infrastructure. Options are [GenerateApiType.INFRASTRUCTURE] or
     * [GenerateApiType.API]. Default is [GenerateApiType.API].
     *
     * @since 2.0.0
     */
    private fun initSettingsInfrastructure() {
        val infrastructureCli = CliOption(GENERATE_INFRASTRUCTURE_API, GENERATE_INFRASTRUCTURE_API_DESCRIPTION)
        val infraOptions = HashMap<String, String>()
        infraOptions[GenerateApiType.INFRASTRUCTURE.value] = "Generate Infrastructure API"
        infraOptions[GenerateApiType.API.value] = "Generate API"
        infraOptions[EndpointsCommands.INGORE_ENDPOINT_STARTING_SLASH.value] =
                REMOVE_ENDPOINT_STARTING_SLASH_DESCRIPTION
        infrastructureCli.enum = infraOptions

        cliOptions.add(infrastructureCli)
    }

    /**
     * Adds all headers options to this generator
     *
     * @since 2.0.0
     */
    private fun initHeaders() {
        val headersCli = CliOption(HEADER_CLI, HEADER_CLI_DESCRIPTION)
        val headersOptions = HashMap<String, String>()
        headersOptions[HeadersCommands.REMOVE_MINUS_WORD_FROM_PROPERTY.value] =
                REMOVE_MINUS_TEXT_FROM_HEADER_DESCRIPTION
        headersCli.enum = headersOptions
        cliOptions.add(headersCli)
    }

    /**
     * Settings to allow empty data classes. These are not allowed by default because they do not pass
     * kotlin compile. All empty data classes are re-typed to String.
     *
     * @since 2.0.0
     */
    private fun initSettingsEmptyDataClass() {
        cliOptions.add(
                CliOption.newBoolean(
                        EMPTY_DATA_CLASS,
                        EMPTY_DATA_CLASS_DESCRIPTION,
                        false
                )
        )
    }

    /**
     * Settings to cast array of composed schema (Array<OneOf...>) to array of kotlin.Any (Array<kotlin.Any).
     *
     * @since 2.0.0
     */
    private fun initSettingsComposedArrayAny() {
        cliOptions.add(
                CliOption.newBoolean(
                        COMPOSED_ARRAY_ANY,
                        COMPOSED_ARRAY_ANY_DESCRIPTION,
                        true
                )
        )
    }

    /**
     * Settings to generate type aliases for primitives. Default codegen does not generate aliases to primitives and
     * instead it uses the primitives. Viz: https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/DefaultGenerator.java#L496.
     *
     * @since 2.0.0
     */
    private fun initSettingsGeneratePrimitiveTypeAlias() {
        cliOptions.add(
                CliOption.newBoolean(
                        GENERATE_PRIMITIVE_TYPE_ALIAS,
                        GENERATE_PRIMITIVE_TYPE_ALIAS_DESCRIPTION,
                        false
                )
        )
    }

    /**
     * Settings used to to remove parameters from operations. Value should an array/list of strings.
     *
     * @since 2.0.0
     */
    private fun initSettingsRemoveOperationParams() {
        val removeOperationParamsCli = CliOption(REMOVE_OPERATION_PARAMS, REMOVE_OPERATION_PARAMS_DESCRIPTION)
        cliOptions.add(removeOperationParamsCli)
    }

    /**
     * Adds additional libraries to this generator: [ROOM] and [ROOM2].
     *
     * @since 2.0.0
     */
    private fun addLibraries() {
        supportedLibraries[ROOM] =
                "Platform: Room v1. JSON processing: Moshi 1.9.2."
        supportedLibraries[ROOM2] =
                "Platform: Room v2 (androidx). JSON processing: Moshi 1.9.2."

        val libraryOption = CliOption(CodegenConstants.LIBRARY, "Library template (sub-template) to use")
        libraryOption.enum = supportedLibraries
        libraryOption.default = JVM_RETROFIT2
        cliOptions.add(libraryOption)
        setLibrary(JVM_RETROFIT2)
    }

    /**
     * Processes options for infrastructure. Removes all supporting infrastructure files from the generation isf this
     * option is set to false (do not generate infrastructure). For more information see [initSettingsInfrastructure].
     *
     * @since 2.0.0
     */
    private fun processOptsInfrastructure() {
        var generateInfrastructure = false
        if (additionalProperties.containsKey(GENERATE_INFRASTRUCTURE_API)) {
            generateInfrastructure = convertPropertyToBooleanAndWriteBack(GENERATE_INFRASTRUCTURE_API)
        }

        if (!generateInfrastructure) {
            supportingFiles.clear()
        }
    }

    /**
     * Adds additional supporting files like Readme, build.gradle or settings.gradle.
     *
     * @since 2.0.0
     */
    private fun processOptsAdditionalSupportingFiles() {
        supportingFiles.add(SupportingFile("README.mustache", "", "README.md"))
        supportingFiles.add(SupportingFile("build.gradle.mustache", "", "build.gradle"))
        supportingFiles.add(SupportingFile("settings.gradle.mustache", "", "settings.gradle"))
    }

    /**
     * Processes options for additional settings:
     * - Empty data class: allows generating empty data classes. For more information see [initSettingsEmptyDataClass].
     * - Composed array as any: transforms array of composed to array or kotlin.Any. For more information see [initSettingsComposedArrayAny].
     * - Generate primitive type alias: generates alias for primitive objects. For more information see [initSettingsGeneratePrimitiveTypeAlias].
     *
     * @since 2.0.0
     */
    private fun processOptsAdditional() {
        if (additionalProperties.containsKey(EMPTY_DATA_CLASS)) {
            emptyDataClasses = convertPropertyToBooleanAndWriteBack(EMPTY_DATA_CLASS)
        }

        if (additionalProperties.containsKey(COMPOSED_ARRAY_ANY)) {
            composedArrayAsAny = convertPropertyToBooleanAndWriteBack(COMPOSED_ARRAY_ANY)
        }

        if (additionalProperties.containsKey(GENERATE_PRIMITIVE_TYPE_ALIAS)) {
            generatePrimitiveTypeAlias = convertPropertyToBooleanAndWriteBack(GENERATE_PRIMITIVE_TYPE_ALIAS)
        }

        if (additionalProperties.containsKey(CodegenConstants.MODEL_NAME_SUFFIX)) {
            setModelNameSuffix(additionalProperties[CodegenConstants.MODEL_NAME_SUFFIX] as String?)
        }

        if (additionalProperties.containsKey(REMOVE_OPERATION_PARAMS)) {
            removeOperationParams = when (val tempArray = additionalProperties[REMOVE_OPERATION_PARAMS]) {
                is Array<*> -> tempArray.mapNotNull { mapAnyToStringOrNull(it) }
                is List<*> -> tempArray.mapNotNull { mapAnyToStringOrNull(it) }
                else -> emptyList()
            }
        }
    }

    /**
     * Maps [Any]? value to [String]?.
     *
     * @param value to be mapped to [String]?
     * @since 2.0.0
     */
    private fun mapAnyToStringOrNull(value: Any?): String? = if (value is String) {
        value
    } else {
        null
    }

    /**
     * Changes schemas (excluding [ArraySchema], [MapSchema] and [ComposedSchema]) that do not have set type and do not
     * contain any properties. These schemas are set as [String] so they are parsed as a [StringSchema] instead of
     * [ObjectSchema]. This helps to avoid empty data classes which cannot pass Kotlin compilation.
     *
     * @param name of the schema being checked
     * @param schema to be checked for empty type and properties
     * @since 2.0.0
     */
    private fun emptyDataClassAsString(name: String?, schema: Schema<*>?) {
        if (!emptyDataClasses) {
            schema?.let {
                if (it !is ArraySchema && it !is MapSchema && it !is ComposedSchema
                        && (it.type == null || it.type.isEmpty())
                        && (it.properties == null || it.properties.isEmpty())
                ) {
                    logger.info("Schema: $name re-typed to \"string\"")
                    it.type = "string"
                }
            }
        }
    }

    /**
     * Changes array that contains composed schema to array that contains object schema because that will be changed to
     * [kotlin.Any] later in the generation.
     *
     * @param property to be checked for array of composed
     * @since 2.0.0
     */
    private fun composedArrayAsAny(name: String?, property: Schema<*>?) {
        if (composedArrayAsAny && property is ArraySchema && property.items is ComposedSchema) {
            logger.info("Schema: $name is array of composed -> changed to array of object")
            property.items = ObjectSchema()
        }
    }

    /**
     * Sets vendor extensions to the model and it's properties. Extensions added: [markModelAsTypeAlias] and
     * [escapePropertyBaseNameLiteral].
     *
     * @param model to have extensions added
     * @since 2.0.0
     */
    private fun setModelVendorExtensions(model: CodegenModel) {
        val modelProperties = model.vars +
                model.allVars +
                model.optionalVars +
                model.requiredVars +
                model.readOnlyVars +
                model.readWriteVars +
                model.parentVars
        markModelAsTypeAlias(model, modelProperties.size)
        escapePropertyBaseNameLiteral(modelProperties)
    }

    /**
     * Marks model as typealias using vendor extension [VENDOR_EXTENSION_IS_ALIAS]. This extension is set in two
     * cases:
     * - [emptyDataClasses] is set to false and model has no properties.
     * - [generatePrimitiveTypeAlias] is set to true and model is alias.
     *
     * @param model to be checked set as type alias
     * @param modelPropertiesCount used in the first case
     * @since 2.0.0
     */
    private fun markModelAsTypeAlias(model: CodegenModel, modelPropertiesCount: Int) {
        if ((!emptyDataClasses && modelPropertiesCount <= 0) || (model.isAlias && generatePrimitiveTypeAlias)) {
            logger.info("Model: ${model.name} marked as typealias")
            model.vendorExtensions[VENDOR_EXTENSION_IS_ALIAS] = true
            if (model.dataType == null) {
                model.dataType = model.parent
            }
            if (generatePrimitiveTypeAlias) {
                model.isAlias = false
            }
        }
    }

    /**
     * Adds vendor extension [VENDOR_EXTENSION_BASE_NAME_LITERAL] which contains escaped base name for use as a string
     * literal.
     *
     * @param modelProperties all properties to have this extension set
     * @since 2.0.0
     */
    private fun escapePropertyBaseNameLiteral(modelProperties: List<CodegenProperty>) {
        modelProperties.forEach { property ->
            property.vendorExtensions[VENDOR_EXTENSION_BASE_NAME_LITERAL] =
                    property.baseName.replace("$", "\\$")
        }
    }

    /**
     * Filters out operation params if their base name is contained in [removeOperationParams] list.
     *
     * @param operation to have params filtered
     * @since 2.0.0
     */
    private fun filterOperationParams(operation: CodegenOperation) {
        if (removeOperationParams.isNotEmpty()) {
            operation.allParams.removeIf { removeOperationParams.contains(it.baseName) }
            operation.allParams.last().hasMore = false
        }
    }

    /**
     * Checks if operation is Multipart or not.
     *
     * @param consumes operation consumes list
     * @return true if multipart
     * @since 2.0.0
     */
    private fun isMultipartType(consumes: List<Map<String, String>>): Boolean {
        val firstType = consumes[0]
        return "multipart/form-data" == firstType["mediaType"]
    }
}
