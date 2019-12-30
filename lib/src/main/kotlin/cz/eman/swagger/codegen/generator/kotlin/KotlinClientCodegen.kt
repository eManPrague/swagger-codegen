package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.language.*
import io.swagger.v3.oas.models.media.*
import org.openapitools.codegen.*
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Kotlin client generator based on [AbstractKotlinCodegen]. Contains libraries and options that are not supported in
 * default OpenAPI generator (https://github.com/OpenAPITools/openapi-generator).
 *
 * Supported libraries:
 * - `Retrofit2` - generates api containing retrofit functions and required model classes.
 * - `Room v1` - generated model classes supporting Room up to version 1.1.1.
 * - `Room v2 (androidx)` - generates model classes supporting Room from version 2.0.0.
 *
 * Additional generator options:
 * - `dateLibrary` - By this property you can set date library used to serialize dates and times.
 * - `generateInfrastructure` - By this property you can enable to generate API infrastructure.
 * - `collectionType` - By this property cou can change collection type.
 * - `emptyDataClasses` - By this property you can enable empty data classes being generated. (Note: it should not pass Kotlin compilation.)
 * - `composedArrayAsAny` - By this property array of composed is changed to array of object (kotlin.Any).
 * - `generatePrimitiveTypeAlias` - By this property aliases to primitive are also generated.
 *
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @author eMan s.r.o. (david.sucharda@eman.cz)
 * @since 1.1.0
 */
open class KotlinClientCodegen : AbstractKotlinCodegen() {

    private val LOGGER = LoggerFactory.getLogger(KotlinClientCodegen::class.java)

    private var dateLib = DateLibrary.JAVA8.value
    private var collectionType = CollectionType.ARRAY.value
    private var emptyDataClasses = false
    private var composedArrayAsAny = true
    private var generatePrimitiveTypeAlias = false
    private val numberDataTypes = arrayOf("kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double")

    companion object {
        const val RETROFIT2 = "retrofit2"
        const val ROOM = "room"
        const val ROOM2 = "room2"

        const val VENDOR_EXTENSION_BASE_NAME_LITERAL = "x-base-name-literal"
        const val VENDOR_EXTENSION_IS_ALIAS = "x-is-alias"
    }

    enum class DateLibrary constructor(val value: String) {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8"),
        MILLIS("millis")
    }

    enum class CollectionType(val value: String) {
        ARRAY("array"),
        LIST("list");
    }

    enum class GenerateApiType constructor(val value: String) {
        INFRASTRUCTURE("infrastructure"),
        API("api")
    }

    /**
     * Constructs an instance of `KotlinClientCodegen`.
     */
    init {
        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase
        initArtifact()
        initTemplates()
        initSettings()
        initLibraries()
    }

    override fun setLibrary(library: String?) {
        super.setLibrary(library)
        LOGGER.info("Setting library: $library")
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

    override fun toModelName(name: String): String {
        val modelName = super.toModelName(name)
        return if (modelName.startsWith("kotlin.") || modelName.startsWith("java.")) {
            modelName
        } else {
            "$modelNamePrefix$modelName$modelNameSuffix"
        }
    }

    override fun toApiName(name: String?): String {
        return super.toApiName(name) + apiNameSuffix
    }

    /**
     * Processes and adds generator options.
     *
     * @since 1.1.0
     */
    override fun processOpts() {
        super.processOpts()
        processOptsDateLib()
        processOptsInfrastructure()
        processOptsCollectionType()
        processOptsAdditionalSupportingFiles()
        processOptsAdditional()
    }

    /**
     * Modifies schema before the actual [fromModel] function is called. Modifies model based on [emptyDataClasses] and
     * [composedArrayAsAny] options.
     *
     * @since 1.1.0
     */
    override fun fromModel(name: String?, schema: Schema<*>?): CodegenModel {
        emptyDataClassAsString(name, schema)
        composedArrayAsAny(name, schema)
        return super.fromModel(name, schema)
    }

    /**
     * Enum number names are unified with Java code generation.
     *
     * @since 1.1.0
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
     * @since 1.1.0
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
     * @since 1.1.0
     */
    @Suppress("UNCHECKED_CAST")
    override fun postProcessOperationsWithModels(
        objs: Map<String?, Any?>,
        allModels: List<Any?>?
    ): Map<String, Any>? {
        super.postProcessOperationsWithModels(objs, allModels)
        val operations = objs["operations"] as? Map<String, Any>?
        if (operations != null) {
            (operations["operation"] as List<*>?)?.forEach { operation ->
                if (operation is CodegenOperation && operation.hasConsumes == java.lang.Boolean.TRUE) {
                    if (isMultipartType(operation.consumes)) {
                        operation.isMultipart = java.lang.Boolean.TRUE
                    }
                }
            }
        }
        return operations
    }

    /**
     * Initializes artifact settings such as id, output folder or package name.
     *
     * @since 1.1.0
     */
    private fun initArtifact() {
        val libraryArtifact = if (library != null) "-$library" else ""
        artifactId = "kotlin${libraryArtifact}-client"
        packageName = "cz.eman.swagger"
        apiPackage = "$packageName.api"
        modelPackage = "$packageName.model"
        outputFolder = "generated-code" + File.separator + artifactId
    }

    /**
     * Initializes template files for generator.
     *
     * @since 1.1.0
     */
    private fun initTemplates() {
        templateDir = "kotlin-client-v2"
        embeddedTemplateDir = templateDir
        modelTemplateFiles["model.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"

        if (library != ROOM && library != ROOM2) {
            LOGGER.info("Adding API template files")
            apiTemplateFiles["api.mustache"] = ".kt"
            apiDocTemplateFiles["api_doc.mustache"] = ".md"
        } else {
            LOGGER.info("Removing API template files")
            apiTemplateFiles.clear()
            apiDocTemplateFiles.clear()
        }
    }

    /**
     * Adds all settings options to this generator.
     *
     * @since 1.1.0
     */
    private fun initSettings() {
        initSettingsDateLibrary()
        initSettingsInfrastructure()
        initSettingsCollectionType()
        initSettingsEmptyDataClass()
        initSettingsComposedArrayAny()
        initSettingsGeneratePrimitiveTypeAlias()
    }

    /**
     * Settings defining default date library used. Options are [DateLibrary.THREETENBP], [DateLibrary.STRING],
     * [DateLibrary.JAVA8] or [DateLibrary.MILLIS]. Default is [DateLibrary.JAVA8].
     *
     * @since 1.1.0
     */
    private fun initSettingsDateLibrary() {
        val dateLibrary = CliOption(DATE_LIBRARY, DATE_LIBRARY_DESCRIPTION)
        val dateOptions = HashMap<String, String>()
        dateOptions[DateLibrary.THREETENBP.value] = "Threetenbp"
        dateOptions[DateLibrary.STRING.value] = "String"
        dateOptions[DateLibrary.JAVA8.value] = "Java 8 native JSR310"
        dateOptions[DateLibrary.MILLIS.value] = "Date Time as Long"
        dateLibrary.enum = dateOptions
        cliOptions.add(dateLibrary)
    }

    /**
     * Settings used to generate api only or api with infrastructure. Options are [GenerateApiType.INFRASTRUCTURE] or
     * [GenerateApiType.API]. Default is [GenerateApiType.API].
     *
     * @since 1.1.0
     */
    private fun initSettingsInfrastructure() {
        val infrastructureCli = CliOption(GENERATE_INFRASTRUCTURE_API, GENERATE_INFRASTRUCTURE_API_DESCRIPTION)
        val infraOptions = HashMap<String, String>()
        infraOptions[GenerateApiType.INFRASTRUCTURE.value] = "Generate Infrastructure API"
        infraOptions[GenerateApiType.API.value] = "Generate API"
        infrastructureCli.enum = infraOptions
        cliOptions.add(infrastructureCli)
    }

    /**
     * Settings to change collection type that this generator supports. Types used are [CollectionType.ARRAY] and
     * [CollectionType.LIST]. Default is [CollectionType.ARRAY].
     *
     * @since 1.1.0
     */
    private fun initSettingsCollectionType() {
        val collectionTypeCli = CliOption(COLLECTION_TYPE, COLLECTION_TYPE_DESCRIPTION)
        val collectionOptions: MutableMap<String, String> = java.util.HashMap()
        collectionOptions[CollectionType.ARRAY.value] = "kotlin.Array"
        collectionOptions[CollectionType.LIST.value] = "kotlin.collections.List"
        collectionTypeCli.enum = collectionOptions
        collectionTypeCli.default = this.collectionType
        cliOptions.add(collectionTypeCli)
    }

    /**
     * Settings to allow empty data classes. These are not allowed by default because they do not pass
     * kotlin compile. All empty data classes are re-typed to String.
     *
     * @since 1.1.0
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
     * @since 1.1.0
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
     * @since 1.1.0
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
     * Initializes supported libraries for this generator. Supported libraries are: [RETROFIT2], [ROOM] and [ROOM2].
     *
     * @since 1.1.0
     */
    private fun initLibraries() {
        supportedLibraries[RETROFIT2] =
            "[DEFAULT] Platform: Retrofit2 2.6.2. JSON processing: Moshi 1.9.2."
        supportedLibraries[ROOM] =
            "Platform: Room v1. JSON processing: Moshi 1.9.2."
        supportedLibraries[ROOM2] =
            "Platform: Room v2 (androidx). JSON processing: Moshi 1.9.2."

        val libraryOption = CliOption(CodegenConstants.LIBRARY, "Library template (sub-template) to use")
        libraryOption.enum = supportedLibraries
        libraryOption.default = RETROFIT2
        cliOptions.add(libraryOption)
        setLibrary(RETROFIT2)
    }

    /**
     * Processes options for date library. Alters type mapping based on which library is used. For more information
     * see [initSettingsDateLibrary].
     *
     * @since 1.1.0
     */
    private fun processOptsDateLib() {
        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            dateLib = additionalProperties[DATE_LIBRARY].toString()
        }

        when (dateLib) {
            DateLibrary.THREETENBP.value -> {
                additionalProperties[DateLibrary.THREETENBP.value] = true
                typeMapping["date"] = "LocalDate"
                typeMapping["DateTime"] = "LocalDateTime"
                importMapping["LocalDate"] = "org.threeten.bp.LocalDate"
                importMapping["LocalDateTime"] = "org.threeten.bp.LocalDateTime"
                defaultIncludes.add("org.threeten.bp.LocalDateTime")
            }
            DateLibrary.STRING.value -> {
                typeMapping["date-time"] = "kotlin.String"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.String"
            }
            DateLibrary.JAVA8.value -> additionalProperties[DateLibrary.JAVA8.value] = true
            DateLibrary.MILLIS.value -> {
                typeMapping["date-time"] = "kotlin.Long"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.Long"
            }
        }
    }

    /**
     * Processes options for infrastructure. Adds supporting file if infrastructure should be generated with the api.
     * For more information see [initSettingsInfrastructure].
     *
     * @since 1.1.0
     */
    private fun processOptsInfrastructure() {
        var generateInfrastructure = true
        if (additionalProperties.containsKey(GENERATE_INFRASTRUCTURE_API)) {
            generateInfrastructure = convertPropertyToBooleanAndWriteBack(GENERATE_INFRASTRUCTURE_API)
        }

        if (generateInfrastructure) {
            val infrastructureFolder =
                (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/")
            //supportingFiles.add(SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"))
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/ApiAbstractions.kt.mustache",
                    infrastructureFolder,
                    "ApiAbstractions.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/ApiInfrastructureResponse.kt.mustache",
                    infrastructureFolder,
                    "ApiInfrastructureResponse.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/ApplicationDelegates.kt.mustache",
                    infrastructureFolder,
                    "ApplicationDelegates.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/RequestConfig.kt.mustache",
                    infrastructureFolder,
                    "RequestConfig.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/RequestMethod.kt.mustache",
                    infrastructureFolder,
                    "RequestMethod.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/ResponseExtensions.kt.mustache",
                    infrastructureFolder,
                    "ResponseExtensions.kt"
                )
            )
            supportingFiles.add(
                SupportingFile(
                    "infrastructure/Serializer.kt.mustache",
                    infrastructureFolder,
                    "Serializer.kt"
                )
            )
            supportingFiles.add(SupportingFile("infrastructure/Errors.kt.mustache", infrastructureFolder, "Errors.kt"))
        }
    }

    /**
     * Processes options for collection type. Changes mapping based on which collection is selected. For more
     * information see [initSettingsCollectionType].
     *
     * @since 1.1.0
     */
    private fun processOptsCollectionType() {
        if (additionalProperties.containsKey(COLLECTION_TYPE)) {
            collectionType = additionalProperties[COLLECTION_TYPE].toString()
        }

        if (CollectionType.LIST.value == collectionType) {
            typeMapping["array"] = "kotlin.collections.List"
            typeMapping["list"] = "kotlin.collections.List"
            additionalProperties["isList"] = true
        }
    }

    /**
     * Adds additional supporting files like Readme, build.gradle or settings.gradle.
     *
     * @since 1.1.0
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
     * @since 1.1.0
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
    }

    /**
     * Changes schemas (excluding [ArraySchema], [MapSchema] and [ComposedSchema]) that do not have set type and do not
     * contain any properties. These schemas are set as [String] so they are parsed as a [StringSchema] instead of
     * [ObjectSchema]. This helps to avoid empty data classes which cannot pass Kotlin compilation.
     *
     * @param name of the schema being checked
     * @param schema to be checked for empty type and properties
     * @since 1.1.0
     */
    private fun emptyDataClassAsString(name: String?, schema: Schema<*>?) {
        if (!emptyDataClasses) {
            schema?.let {
                if (it !is ArraySchema && it !is MapSchema && it !is ComposedSchema
                    && (it.type == null || it.type.isEmpty())
                    && (it.properties == null || it.properties.isEmpty())
                ) {
                    LOGGER.info("Schema: $name re-typed to \"string\"")
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
     * @since 1.1.0
     */
    private fun composedArrayAsAny(name: String?, property: Schema<*>?) {
        if (composedArrayAsAny && property is ArraySchema && property.items is ComposedSchema) {
            LOGGER.info("Schema: $name is array of composed -> changed to array of object")
            property.items = ObjectSchema()
        }
    }

    /**
     * Sets vendor extensions to the model and it's properties. Extensions added: [markModelAsTypeAlias] and
     * [escapePropertyBaseNameLiteral].
     *
     * @param model to have extensions added
     * @since 1.1.0
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
     * @since 1.1.0
     */
    private fun markModelAsTypeAlias(model: CodegenModel, modelPropertiesCount: Int) {
        if ((!emptyDataClasses && modelPropertiesCount <= 0) || (model.isAlias && generatePrimitiveTypeAlias)) {
            LOGGER.info("Model: ${model.name} marked as typealias")
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
     * @since 1.1.0
     */
    private fun escapePropertyBaseNameLiteral(modelProperties: List<CodegenProperty>) {
        modelProperties.forEach { property ->
            property.vendorExtensions[VENDOR_EXTENSION_BASE_NAME_LITERAL] =
                property.baseName.replace("$", "\\$")
        }
    }

    /**
     * Checks if operation is Multipart or not.
     *
     * @param consumes operation consumes list
     * @return true if multipart
     * @since 1.1.0
     */
    private fun isMultipartType(consumes: List<Map<String, String>>): Boolean {
        val firstType = consumes[0]
        return "multipart/form-data" == firstType["mediaType"]
    }

}