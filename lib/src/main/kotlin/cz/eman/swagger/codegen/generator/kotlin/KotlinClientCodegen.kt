package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.language.GENERATE_INFRASTRUCTURE_API
import cz.eman.swagger.codegen.language.INFRASTRUCTURE_CLI
import io.swagger.v3.oas.models.media.*
import org.openapitools.codegen.*
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import java.io.File
import java.util.stream.Stream

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @author eMan s.r.o. (david.sucharda@eman.cz)
 */
open class KotlinClientCodegen : AbstractKotlinCodegen() {

    private var collectionType = CollectionType.ARRAY.value
    private var dateLib = DateLibrary.JAVA8.value
    private var allowEmptyDataClasses = false
    private val numberDataTypes = arrayOf("kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double")

    companion object {
        const val RETROFIT2 = "retrofit2"
        const val ROOM = "room"
        const val ROOM2 = "room2"

        const val CLASS_API_SUFFIX = "Service"
        const val DATE_LIBRARY = "dateLibrary"
        const val DATE_LIBRARY_DESCRIPTION = "Option to change Date library to use (default: java8)."
        const val COLLECTION_TYPE = "collectionType"
        const val COLLECTION_TYPE_DESCRIPTION = "Option to change Collection type to use (default: array)."
        const val EMPTY_DATA_CLASS = "emptyDataClasses"
        const val EMPTY_DATA_CLASS_DESCRIPTION = "Option to allow empty data classes (default: false)."

        const val VENDOR_EXTENSION_BASE_NAME_LITERAL = "x-base-name-literal"
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

    enum class DtoSuffix constructor(val value: String) {
        DTO("dto"),
        DEFAULT("default")
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
        return super.toApiName(name) + CLASS_API_SUFFIX
    }

    override fun processOpts() {
        super.processOpts()
        processOptsDateLib()
        processOptsInfrastructure()
        processOptsCollectionType()
        processOptsAdditional()
    }

    override fun fromModel(name: String?, schema: Schema<*>?): CodegenModel {
        emptyDataClassAsString(schema)
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

    override fun postProcessModels(objs: Map<String?, Any?>): Map<String?, Any?>? {
        val objects = super.postProcessModels(objs)
        val models = objs["models"] as List<*>? ?: emptyList<Any>()
        for (model in models) {
            val mo = model as Map<*, *>
            (mo["model"] as CodegenModel?)?.let {
                // escape the variable base name for use as a string literal
                Stream.of(
                    it.vars,
                    it.allVars,
                    it.optionalVars,
                    it.requiredVars,
                    it.readOnlyVars,
                    it.readWriteVars,
                    it.parentVars
                ).flatMap { obj: List<CodegenProperty> -> obj.stream() }.forEach { property ->
                    property.vendorExtensions[VENDOR_EXTENSION_BASE_NAME_LITERAL] =
                        property.baseName.replace("$", "\\$")
                }
            }
        }
        return objects
    }

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
        // TODO parameter if use api with header param or not
        //apiTemplateFiles["api_without_header.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"

        if (library != ROOM && library != ROOM2) {
            apiTemplateFiles["api.mustache"] = ".kt"
            apiDocTemplateFiles["api_doc.mustache"] = ".md"
        } else {
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
        val infrastructureCli = CliOption(INFRASTRUCTURE_CLI, "Option to add infrastructure package")
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
     * Initializes supported libraries for this generator. For now there is only [RETROFIT2] library supported.
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
            generateInfrastructure = additionalProperties[GENERATE_INFRASTRUCTURE_API].toString() == "true"
        }

        if (generateInfrastructure) {
            val infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/")
            //supportingFiles.add(SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"))
            supportingFiles.add(SupportingFile("infrastructure/ApiAbstractions.kt.mustache", infrastructureFolder, "ApiAbstractions.kt"))
            supportingFiles.add(SupportingFile("infrastructure/ApiInfrastructureResponse.kt.mustache", infrastructureFolder, "ApiInfrastructureResponse.kt"))
            supportingFiles.add(SupportingFile("infrastructure/ApplicationDelegates.kt.mustache", infrastructureFolder, "ApplicationDelegates.kt"))
            supportingFiles.add(SupportingFile("infrastructure/RequestConfig.kt.mustache", infrastructureFolder, "RequestConfig.kt"))
            supportingFiles.add(SupportingFile("infrastructure/RequestMethod.kt.mustache", infrastructureFolder, "RequestMethod.kt"))
            supportingFiles.add(SupportingFile("infrastructure/ResponseExtensions.kt.mustache", infrastructureFolder, "ResponseExtensions.kt"))
            supportingFiles.add(SupportingFile("infrastructure/Serializer.kt.mustache", infrastructureFolder, "Serializer.kt"))
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
     * Processes options for additional settings like allowing empty data classes and adds additional supporting files.
     *
     * @since 1.1.0
     */
    private fun processOptsAdditional() {
        if (additionalProperties.containsKey(EMPTY_DATA_CLASS)) {
            allowEmptyDataClasses = convertPropertyToBooleanAndWriteBack(EMPTY_DATA_CLASS)
        }

        supportingFiles.add(SupportingFile("README.mustache", "", "README.md"))
        supportingFiles.add(SupportingFile("build.gradle.mustache", "", "build.gradle"))
        supportingFiles.add(SupportingFile("settings.gradle.mustache", "", "settings.gradle"))
    }

    /**
     * Changes schemas (excluding [ArraySchema], [MapSchema] and [ComposedSchema]) that do not have set type and do not
     * contain any properties. These schemas are set as [String] so they are parsed as a [StringSchema] instead of
     * [ObjectSchema]. This helps to avoid empty data classes which cannot pass Kotlin compilation.
     *
     * @param schema to be checked for empty type and properties
     * @since 1.1.0
     */
    private fun emptyDataClassAsString(schema: Schema<*>?) {
        if (!allowEmptyDataClasses) {
            schema?.let {
                if (it !is ArraySchema && it !is MapSchema && it !is ComposedSchema
                    && (it.type == null || it.type.isEmpty())
                    && (it.properties == null || it.properties.isEmpty())
                ) {
                    it.type = "string"
                }
            }
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