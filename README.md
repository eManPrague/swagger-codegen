# OpenApi 3 Codegen / Swagger

[ ![Download](https://api.bintray.com/packages/emanprague/maven/cz.eman.swagger.codegen/images/download.svg?version=1.1.0) ](https://bintray.com/emanprague/maven/cz.eman.swagger.codegen/1.1.0/link)

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
        classpath("cz.eman.swagger:swagger-codegen:2.0.0-alpha01")
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
```Kotlin
swagger {
    setInputSpec("${project.projectDir.absolutePath}/data/api.yaml")
    setOutputDir("${project.buildDir.absolutePath}/swagger")
    setLibrary("retrofit2")
    setGeneratorName("cz.eman.swagger.codegen.generator.kotlin.KotlinRetrofitCodegen")

    val additionalProperties = HashMap<String, Any>()
    additionalProperties["templateEngine"] = "mustache"
    additionalProperties["dateLibrary"] = "millis"
    additionalProperties["enumPropertyNaming"] = "UPPERCASE"
    additionalProperties["modelNameSuffix"] = "Dto"
    additionalProperties["apiNameSuffix"] = "Service"
    additionalProperties["generateInfrastructure"] = false
    additionalProperties["emptyDataClasses"] = false
    additionalProperties["generateAliasAsModel"] = true
    additionalProperties["composedArrayAsAny"] = true
    additionalProperties["generatePrimitiveTypeAlias"] = false
    additionalProperties["apiPackage"] = "cz.mypackage.service"
    additionalProperties["modelPackage"] = "cz.mypackage.model"
    setAdditionalProperties(additionalProperties)
}

```
- `inputSpec` - specify OpenAPI yaml file
- `outputDir` - specify output directory
- `setLibrary` - sets library to generate. Can be either "retrofit2" or "room". Default is "retrofit2".
- `generatorName` - name or class of supported generator
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
    - `modelNameSuffix` - By this property you can define suffix to all model classes. E.g. `UserDto`, ...
    - `apiNameSuffix` - By this property you can define suffix to all api classes. E.g. `UserService`, ...
    - `apiPackage` - By this property you can define a package name for your service classes
    - `modelPackage` - By this property you can define a package name for your model classes

Other options can be found [here](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-maven-plugin/README.md).

If your OpenApi contains some specific objects for parsing JSON, .... You need add the Moshi dependencies

```kotlin
implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
implementation("com.squareup.moshi:moshi-adapters:1.9.2")
```