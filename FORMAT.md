# OpenApi Codegen eMan specs

[eMan generator](https://github.com/eManPrague/swagger-codegen) is a fork of [OpenApiCodegen](https://github.com/OpenAPITools/openapi-generator)  
Specification used: [Open API 3.0.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md)+

## Required in OpenApi specification

### Set format to all data types

All [data types](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#data-types) should contain **format** (when possible). If the format is not set then the generator creates a class wrapping multiple formats (int32 + int64).

```yaml
property:
  type: integer
  format: int32
```

### Set nullability

If API can return property with **null** value then mark it as [nullable](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaNullable). When it is not marked as nullable and server returns null (`nonNullProperty = null`) then it might result with an exception later in the code (depending on JSON parser being used). This should not be the case for primitive types.

```yaml
property:
  type: object
  nullable: true
```

This can be caused by parser using [Unsafe](http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/) class that might set null value to non-null fields.

## Recommended

### Set default values

Default values for [properties](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#properties) provide more information to the generator and developer. Not all JSON parsers use default values but it is useful for developers to know what value property defaults to. This value can also be easily used to test the code.

```yaml
property:
  type: integer
  format: int32
  default: 5
```

Note: setting default value does not make property non-nullable!

### Avoid using reference in reference

Using reference in reference is usually problematic with array schemas. Having array in array (in array ...) is not handled properly by the generator.

```yaml
value:
  type: array
  minItems: 4
  maxItems: 4
  items:
    type: integer
    format: int32

values:
  type: array
  minItems: 0
  items:
    $ref: "#/components/schemas/value"

envelope:
  type: Object
  properties:
    data:
    $ref: "#/components/schemas/values"
```

This example would generate Envelope class with `val data: kotlin.Array<kotlin.Array>` property which would not compile because the second array has no type (kotlin.Array<Int>). Proper generated code should be `data: koltin.Array<Int>` but for that we would need to **remove values array schema** (thus removing the reference in reference).

### No oneOf, anyOf or allOf

In this case the specification is saying that one property or response can be of mutliple types. There are cases in which this makes sense and is completely fine but the generator usually generates unusable or confusing code.

```yaml
itemsArray:
  type: array
  minItems: 5
  maxItems: 6
  items:
    oneOf:
    - type: number
    - type: string
    - type: object
```

This example would represent all item types using one wrapping class `OneOfLessThanNumberCommaStringCommaObjectGreaterThan` containing all supported types. That on it's own is not an issue but this class is usually not linked or generated properly and it does not pass Kotlin compilation.

In this case it is better to allow the developer to decide the type in their own code by representing items as a generic object. Which can be done in two ways:

1. Allow the generator to represent all items as generic object by setting `additionalProperties["composedArrayAsAny"] = true`. All items in array (using oneOf, anyOf or allOf) will be represented as `kotlin.Any`.
2. Modify the specification to contain only object items. Describe/link the containing items (schemas) in the description or example. Modified specification would look like this:

```yaml
itemsArray:
  type: array
  description: Contains oneOf: number, string or object.
  minItems: 5
  maxItems: 6
  items:
    type: object
  example:
    - 1
    - "String"
```

### Provide descriptions and examples

It is probably obvious but it is very useful to provide descriptions and examples to the schemas. These are then cloned to the description of the classes and are visible in the code to provide more information without the need to look everything up. It also removes the possibility of misunderstanding the purpose of the schema and usage.
