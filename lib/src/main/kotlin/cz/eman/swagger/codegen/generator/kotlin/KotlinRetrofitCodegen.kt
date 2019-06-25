package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.language.GENERATE_INFRASTRUCTURE_API
import cz.eman.swagger.codegen.language.INFRASTRUCTURE_CLI
import io.swagger.codegen.v3.CliOption
import io.swagger.codegen.v3.CodegenConstants
import io.swagger.codegen.v3.CodegenType
import io.swagger.codegen.v3.SupportingFile
import io.swagger.codegen.v3.generators.kotlin.AbstractKotlinCodegen
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.collections.HashMap

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 */
open class KotlinRetrofitCodegen() : AbstractKotlinCodegen() {

    val DATE_LIBRARY = "dateLibrary"

    val CLASS_API_SUFFIX = "Service"

    var LOGGER = LoggerFactory.getLogger(KotlinRetrofitCodegen::class.java)

    protected var dateLib = DateLibrary.JAVA8.value

    enum class DateLibrary private constructor(val value: String) {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8"),
        MILLIS("millis")
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
        apiPackage = packageName + ".api"
        modelPackage = packageName + ".model"

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
    }

    override fun getTag(): CodegenType {
        return CodegenType.OTHER
    }

    override fun getName(): String {
        return "kotlin-retrofit-client"
    }

    override fun getHelp(): String {
        return "Generates a kotlin room classes."
    }

    override fun getDefaultTemplateDir(): String {
        return "Kotlin"
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

    fun setDateLibrary(library: String) {
        this.dateLib = library
    }

    override fun toApiName(name: String?): String {
        return super.toApiName(name) + CLASS_API_SUFFIX
    }

    override fun processOpts() {
        super.processOpts()

        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties[DATE_LIBRARY].toString())
        }

        if (DateLibrary.THREETENBP.value == dateLib) {
            additionalProperties[DateLibrary.THREETENBP.value] = true
            typeMapping["date"] = "LocalDate"
            typeMapping["DateTime"] = "LocalDateTime"
            importMapping["LocalDate"] = "org.threeten.bp.LocalDate"
            importMapping["LocalDateTime"] = "org.threeten.bp.LocalDateTime"
            defaultIncludes.add("org.threeten.bp.LocalDateTime")
        } else if (DateLibrary.STRING.value == dateLib) {
            typeMapping["date-time"] = "kotlin.String"
            typeMapping["date"] = "kotlin.String"
            typeMapping["Date"] = "kotlin.String"
            typeMapping["DateTime"] = "kotlin.String"
        } else if (DateLibrary.JAVA8.value == dateLib) {
            additionalProperties[DateLibrary.JAVA8.value] = true
        } else if (DateLibrary.MILLIS.value == dateLib) {
            typeMapping["date-time"] = "kotlin.Long"
            typeMapping["date"] = "kotlin.String"
            typeMapping["Date"] = "kotlin.String"
            typeMapping["DateTime"] = "kotlin.Long"
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
    }

}