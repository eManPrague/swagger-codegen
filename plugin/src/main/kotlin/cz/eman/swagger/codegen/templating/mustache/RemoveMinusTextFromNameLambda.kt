package cz.eman.swagger.codegen.templating.mustache

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import org.openapitools.codegen.CodegenConfig
import java.io.Writer

/**
 * A lambda which removes word *minus* in a fragment if it's in it.
 *
 *
 * ```mustache
 * {{#lambda.removeMinusText}}{{paramName}}{{/lambda.removeMinusText}}
 * ```
 *
 * For instance if _paramName=dogMinusCat_ so result will be _dogCat_
 *
 * @author eMan a.s. (info@eman.cz)
 * @since 2.0.0
 * @see[Mustache.Lambda]
 */
class RemoveMinusTextFromNameLambda(private val generator: CodegenConfig) : Mustache.Lambda {

    companion object {
        const val LAMBDA_NAME = "removeMinusText"
    }

    override fun execute(frag: Template.Fragment, out: Writer) {
        out.write(frag.execute().replace("minus", "", true))
    }
}