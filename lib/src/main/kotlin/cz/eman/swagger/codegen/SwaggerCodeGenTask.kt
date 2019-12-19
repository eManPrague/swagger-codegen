package cz.eman.swagger.codegen

import org.openapitools.codegen.config.CodegenConfigurator

/**
 * @author eMan s.r.o. (vaclav.souhrada@eman.cz)
 */
open class SwaggerCodeGenTask : AbstractSwaggerGenTask() {

    init {
        description = "Generates code from the swagger spec and the CodegenConfigurator."
    }

    override val configuration: CodegenConfigurator by lazy { project.extensions.getByType(CodegenConfigurator::class.java) }

}