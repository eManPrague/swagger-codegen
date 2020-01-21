Change Log
==========
## 2.0.0-alpha02 (2019-01-21)

### Added
- Introduced a new labda `RemoveMinusTextFromNameLambda` (`{{#lambda.removeMinusText}}{{paramName}}{{/lambda.removeMinusText}}`) which removes word *minus* in a fragment if it's in it.
- New property to remove `minus` word from the header's property name: `additionalProperties["removeMinusTextInHeaderProperty"] = true`. By default `false` will be used.

### Fixed
- The `modelNameSuffix` has been added twice to generated code. It was caused by migration to OpenApi (this bug has been fixed in OpenApi, so we removed our fix.)

## 2.0.0-alpha01 (2020-01-17)
- Migrated from Swagger to OpenApi generator (https://github.com/OpenAPITools/openapi-generator).
- Merged Retrofit and Room generator together.
- Empty data class is defined as a String type alias.
- Changed anonymous enum names from _* to NUMBER_*.
- Added support for Room 2 (androidx).
- Added option to change array of composed to array of object (kotlin.Any).
- Added option to generate primitive aliases.

## 1.0.0 (2019-06-25)
- Initial version