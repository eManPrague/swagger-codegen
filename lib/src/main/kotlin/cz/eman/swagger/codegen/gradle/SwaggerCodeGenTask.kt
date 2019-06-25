package cz.eman.swagger.codegen.gradle

import io.swagger.codegen.v3.config.CodegenConfigurator

/**
 * eMan s.r.o.
 */
open class SwaggerCodeGenTask : AbstractSwaggerGenTask() {

    init {
        description = "Generates code from the swagger spec and the CodegenConfigurator."
    }

    override val configuration: CodegenConfigurator by lazy { project.extensions.getByType(CodegenConfigurator::class.java) }

}