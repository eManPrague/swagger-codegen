# OpenApi 3 Codegen / Swagger

[ ![Download](https://api.bintray.com/packages/emanprague/maven/cz.eman.swagger.codegen/images/download.svg?version=2.2.1) ](https://bintray.com/emanprague/maven/cz.eman.swagger.codegen/2.2.1/link)

The Swagger codegen contains a template-driven engine to generate documentation, code for Java, Kotlin and Android such like Retrofit and Room. It is a fork of the https://github.com/OpenAPITools/openapi-generator with modifications

### How to use it?

### Gradle

#### 1. Add jCenter and codegen dependency
```kotlin
buildscript {
    repositories {
        jcenter()
    }

    // Kotlin Gradle DSL
    dependencies {
        classpath("cz.eman.swagger:swagger-codegen:2.2.0")
    }
}
```

#### 2. Apply plugin in your artifact's module

```Kotlin
plugins {
    id("swagger-codegen")
}
```

#### 3. Configure Swagger Codegen plugin

##### 3.1 Retrofit - Kotlin
Basic configuration in Kotlin Gradle DSL (use an additional properties what you need for your project):
```Kotlin
configure<SwaggerCodeGenConfig> {
    sourcePath = "${project.projectDir.absolutePath}/swagger"
    outputPath = "${buildDir.absolutePath}/swagger"
    setLibrary("jvm-retrofit2")
    setGeneratorName("cz.eman.swagger.codegen.generator.kotlin.KotlinClientCodegen")

    setAdditionalProperties(
        mutableMapOf(
            "templateEngine" to "mustache",
            "dateLibrary" to "millis",
            "enumPropertyNaming" to "UPPERCASE",
            "modelNameSuffix" to "Dto",
            "apiNameSuffix" to "Service",
            "generateInfrastructure" to false,
            "emptyDataClasses" to false,
            "composedArrayAsAny" to true,
            "generateAliasAsModel" to true,
            "composedVarsNotRequired" to true,
            "removeMinusTextInHeaderProperty" to true,
            "ignoreEndpointStartingSlash" to true,
            "generatePrimitiveTypeAlias" to false,
            "arrayAsArrayList" to false,
            "apiPackage" to "cz.mypackage.service",
            "modelPackage" to "cz.mypackage.model"
            "removeOperationParams" to arrayOf("X-Access-Token", "Accept-Language", ...)
        )
    )

    configs = listOf(
        // Input file, Output directory, Library, Additional Properties
        SwaggerCodeGenTaskConfig("first.yaml", "first", "jvm-retrofit2", mapOf("apiPackage" to "cz.mypackage.first.service")),
        SwaggerCodeGenTaskConfig("second.yaml", "second", "room2", mapOf("apiPackage" to "cz.mypackage.second.service"))
    )
}

```
- `sourcePath` - specify folder where OpenAPI yaml files are saved
- `outputPath` - specify output directory
- `setLibrary` - sets library to generate. Can be either "retrofit2" or "room". Default is "retrofit2".
- `generatorName` - name or class of supported generator
- `autoHook` - enables to auto hook generation task to compileKotlin or compileJava (default: true).
- `forceJava` - forces java compilation when compileKotlin is present (default: false).
- `configs` - specify input files, output directory and library
- AdditionalProperties:
    - `templateEngine` - Currently this generator is supporting only `mustache`. Support of `handlebars` is in a progress. 
    - `dateLibrary` - By this property you can set date library used to serialize dates and times.
    - `enumPropertyNaming` - By this property you can change enum property naming style. ("camelCase", "PascalCase", "snake_case", "original", "UPPERCASE")
    - `generateInfrastructure` - By this property you can enable to generate API infrastructure.
    - `collectionType` - By this property cou can change collection type.
    - `emptyDataClasses` - By this property you can enable empty data classes being generated. (Note: it should not pass Kotlin compilation.)
    - `generateAliasAsModel` - By this property you can generate alias (array, map) as model.
    - `composedArrayAsAny` - By this property array of composed is changed to array of object (kotlin.Any).
    - `generatePrimitiveTypeAlias` - By this property aliases to primitive are also generated.
    - `composedVarsNotRequired` - By this property Composed schemas (oneOf, anyOf) will have all variables as not required (nullable).
       Can be used for schema that references object that is required to mark it as not required.
    - `modelNameSuffix` - By this property you can define suffix to all model classes. E.g. `UserDto`, ...
    - `apiNameSuffix` - By this property you can define suffix to all api classes. E.g. `UserService`, ...
    - `apiPackage` - By this property you can define a package name for your service classes
    - `modelPackage` - By this property you can define a package name for your model classes
    - `removeMinusTextInHeaderProperty` - By this property you can enable to generate name of header property without text minus if it is present.
    - `removeOperationParams` - By this property you can remove specific parameters from API operations.
    - `arrayAsArrayList` - By this property you can forcefully represent Array as ArrayList which can be useful with complex schemas. Use with caution.
    - `ignoreEndpointStartingSlash` - By this property you can ignore a starting slash from an endpoint definition if it is present

Other options can be found [here](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-maven-plugin/README.md).

If your OpenApi contains some specific objects for parsing JSON, .... You need add the Moshi dependencies

```kotlin
implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
implementation("com.squareup.moshi:moshi-adapters:1.9.2")
```