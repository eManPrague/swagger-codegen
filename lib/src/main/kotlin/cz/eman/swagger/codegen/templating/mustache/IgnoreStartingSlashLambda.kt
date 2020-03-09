package cz.eman.swagger.codegen.templating.mustache

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import org.openapitools.codegen.CodegenConfig
import java.io.Writer

/**
 * A lambda which removes a starting *`/`* in a fragment if it's present.
 *
 *
 * ```mustache
 * {{#lambda.ignoreStartingSlash}}{{paramName}}{{/lambda.ignoreStartingSlash}}
 * ```
 *
 * For instance if _paramName=/v1/api/nice_ so result will be _v1/api/nice_
 *
 * @author eMan s.r.o.
 * @since 2.0.0
 * @see[Mustache.Lambda]
 */
class IgnoreStartingSlashLambda(private val generator: CodegenConfig) : Mustache.Lambda {

    companion object {
        const val LAMBDA_NAME = "ignoreStartingSlash"
    }

    override fun execute(frag: Template.Fragment, out: Writer) {
        val segmentPath = frag.execute()
        if (segmentPath.startsWith("/")) {
            out.write(segmentPath.replaceFirst("/", "", true))
        } else {
            out.write(segmentPath)
        }
    }
}