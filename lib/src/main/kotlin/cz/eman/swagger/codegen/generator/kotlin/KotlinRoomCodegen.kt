package cz.eman.swagger.codegen.generator.kotlin

import cz.eman.swagger.codegen.generator.kotlin.KotlinRetrofitCodegen.CollectionType
import cz.eman.swagger.codegen.generator.kotlin.KotlinRetrofitCodegen.DateLibrary
import cz.eman.swagger.codegen.language.GENERATE_INFRASTRUCTURE_API
import io.swagger.v3.oas.models.media.*
import org.openapitools.codegen.*
import org.openapitools.codegen.languages.AbstractKotlinCodegen
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @since 1.0.0
 */
open class KotlinRoomCodegen : AbstractKotlinCodegen() {

    private var collectionType = CollectionType.ARRAY.value
    private var dateLib = KotlinRetrofitCodegen.DateLibrary.JAVA8.value
    private var allowEmptyDataClasses = false

    companion object {
        const val CLASS_SUFFIX = "Entity"
        const val DATE_LIBRARY = "dateLibrary"
        const val COLLECTION_TYPE = "collectionType"
        const val EMPTY_DATA_CLASS = "emptyDataClasses"
        const val EMPTY_DATA_CLASS_DESCRIPTION = "Option to allow empty data classes (default: false)"
    }

    enum class DateLibrary constructor(val value: String) {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8"),
        MILLIS("millis")
    }

    /**
     * Constructs an instance of `KotlinRoomCodegen`.
     */
    init {
        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase
        initArtifact()
        initTemplates()
        initSettings()
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

    override fun processOpts() {
        super.processOpts()
        processOptsDateLib()
        processOptsCollectionType()
        processOptsAdditional()
    }

    override fun fromModel(name: String?, schema: Schema<*>?): CodegenModel {
        emptyDataClassAsString(schema)
        return super.fromModel(name, schema)
    }

    /**
     * Initializes artifact settings such as ID, outputFolder or package name.
     *
     * @since 1.1.0
     */
    private fun initArtifact() {
        artifactId = "kotlin-room"
        packageName = "cz.eman.swagger"
        modelPackage = "$packageName.models"
        outputFolder = "generated-code" + File.separator + "kotlin-room"
    }

    /**
     * Initializes template files for generator.
     *
     * @since 1.1.0
     */
    private fun initTemplates() {
        templateDir = "kotlin-room"
        embeddedTemplateDir = templateDir
        modelTemplateFiles["model.mustache"] = ".kt"
        modelDocTemplateFiles["model_doc.mustache"] = ".md"
    }

    /**
     * Adds all settings options to this generator.
     *
     * @since 1.1.0
     */
    private fun initSettings() {
        initSettingsDateLibrary()
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
        val dateLibrary = CliOption(KotlinRetrofitCodegen.DATE_LIBRARY, "Option. Date library to use")
        val dateOptions = HashMap<String, String>()
        dateOptions[KotlinRetrofitCodegen.DateLibrary.THREETENBP.value] = "Threetenbp"
        dateOptions[KotlinRetrofitCodegen.DateLibrary.STRING.value] = "String"
        dateOptions[KotlinRetrofitCodegen.DateLibrary.JAVA8.value] = "Java 8 native JSR310"
        dateOptions[KotlinRetrofitCodegen.DateLibrary.MILLIS.value] = "Date Time as Long"
        dateLibrary.enum = dateOptions
        cliOptions.add(dateLibrary)
    }

    /**
     * Settings to change collection type that this generator supports. Types used are [CollectionType.ARRAY] and
     * [CollectionType.LIST]. Default is [CollectionType.ARRAY].
     *
     * @since 1.1.0
     */
    private fun initSettingsCollectionType() {
        val collectionTypeCli = CliOption(KotlinRetrofitCodegen.COLLECTION_TYPE, "Option. Collection type to use")
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
                KotlinRetrofitCodegen.EMPTY_DATA_CLASS,
                KotlinRetrofitCodegen.EMPTY_DATA_CLASS_DESCRIPTION,
                false
            )
        )
    }

    /**
     * Processes options for date library. Alters type mapping based on which library is used. For more information
     * see [initSettingsDateLibrary].
     *
     * @since 1.1.0
     */
    private fun processOptsDateLib() {
        if (additionalProperties.containsKey(KotlinRetrofitCodegen.DATE_LIBRARY)) {
            dateLib = additionalProperties[KotlinRetrofitCodegen.DATE_LIBRARY].toString()
        }

        when (dateLib) {
            KotlinRetrofitCodegen.DateLibrary.THREETENBP.value -> {
                additionalProperties[KotlinRetrofitCodegen.DateLibrary.THREETENBP.value] = true
                typeMapping["date"] = "LocalDate"
                typeMapping["DateTime"] = "LocalDateTime"
                importMapping["LocalDate"] = "org.threeten.bp.LocalDate"
                importMapping["LocalDateTime"] = "org.threeten.bp.LocalDateTime"
                defaultIncludes.add("org.threeten.bp.LocalDateTime")
            }
            KotlinRetrofitCodegen.DateLibrary.STRING.value -> {
                typeMapping["date-time"] = "kotlin.String"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.String"
            }
            KotlinRetrofitCodegen.DateLibrary.JAVA8.value -> additionalProperties[KotlinRetrofitCodegen.DateLibrary.JAVA8.value] = true
            KotlinRetrofitCodegen.DateLibrary.MILLIS.value -> {
                typeMapping["date-time"] = "kotlin.Long"
                typeMapping["date"] = "kotlin.String"
                typeMapping["Date"] = "kotlin.String"
                typeMapping["DateTime"] = "kotlin.Long"
            }
        }
    }

    /**
     * Processes options for collection type. Changes mapping based on which collection is selected. For more
     * information see [initSettingsCollectionType].
     *
     * @since 1.1.0
     */
    private fun processOptsCollectionType() {
        if (additionalProperties.containsKey(KotlinRetrofitCodegen.COLLECTION_TYPE)) {
            collectionType = additionalProperties[KotlinRetrofitCodegen.COLLECTION_TYPE].toString()
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
        if (additionalProperties.containsKey(KotlinRetrofitCodegen.EMPTY_DATA_CLASS)) {
            allowEmptyDataClasses = convertPropertyToBooleanAndWriteBack(KotlinRetrofitCodegen.EMPTY_DATA_CLASS)
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

}