package cz.eman.swagger.codegen.language

import io.swagger.codegen.v3.*
import io.swagger.codegen.v3.generators.DefaultCodegenConfig
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * @author eMan (vaclav.souhrada@eman.cz
 */
@Deprecated("We are using again an original DefaultCodegenConfig from the Swagger codegen library")
abstract class AbstractKotlinCodegen : DefaultCodegenConfig(), CodegenConfig {

    protected var mArtifactId: String = ""
    protected var mArtifactVersion = "1.0.0"
    protected var mGroupId = "io.swagger"
    protected var mPackageName: String = ""

    protected var srcFolder = "src/main/kotlin"

    protected var apiDocPath = "docs/"
    protected var modelDocPath = "docs/"

    var enumPropertyNaming: CodegenConstants.ENUM_PROPERTY_NAMING_TYPE = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase
        protected set

    init {
        supportsInheritance = true

        languageSpecificPrimitives = HashSet(Arrays.asList(
                "kotlin.Any",
                "kotlin.Byte",
                "kotlin.Short",
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Boolean",
                "kotlin.Char",
                "kotlin.String",
                "kotlin.Array",
                "kotlin.collections.List",
                "kotlin.collections.Map",
                "kotlin.collections.Set"
        ))

        // this includes hard reserved words defined by https://github.com/JetBrains/kotlin/blob/master/core/descriptors/src/org/jetbrains/kotlin/renderer/KeywordStringsGenerated.java
        // as well as keywords from https://kotlinlang.org/docs/reference/keyword-reference.html
        reservedWords = HashSet(Arrays.asList(
                "abstract",
                "annotation",
                "as",
                "break",
                "case",
                "catch",
                "class",
                "companion",
                "const",
                "constructor",
                "continue",
                "crossinline",
                "data",
                "delegate",
                "do",
                "else",
                "enum",
                "external",
                "false",
                "final",
                "finally",
                "for",
                "fun",
                "if",
                "in",
                "infix",
                "init",
                "inline",
                "inner",
                "interface",
                "internal",
                "is",
                "it",
                "lateinit",
                "lazy",
                "noinline",
                "null",
                "object",
                "open",
                "operator",
                "out",
                "override",
                "package",
                "private",
                "protected",
                "public",
                "reified",
                "return",
                "sealed",
                "super",
                "suspend",
                "tailrec",
                "this",
                "throw",
                "true",
                "try",
                "typealias",
                "typeof",
                "val",
                "var",
                "vararg",
                "when",
                "while"
        ))

        defaultIncludes = HashSet(Arrays.asList(
                "kotlin.Byte",
                "kotlin.Short",
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Boolean",
                "kotlin.Char",
                "kotlin.Array",
                "kotlin.collections.List",
                "kotlin.collections.Set",
                "kotlin.collections.Map"
        ))

        typeMapping = HashMap()
        typeMapping["string"] = "kotlin.String"
        typeMapping["boolean"] = "kotlin.Boolean"
        typeMapping["integer"] = "kotlin.Int"
        typeMapping["float"] = "kotlin.Float"
        typeMapping["long"] = "kotlin.Long"
        typeMapping["double"] = "kotlin.Double"
        typeMapping["number"] = "java.math.BigDecimal"
        typeMapping["date-time"] = "java.time.LocalDateTime"
        typeMapping["date"] = "java.time.LocalDateTime"
        typeMapping["file"] = "java.io.File"
        typeMapping["array"] = "kotlin.Array"
        typeMapping["list"] = "kotlin.Array"
        typeMapping["map"] = "kotlin.collections.Map"
        typeMapping["object"] = "kotlin.Any"
        typeMapping["binary"] = "kotlin.Array<kotlin.Byte>"
        typeMapping["Date"] = "java.time.LocalDateTime"
        typeMapping["DateTime"] = "java.time.LocalDateTime"

        instantiationTypes["array"] = "arrayOf"
        instantiationTypes["list"] = "arrayOf"
        instantiationTypes["map"] = "mapOf"

        importMapping = HashMap()
        importMapping["BigDecimal"] = "java.math.BigDecimal"
        importMapping["UUID"] = "java.util.UUID"
        importMapping["File"] = "java.io.File"
        importMapping["Date"] = "java.util.Date"
        importMapping["Timestamp"] = "java.sql.Timestamp"
        importMapping["DateTime"] = "java.time.LocalDateTime"
        importMapping["LocalDateTime"] = "java.time.LocalDateTime"
        importMapping["LocalDate"] = "java.time.LocalDate"
        importMapping["LocalTime"] = "java.time.LocalTime"

        specialCharReplacements[";"] = "Semicolon"

        cliOptions.clear()

        //        addOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC, srcFolder);
        //        addOption(CodegenConstants.PACKAGE_NAME, "Generated artifact package name (e.g. io.swagger).", mPackageName);
        //        addOption(CodegenConstants.GROUP_ID, "Generated artifact package's organization (i.e. maven mGroupId).", mGroupId);
        //        addOption(CodegenConstants.ARTIFACT_ID, "Generated artifact id (name of jar).", mArtifactId);
        //        addOption(CodegenConstants.ARTIFACT_VERSION, "Generated artifact's package version.", mArtifactVersion);

        val enumPropertyNamingOpt = CliOption(CodegenConstants.ENUM_PROPERTY_NAMING, CodegenConstants.ENUM_PROPERTY_NAMING_DESC)
        cliOptions.add(enumPropertyNamingOpt.defaultValue(enumPropertyNaming.name))


    }

    override fun apiDocFileFolder(): String {
        return "$outputFolder/$apiDocPath".replace('/', File.separatorChar)
    }

    override fun apiFileFolder(): String {
        return outputFolder + File.separator + srcFolder + File.separator + apiPackage().replace('.', File.separatorChar)
    }

    override fun escapeQuotationMark(input: String): String {
        // remove " to avoid code injection
        return input.replace("\"", "")
    }

    override fun escapeReservedWord(name: String?): String {
        // TODO: Allow enum escaping as an option (e.g. backticks vs append/prepend underscore vs match model property escaping).
        return String.format("`%s`", name)
    }

    override fun escapeUnsafeCharacters(input: String): String {
        return input.replace("*/", "*_/").replace("/*", "/_*")
    }

    /**
     * Sets the naming convention for Kotlin enum properties
     *
     * @param enumPropertyNamingType The string representation of the naming convention, as defined by [CodegenConstants.ENUM_PROPERTY_NAMING_TYPE]
     */
    fun setEnumPropertyNaming(enumPropertyNamingType: String) {
        try {
            this.enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.valueOf(enumPropertyNamingType)
        } catch (ex: IllegalArgumentException) {
            val sb = StringBuilder("$enumPropertyNamingType is an invalid enum property naming option. Please choose from:")
            for (t in CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.values()) {
                sb.append("\n  ").append(t.name)
            }
            throw RuntimeException(sb.toString())
        }

    }

    /**
     * returns the swagger type for the property
     *
     * @param p Swagger property object
     * @return string presentation of the type
     */
    override fun getSchemaType(p: Schema<*>): String? {
        val swaggerType = super.getSchemaType(p)
        if(swaggerType == null) {
            return swaggerType
        }

        val type: String
        // This maps, for example, long -> kotlin.Long based on hashes in this type's constructor
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping[swaggerType]!!
            if (languageSpecificPrimitives.contains(type)) {
                return toModelName(type)
            }
        } else {
            type = swaggerType
        }
        return toModelName(type)
    }

    /**
     * Output the type declaration of the property
     *
     * @param p Swagger Property object
     * @return a string presentation of the property type
     */
    override fun getTypeDeclaration(p: Schema<*>): String? {
        if (p is ArraySchema) {
            return getArrayTypeDeclaration(p)
        } else if (p is MapSchema) {
            val inner = p.additionalProperties as Schema<*>

            // Maps will be keyed only by primitive Kotlin string
            return getSchemaType(p) + "<kotlin.String, " + getTypeDeclaration(inner) + ">"
        }
        return super.getTypeDeclaration(p)
    }

    override fun modelDocFileFolder(): String {
        return "$outputFolder/$modelDocPath".replace('/', File.separatorChar)
    }

    override fun modelFileFolder(): String {
        return outputFolder + File.separator + srcFolder + File.separator + modelPackage().replace('.', File.separatorChar)
    }

    override fun postProcessModels(objs: Map<String, Any>): Map<String, Any> {
        return postProcessModelsEnum(super.postProcessModels(objs))
    }

    override fun postProcessOperations(objs: Map<String, Any>): Map<String, Any> {
        super.postProcessOperations(objs)

        val operations = objs["operations"] as Map<String, Any>?
        if (operations != null) {
            val ops = operations["operation"] as List<CodegenOperation>
            for (operation in ops) {
                if (operation.path.isNotEmpty() && operation.path.startsWith("/"))
                    operation.path = operation.path.substring(1)
            }
        }

        return objs
    }

    override fun processOpts() {
        super.processOpts()

        if (additionalProperties.containsKey(CodegenConstants.ENUM_PROPERTY_NAMING)) {
            setEnumPropertyNaming(additionalProperties[CodegenConstants.ENUM_PROPERTY_NAMING] as String)
        }

        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            this.setsrcFolder(additionalProperties[CodegenConstants.SOURCE_FOLDER] as String)
        } else {
            additionalProperties[CodegenConstants.SOURCE_FOLDER] = srcFolder
        }

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            this.setmPackageName(additionalProperties[CodegenConstants.PACKAGE_NAME] as String)
            if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE))
                this.setModelPackage("$mPackageName.models")
            if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE))
                this.setApiPackage("$mPackageName.apis")
        } else {
            additionalProperties[CodegenConstants.PACKAGE_NAME] = mPackageName
        }

        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_ID)) {
            this.setmArtifactId(additionalProperties[CodegenConstants.ARTIFACT_ID] as String)
        } else {
            additionalProperties[CodegenConstants.ARTIFACT_ID] = mArtifactId
        }

        if (additionalProperties.containsKey(CodegenConstants.GROUP_ID)) {
            this.setmGroupId(additionalProperties[CodegenConstants.GROUP_ID] as String)
        } else {
            additionalProperties[CodegenConstants.GROUP_ID] = mGroupId
        }

        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_VERSION)) {
            this.setmArtifactVersion(additionalProperties[CodegenConstants.ARTIFACT_VERSION] as String)
        } else {
            additionalProperties[CodegenConstants.ARTIFACT_VERSION] = mArtifactVersion
        }

        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            LOGGER.warn(CodegenConstants.INVOKER_PACKAGE + " with " + this.name + " generator is ignored. Use " + CodegenConstants.PACKAGE_NAME + ".")
        }

        additionalProperties[CodegenConstants.API_PACKAGE] = apiPackage()
        additionalProperties[CodegenConstants.MODEL_PACKAGE] = modelPackage()

        additionalProperties["apiDocPath"] = apiDocPath
        additionalProperties["modelDocPath"] = modelDocPath


    }

    fun setmArtifactId(mArtifactId: String) {
        this.mArtifactId = mArtifactId
    }

    fun setmArtifactVersion(mArtifactVersion: String) {
        this.mArtifactVersion = mArtifactVersion
    }

    fun setmGroupId(mGroupId: String) {
        this.mGroupId = mGroupId
    }

    fun setmPackageName(mPackageName: String) {
        this.mPackageName = mPackageName
    }

    fun setsrcFolder(srcFolder: String) {
        this.srcFolder = srcFolder
    }

    /**
     * Return the sanitized variable name for enum
     *
     * @param value    enum variable name
     * @param datatype data type
     * @return the sanitized variable name for enum
     */
    override fun toEnumVarName(value: String, datatype: String?): String {
        var modified: String
        if (value.length == 0) {
            modified = "EMPTY"
        } else {
            modified = value
            modified = sanitizeKotlinSpecificNames(modified)
        }

        when (enumPropertyNaming) {
            CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.original ->
                // NOTE: This is provided as a last-case allowance, but will still result in reserved words being escaped.
                modified = value
            CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase ->
                // NOTE: Removes hyphens and underscores
                modified = DefaultCodegenConfig.camelize(modified, true)
            CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.PascalCase -> {
                // NOTE: Removes hyphens and underscores
                val result = DefaultCodegenConfig.camelize(modified)
                modified = titleCase(result)
            }
            CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.snake_case ->
                // NOTE: Removes hyphens
                modified = DefaultCodegenConfig.underscore(modified)
            CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.UPPERCASE -> modified = modified.toUpperCase()
        }

        return if (reservedWords.contains(modified)) {
            escapeReservedWord(modified)
        } else modified

    }

    override fun toInstantiationType(p: Schema<*>?): String? {
        return if (p is ArraySchema) {
            getArrayTypeDeclaration((p as ArraySchema?)!!)
        } else super.toInstantiationType(p)
    }

    /**
     * Return the fully-qualified "Model" name for import
     *
     * @param name the name of the "Model"
     * @return the fully-qualified "Model" name for import
     */
    override fun toModelImport(name: String): String {
        // toModelImport is called while processing operations, but DefaultCodegen doesn't
        // define imports correctly with fully qualified primitives and models as defined in this generator.
        return if (needToImport(name)) {
            super.toModelImport(name)
        } else name

    }

    /**
     * Output the proper model name (capitalized).
     * In case the name belongs to the TypeSystem it won't be renamed.
     *
     * @param name the name of the model
     * @return capitalized model name
     */
    override fun toModelName(name: String?): String {
        // Allow for explicitly configured kotlin.* and java.* types
        if (name!!.startsWith("kotlin.") || name.startsWith("java.")) {
            return name
        }

        // If importMapping contains name, assume this is a legitimate model name.
        if (importMapping.containsKey(name)) {
            return importMapping[name]!!
        }

        var modifiedName = name.replace("\\.".toRegex(), "")
        modifiedName = sanitizeKotlinSpecificNames(modifiedName)

        // Camelize name of nested properties
        modifiedName = DefaultCodegenConfig.camelize(modifiedName)

        if (reservedWords.contains(modifiedName)) {
            modifiedName = escapeReservedWord(modifiedName)
        }

        return titleCase(modifiedName) + getClassSuffix()
    }

    open fun getClassSuffix(): String {
        return ""
    }

    override fun toModelFilename(name: String): String {
        // Should be the same as the model name
        return toModelName(name)
    }

    /**
     * Provides a strongly typed declaration for simple arrays of some type and arrays of arrays of some type.
     *
     * @param arr
     * @return
     */
    private fun getArrayTypeDeclaration(arr: ArraySchema): String? {
        // TODO: collection type here should be fully qualified namespace to avoid model conflicts
        // This supports arrays of arrays.
        val arrayType = typeMapping["array"]
        val instantiationType = StringBuilder(arrayType)
        val items = arr.items
        if(items == null) {
            return null;
        }
        val nestedType = getTypeDeclaration(items)
        // TODO: We may want to differentiate here between generics and primitive arrays.
        instantiationType.append("<").append(nestedType).append(">")
        return instantiationType.toString()
    }

    /**
     * Sanitize against Kotlin specific naming conventions, which may differ from those required by [DefaultCodegen.sanitizeName].
     *
     * @param name string to be sanitize
     * @return sanitized string
     */
    private fun sanitizeKotlinSpecificNames(name: String): String {
        var word = name
        for ((key, value) in specialCharReplacements) {
            // Underscore is the only special character we'll allow
            if (key != "_") {
                word = word.replace("\\Q$key\\E".toRegex(), value)
            }
        }

        // Fallback, replace unknowns with underscore.
        word = word.replace("\\W+".toRegex(), "_")
        if (word.matches("\\d.*".toRegex())) {
            word = "_$word"
        }

        // _, __, and ___ are reserved in Kotlin. Treat all names with only underscores consistently, regardless of count.
        if (word.matches("^_*$".toRegex())) {
            word = word.replace("\\Q_\\E".toRegex(), "Underscore")
        }

        return word
    }

    private fun titleCase(input: String): String {
        return input.substring(0, 1).toUpperCase() + input.substring(1)
    }

    override fun isReservedWord(word: String?): Boolean {
        // We want case-sensitive escaping, to avoid unnecessary backtick-escaping.
        return reservedWords.contains(word)
    }

    /**
     * Check the type to see if it needs import the library/module/package
     *
     * @param type name of the type
     * @return true if the library/module/package of the corresponding type needs to be imported
     */
    override fun needToImport(type: String): Boolean {
        // provides extra protection against improperly trying to import language primitives and java types
        val imports = !type.startsWith("kotlin.") && !type.startsWith("java.") && !defaultIncludes.contains(type) && !languageSpecificPrimitives.contains(type) && !type.equals("arrayOf")
        return imports
    }

    companion object {
        internal var LOGGER = LoggerFactory.getLogger(AbstractKotlinCodegen::class.java)
    }
}