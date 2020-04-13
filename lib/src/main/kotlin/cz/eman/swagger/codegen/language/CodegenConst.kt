package cz.eman.swagger.codegen.language

/**
 * @author eMan s.r.o. (david.sucharda@eman.cz)
 * @since 2.0.0
 */
const val GENERATE_INFRASTRUCTURE_API = "generateInfrastructure"
const val GENERATE_INFRASTRUCTURE_API_DESCRIPTION = "Option to add infrastructure package"

const val EMPTY_DATA_CLASS = "emptyDataClasses"
const val EMPTY_DATA_CLASS_DESCRIPTION = "Option to allow empty data classes (default: false)."

const val COMPOSED_ARRAY_ANY = "composedArrayAsAny"
const val COMPOSED_ARRAY_ANY_DESCRIPTION =
    "Option to cast array of composed schema (Array<OneOf...>) to array of kotlin.Any (Array<kotlin.Any)."

const val GENERATE_PRIMITIVE_TYPE_ALIAS = "generatePrimitiveTypeAlias"
const val GENERATE_PRIMITIVE_TYPE_ALIAS_DESCRIPTION = "Option to generate typealias for primitives."

const val COMPOSED_VARS_NOT_REQUIRED = "composedVarsNotRequired"
const val COMPOSED_VARS_NOT_REQUIRED_DESCRIPTION = "Option to force variables of ComposedSchema (oneOf, anyOf) to not be required. Default is false."

const val REMOVE_OPERATION_PARAMS = "removeOperationParams"
const val REMOVE_OPERATION_PARAMS_DESCRIPTION = "Option to remove specific parameters from operation. Uses base name to filter the parameters."

const val HEADER_CLI = "headerCliOptions"
const val HEADER_CLI_DESCRIPTION = "Options to generate Header"
const val REMOVE_MINUS_TEXT_FROM_HEADER = "removeMinusTextInHeaderProperty"
const val REMOVE_MINUS_TEXT_FROM_HEADER_DESCRIPTION = "Remove minus text from header's property name if it is present."

const val REMOVE_ENDPOINT_STARTING_SLASH = "ignoreEndpointStartingSlash"
const val REMOVE_ENDPOINT_STARTING_SLASH_DESCRIPTION = "Remove/Ignore a starting slash from an endpoint definition if it is present."