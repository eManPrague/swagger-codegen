package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.language.AbstractKotlinCodegen
import io.swagger.codegen.v3.CliOption
import io.swagger.codegen.v3.CodegenConstants
import io.swagger.codegen.v3.CodegenType
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


/**
 * eMan s.r.o.
 */
open class RoomCodegen() : AbstractKotlinCodegen() {


    val DATE_LIBRARY = "dateLibrary"

    val CLASS_SUFFIX = "Entity"

    var LOGGER = LoggerFactory.getLogger(RoomCodegen::class.java)

    protected var dateLib = DateLibrary.JAVA8.value

    enum class DateLibrary private constructor(val value: String) {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8"),
        MILLIS("millis")
    }

    /**
     * Constructs an instance of `KotlinClientCodegen`.
     */
    init {

        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase

        mArtifactId = "kotlin-room"
        mPackageName = "cz.eman.swagger"

        outputFolder = "generated-code" + File.separator + "kotlin-room"
        modelTemplateFiles["model.mustache"] = ".kt"
        //apiTemplateFiles["!edited_api.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"
        //apiDocTemplateFiles["api_doc.mustache"] = ".md"
        templateDir = "kotlin-room"
        embeddedTemplateDir = templateDir
        //apiPackage = mPackageName + ".apis"
        modelPackage = mPackageName + ".models"

        val dateLibrary = CliOption(DATE_LIBRARY, "Option. Date library to use")
        val dateOptions = HashMap<String, String>()
        dateOptions[DateLibrary.THREETENBP.value] = "Threetenbp"
        dateOptions[DateLibrary.STRING.value] = "String"
        dateOptions[DateLibrary.JAVA8.value] = "Java 8 native JSR310"
        dateOptions[DateLibrary.MILLIS.value] = "Date Time as Long"
        dateLibrary.enum = dateOptions
        cliOptions.add(dateLibrary)

    }

    override fun getClassSuffix(): String {
        return CLASS_SUFFIX
    }

    override fun getTag(): CodegenType {
        return CodegenType.OTHER
    }

    override fun getName(): String {
        return "kotlin-room"
    }

    override fun getHelp(): String {
        return "Generates a kotlin room classes."
    }

    override fun getDefaultTemplateDir(): String {
        return "Kotlin"
    }

    fun setDateLibrary(library: String) {
        this.dateLib = library
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

//        supportingFiles.add(SupportingFile("README.mustache", "", "README.md"))
//
//        supportingFiles.add(SupportingFile("build.gradle.mustache", "", "build.gradle"))
//        supportingFiles.add(SupportingFile("settings.gradle.mustache", "", "settings.gradle"))
//
//        val infrastructureFolder = (srcFolder + File.separator + mPackageName + File.separator + "infrastructure").replace(".", "/")
//
//        supportingFiles.add(SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/ApiAbstractions.kt.mustache", infrastructureFolder, "ApiAbstractions.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/ApiInfrastructureResponse.kt.mustache", infrastructureFolder, "ApiInfrastructureResponse.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/ApplicationDelegates.kt.mustache", infrastructureFolder, "ApplicationDelegates.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/RequestConfig.kt.mustache", infrastructureFolder, "RequestConfig.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/RequestMethod.kt.mustache", infrastructureFolder, "RequestMethod.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/ResponseExtensions.kt.mustache", infrastructureFolder, "ResponseExtensions.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/Serializer.kt.mustache", infrastructureFolder, "Serializer.kt"))
//        supportingFiles.add(SupportingFile("infrastructure/Errors.kt.mustache", infrastructureFolder, "Errors.kt"))
    }

}