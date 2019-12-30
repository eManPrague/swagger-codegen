package cz.eman.swagger.codegen.language

/**
 * @author eMan s.r.o. (david.sucharda@eman.cz)
 * @since 1.1.0
 */
const val DATE_LIBRARY = "dateLibrary"
const val DATE_LIBRARY_DESCRIPTION = "Option to change Date library to use (default: java8)."
const val GENERATE_INFRASTRUCTURE_API = "generateInfrastructure"
const val GENERATE_INFRASTRUCTURE_API_DESCRIPTION = "Option to add infrastructure package"
const val COLLECTION_TYPE = "collectionType"
const val COLLECTION_TYPE_DESCRIPTION = "Option to change Collection type to use (default: array)."
const val EMPTY_DATA_CLASS = "emptyDataClasses"
const val EMPTY_DATA_CLASS_DESCRIPTION = "Option to allow empty data classes (default: false)."
const val COMPOSED_ARRAY_ANY = "composedArrayAsAny"
const val COMPOSED_ARRAY_ANY_DESCRIPTION =
    "Option to cast array of composed schema (Array<OneOf...>) to array of kotlin.Any (Array<kotlin.Any)."
const val GENERATE_PRIMITIVE_TYPE_ALIAS = "generatePrimitiveTypeAlias"
const val GENERATE_PRIMITIVE_TYPE_ALIAS_DESCRIPTION = "Option to generate typealias for primitives."