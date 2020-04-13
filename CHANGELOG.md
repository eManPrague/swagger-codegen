Change Log
==========

## 2.1.0 (TBD)

### Added
- @Path params are always first in operation.

### Changed
- Kotlin updated to [v1.3.71](https://github.com/JetBrains/kotlin/releases/tag/v1.3.71)
- OpenApi Codegen updated to [v4.3.0](https://github.com/OpenAPITools/openapi-generator/releases/tag/v4.3.0)

## 2.0.1 (2020-03-10)

### Fixed
- Fix of an error `Could not generate api file List is empty` caused if endpoint has no parameters after removing all from 
parameter `removeOperationParams` (occurred if this parameter is present).

## 2.0.0 (2020-03-09)
- Introduced a new config parameter `ignoreEndpointStartingSlash` to ignore endpoint's starting slash in generated 
Retrofit API service class. See `IgnoreStartingSlashLambda` for more details.
- Introduced a new lambda `IgnoreStartingSlashLambda` (`{{#lambda.ignoreStartingSlash}}{{paramName}}{{/lambda.ignoreStartingSlash}}`) 
which removes word *minus* in a fragment if it's in it.
- For rest of changes see all `2.0.0-xx` changelog messages.

## 2.0.0-rc03 (2020-02-18)

### Fixed
- Fix of critical bug when `removeOperationParams` is used, then api function is not generated it correctly.

## 2.0.0-rc02 (2020-02-18)

### Added
- Option to remove specific parameters from operations:
```
"removeOperationParams" to arrayOf("X-Access-Token", "Accept-Language", ...)
```

## 2.0.0-rc01 (2020-02-05)

### Changed
- OpenApi generator updated to release [v4.2.3](https://github.com/OpenAPITools/openapi-generator/releases/tag/v4.2.3)
- Enable multiple configuration files in one swagger task (see README.md).
- Tasks now contain additionalProperties setting (see README.md).
- Task configs can have all values null.
- Generator is safe with null parameters.

## 2.0.0-alpha02 (2019-01-21)

### Added
- Introduced a new lambda `RemoveMinusTextFromNameLambda` (`{{#lambda.removeMinusText}}{{paramName}}{{/lambda.removeMinusText}}`) which removes word *minus* in a fragment if it's in it.
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