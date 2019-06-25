# Swagger / OpenApi 3 Codegen
The Swagger codegen contains a template-driven engine to generate documentation, code for Java, Kotlin and Android such like Retrofit and Room. It is a fork of the https://github.com/swagger-api/swagger-codegen with modifications

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
        classpath("cz.eman.swagger:swagger-codegen:1.0.0")
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
    inputSpecURL = "${project.projectDir.absolutePath}/data/api.yaml"
    outputDir = "${project.buildDir.absolutePath}/swagger"
    lang = "cz.eman.swagger.codegen.generator.kotlin.KotlinRetrofitCodegen"
    additionalProperties["templateEngine"] = "mustache"
    additionalProperties["dateLibrary"] = "millis"
    additionalProperties["enumPropertyNaming"] = "UPPERCASE"
    additionalProperties["modelNameSuffix"] = "Dto"
    additionalProperties["generateInfrastructure"] = false
    additionalProperties["apiPackage"] = "cz.mypackage.service"
    additionalProperties["modelPackage"] = "cz.mypackage.model"
}

```
- `inputSpecURL` -
- `outputDir` - specify output directory
- `lang` -
- AdditionalProperties:
    - `templateEngine` - Currently this generator is supporting only `mustache`. Support of `handlebars` is in a progress. 
    - `dateLibrary` - 
    - `enumPropertyNaming` - 
    - `generateInfrastructure` -
    - `modelNameSuffix` - By this property you can define suffix to all model classes. E.g. `UserDto`, ...
    - `apiPackage` - By this property you can define a package name for your service classes
    - `modelPackage` - By this property you can define a package name for your model classes
