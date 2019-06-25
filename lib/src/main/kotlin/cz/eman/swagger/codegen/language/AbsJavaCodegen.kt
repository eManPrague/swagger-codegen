package cz.eman.gradle.swagger.languages

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Options
import io.swagger.codegen.v3.CodegenConstants
import io.swagger.codegen.v3.CodegenModel
import io.swagger.codegen.v3.CodegenProperty
import io.swagger.codegen.v3.generators.DefaultCodegenConfig
import io.swagger.codegen.v3.generators.java.JavaClientCodegen
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.util.SchemaTypeUtil
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


/**
 * TODO CLASS_DESCRIPTION
 *
 * @author david.sucharda (david.sucharda@eman.cz)
 */
abstract class AbsJavaCodegen : JavaClientCodegen() {

    private val LOGGER = LoggerFactory.getLogger(AbsJavaCodegen::class.java)

    protected var mArtifactId: String = ""
    protected var mPackageName: String = ""

    init {
        mPackageName = "cz.eman.swagger"
    }

    override fun processOpts() {
        super.processOpts()

        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_ID)) {
            this.setmArtifactId(additionalProperties[CodegenConstants.ARTIFACT_ID] as String)
        } else {
            additionalProperties[CodegenConstants.ARTIFACT_ID] = mArtifactId
        }

        modelPackage = "$mPackageName.models"
        apiPackage = "$mPackageName.api"
        invokerPackage = "$mPackageName.models"

        importMapping.remove("ApiModelProperty")
        importMapping.remove("ApiModel")
        importMapping.remove("Schema")
    }

    override fun postProcessModelProperty(model: CodegenModel?, property: CodegenProperty?) {
        super.postProcessModelProperty(model, property)

        model?.imports?.let { imp ->
            if (imp.contains("ApiModel")) {
                imp.remove("ApiModel")
            }

            if (imp.contains("ApiModelProperty")) {
                imp.remove("ApiModelProperty")
            }
        }
    }

    override fun toModelName(name: String?): String? {
        // We need to check if import-mapping has a different model for this class, so we use it
        // instead of the auto-generated one.
        if (importMapping.containsKey(name)) {
            return importMapping[name]
        }

        val sanitizedName = sanitizeName(name)

        var nameWithPrefixSuffix = sanitizedName
        if (!StringUtils.isEmpty(modelNamePrefix) && sanitizedName != "Object") {
            // add '_' so that model name can be camelized correctly
            nameWithPrefixSuffix = modelNamePrefix + "_" + nameWithPrefixSuffix
        }

        if (!StringUtils.isEmpty(modelNameSuffix) && sanitizedName != "Object") {
            // add '_' so that model name can be camelized correctly
            nameWithPrefixSuffix = nameWithPrefixSuffix + "_" + modelNameSuffix
        }

        // camelize the model name
        // phone_number => PhoneNumber
        val camelizedName = DefaultCodegenConfig.camelize(nameWithPrefixSuffix)

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(camelizedName)) {
            val modelName = "Model$camelizedName"
            LOGGER.warn("$camelizedName (reserved word) cannot be used as model name. Renamed to $modelName")
            return modelName
        }

        // model name starts with number
        if (camelizedName.matches("^\\d.*".toRegex())) {
            val modelName = "Model$camelizedName" // e.g. 200Response => Model200Response (after camelize)
            LOGGER.warn("$name (model name starts with number) cannot be used as model name. Renamed to $modelName")
            return modelName
        }

        return camelizedName
    }

    override fun fromModel(name: String?, schema: Schema<*>?, allSchemas: MutableMap<String, Schema<Any>>?): CodegenModel {
        fixSchemaType(schema)

        val model = super.fromModel(name, schema, allSchemas)

        if (model.imports.contains("ApiModel")) {
            model.imports.remove("ApiModel")
        }

        if (model.imports.contains("ApiModelProperty")) {
            model.imports.remove("ApiModelProperty")
        }

        if (model.imports.contains("Schema")) {
            model.imports.remove("Schema")
        }

        return model
    }

    /**
     * Fixes type for schema. There is an issue where type [SchemaTypeUtil.INTEGER_TYPE] with format
     * [SchemaTypeUtil.INTEGER32_FORMAT] is wrongly represented as [SchemaTypeUtil.NUMBER_TYPE].
     *
     * @since 1.2.1
     */
    private fun fixSchemaType(schema: Schema<*>?) {
        schema?.let {
            if (it is IntegerSchema && it.type == SchemaTypeUtil.NUMBER_TYPE && it.format == SchemaTypeUtil.INTEGER32_FORMAT) {
                it.type = SchemaTypeUtil.INTEGER_TYPE
            }
        }
    }

    /**
     * Add custom handlerbars.
     *
     * @since 1.2.1
     */
    override fun addHandlebarHelpers(handlebars: Handlebars) {
        super.addHandlebarHelpers(handlebars)
        handlebars.registerHelpers(HandlebarsHelpers())
    }

    private fun setmArtifactId(mArtifactId: String) {
        this.mArtifactId = mArtifactId
    }

    /**
     * Custom Handlebar helpers. This helper allow to compare two values.
     *
     * @since 1.2.1
     */
    inner class HandlebarsHelpers {
        @Throws(IOException::class)
        fun equals(obj1: Any, options: Options): CharSequence {
            val obj2 = options.param<Any>(0)
            return if (Objects.equals(obj1, obj2)) options.fn() else options.inverse()
        }
    }

}