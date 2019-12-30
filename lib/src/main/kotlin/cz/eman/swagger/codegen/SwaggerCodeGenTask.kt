package cz.eman.swagger.codegen

import org.openapitools.codegen.config.CodegenConfigurator

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 * @since 1.0.0
 */
open class SwaggerCodeGenTask : AbstractSwaggerGenTask() {

    init {
        description = "Generates code from the swagger spec and the CodegenConfigurator."
    }

    override val configuration: CodegenConfigurator by lazy { project.extensions.getByType(CodegenConfigurator::class.java) }

}