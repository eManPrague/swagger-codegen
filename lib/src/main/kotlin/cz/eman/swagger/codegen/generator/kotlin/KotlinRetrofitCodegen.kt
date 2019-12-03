package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.language.GENERATE_INFRASTRUCTURE_API
import cz.eman.swagger.codegen.language.INFRASTRUCTURE_CLI
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import org.openapitools.codegen.languages.KotlinClientCodegen
import org.slf4j.LoggerFactory
import io.swagger.v3.oas.models.media.*
import org.openapitools.codegen.*
import java.io.File

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 */
open class KotlinRetrofitCodegen : AbstractKotlinCodegen() {

    private val LOGGER = LoggerFactory.getLogger(KotlinRetrofitCodegen::class.java)
    private var collectionType = CollectionType.ARRAY.value
    private var dateLib = DateLibrary.JAVA8.value

    companion object {
        const val DATE_LIBRARY = "dateLibrary"
        const val CLASS_API_SUFFIX = "Service"
    }

    enum class DateLibrary private constructor(val value: String) {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8"),
        MILLIS("millis")
    }

    enum class CollectionType(val value: String) {
        ARRAY("array"), LIST("list");
    }

    enum class GenerateApiType private constructor(val value: String) {
        INFRASTRUCTURE("infrastructure"),
        API("api")
    }

    enum class DtoSuffix private constructor(val value: String) {
        DTO("dto"),
        DEFAULT("default")
    }

    /**
     * Constructs an instance of `KotlinClientCodegen`.
     */
    init {
        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase

        artifactId = "kotlin-retrofit-client"
        packageName = "cz.eman.swagger"

        outputFolder = "generated-code" + File.separator + "kotlin-retrofit-client"
        modelTemplateFiles["model.mustache"] = ".kt"
        apiTemplateFiles["api.mustache"] = ".kt"
        //apiTemplateFiles["!edited_api.mustache"] = ".kt"
        // TODO parameter if use api with header param or not
        //apiTemplateFiles["api_without_header.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"
        apiDocTemplateFiles["api_doc.mustache"] = ".md"
        templateDir = "kotlin-retrofit-client"
        embeddedTemplateDir = templateDir
        apiPackage = "$packageName.api"
        modelPackage = "$packageName.model"

        val dateLibrary = CliOption(DATE_LIBRARY, "Option. Date library to use")
        val dateOptions = HashMap<String, String>()
        dateOptions[DateLibrary.THREETENBP.value] = "Threetenbp"
        dateOptions[DateLibrary.STRING.value] = "String"
        dateOptions[DateLibrary.JAVA8.value] = "Java 8 native JSR310"
        dateOptions[DateLibrary.MILLIS.value] = "Date Time as Long"
        dateLibrary.enum = dateOptions
        cliOptions.add(dateLibrary)

        val infrastructureCli = CliOption(INFRASTRUCTURE_CLI, "Option to add infrastructure package")
        val infraOptions = HashMap<String, String>()
        infraOptions[GenerateApiType.INFRASTRUCTURE.value] = "Generate Infrastructure API"
        infraOptions[GenerateApiType.API.value] = "Generate API"
        infrastructureCli.enum = infraOptions
        cliOptions.add(infrastructureCli)

        val collectionType = CliOption(KotlinClientCodegen.COLLECTION_TYPE, "Option. Collection type to use")
        val collectionOptions: MutableMap<String, String> = java.util.HashMap()
        collectionOptions[KotlinClientCodegen.CollectionType.ARRAY.value] = "kotlin.Array"
        collectionOptions[KotlinClientCodegen.CollectionType.LIST.value] = "kotlin.collections.List"
        collectionType.enum = collectionOptions
        collectionType.default = this.collectionType
        cliOptions.add(collectionType)
    }

    override fun getTag(): CodegenType {
        return CodegenType.CLIENT
    }

    override fun getName(): String {
        return "kotlin-retrofit-client"
    }

    override fun getHelp(): String {
        return "Generates a Kotlin Retrofit2 classes."
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

    private fun setDateLibrary(library: String) {
        this.dateLib = library
    }

    private fun setCollectionType(collectionType: String?) {
        this.collectionType = collectionType!!
    }

    override fun toApiName(name: String?): String {
        return super.toApiName(name) + CLASS_API_SUFFIX
    }

    override fun processOpts() {
        super.processOpts()

        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties[DATE_LIBRARY].toString())
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

        supportingFiles.add(SupportingFile("README.mustache", "", "README.md"))
        supportingFiles.add(SupportingFile("build.gradle.mustache", "", "build.gradle"))
        supportingFiles.add(SupportingFile("settings.gradle.mustache", "", "settings.gradle"))

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

        if (additionalProperties.containsKey(KotlinClientCodegen.COLLECTION_TYPE)) {
            setCollectionType(additionalProperties[KotlinClientCodegen.COLLECTION_TYPE].toString())
        }

        if (KotlinClientCodegen.CollectionType.LIST.value == collectionType) {
            typeMapping["array"] = "kotlin.collections.List"
            typeMapping["list"] = "kotlin.collections.List"
            additionalProperties["isList"] = true
        }
    }

//    override fun fromModel(name: String?, schema: Schema<*>?, allDefinitions: MutableMap<String, Schema<Any>>?): CodegenModel {
//        fixEmptyDataClass(schema)
//        return super.fromModel(name, schema, allDefinitions)
//    }

    /**
     * Kotlin data classes cannot be without value. This functions adds ignore value to the class
     * to make sure it compiles. Make sure to check the schema definition.
     *
     * @param schema to be checked
     */
    private fun fixEmptyDataClass(schema: Schema<*>?) {
        schema?.let {
            if (it !is ArraySchema && it !is MapSchema && it !is ComposedSchema && (it.properties == null || it.properties.isEmpty())) {
                it.properties = java.util.HashMap<String, Schema<String>>().apply { put("ignore", StringSchema().apply { description("No values defined for this class. Please check schema definition for this class.") }) }.toMap()
            }
        }
    }
}