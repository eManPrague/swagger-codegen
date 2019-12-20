package cz.eman.swagger.codegen.generator.kotlin

import org.openapitools.codegen.CliOption
import org.openapitools.codegen.CodegenConstants
import org.openapitools.codegen.CodegenType
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @since 1.0.0
 */
open class KotlinRoomCodegen : AbstractKotlinCodegen() {

    var LOGGER = LoggerFactory.getLogger(KotlinRoomCodegen::class.java)

    private var dateLib = DateLibrary.JAVA8.value

    enum class DateLibrary constructor(val value: String) {
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

        artifactId = "kotlin-room"
        packageName = "cz.eman.swagger"

        outputFolder = "generated-code" + File.separator + "kotlin-room"
        modelTemplateFiles["model.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"
        templateDir = "kotlin-room"
        embeddedTemplateDir = templateDir
        modelPackage = "$packageName.models"

        val dateLibrary = CliOption(DATE_LIBRARY, "Option. Date library to use")
        val dateOptions = HashMap<String, String>()
        dateOptions[DateLibrary.THREETENBP.value] = "Threetenbp"
        dateOptions[DateLibrary.STRING.value] = "String"
        dateOptions[DateLibrary.JAVA8.value] = "Java 8 native JSR310"
        dateOptions[DateLibrary.MILLIS.value] = "Date Time as Long"
        dateLibrary.enum = dateOptions
        cliOptions.add(dateLibrary)
    }

    override fun getModelNameSuffix(): String {
        return CLASS_SUFFIX
    }

    override fun getTag(): CodegenType {
        return CodegenType.OTHER
    }

    override fun getName(): String {
        return "kotlin-room"
    }

    override fun getHelp(): String {
        return "Generates a Kotlin Room classes."
    }

    fun setDateLibrary(library: String) {
        this.dateLib = library
    }

    override fun processOpts() {
        super.processOpts()

        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties[DATE_LIBRARY].toString())
        }

        when {
            DateLibrary.THREETENBP.value == dateLib -> {
                additionalProperties[DateLibrary.THREETENBP.value] = true
                typeMapping["date"] = "LocalDate"
                typeMapping["DateTime"] = "LocalDateTime"
                importMapping["LocalDate"] = "org.threeten.bp.LocalDate"
                importMapping["LocalDateTime"] = "org.threeten.bp.LocalDateTime"
                defaultIncludes.add("org.threeten.bp.LocalDateTime")
            }
            DateLibrary.STRING.value == dateLib -> {
                typeMapping["date-time"] = "kotlin.String"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.String"
            }
            DateLibrary.JAVA8.value == dateLib -> {
                additionalProperties[DateLibrary.JAVA8.value] = true
            }
            DateLibrary.MILLIS.value == dateLib -> {
                typeMapping["date-time"] = "kotlin.Long"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.Long"
            }
        }
    }

    companion object {
        const val DATE_LIBRARY = "dateLibrary"
        const val CLASS_SUFFIX = "Entity"
    }

}