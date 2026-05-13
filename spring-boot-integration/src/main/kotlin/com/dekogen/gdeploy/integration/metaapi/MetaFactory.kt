package com.dekogen.gdeploy.integration.metaapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody as OpenApiRequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter as JavaParameter

@Component
class MetaFactory {
    fun createOpenApiForClient(clientClass: Class<*>): OpenAPI {
        val mappings = clientClass.constructors
            .flatMap { constructor -> constructor.parameterTypes.toList() }
            .distinct()

        val paths = Paths()
        mappings.forEach { mapping ->
            val rootPath = mapping.getAnnotation(RequestMapping::class.java)
                ?.firstPath()
                .orEmpty()

            mapping.methods
                .mapNotNull { method -> createOperationMapping(rootPath, method) }
                .forEach { operationMapping ->
                    val pathItem = paths[operationMapping.path] ?: PathItem().also {
                        paths.addPathItem(operationMapping.path, it)
                    }
                    operationMapping.applyTo(pathItem)
                }
        }

        return OpenAPI()
            .openapi("3.1.0")
            .info(Info().title("${clientClass.simpleName} API metadata").version("v0"))
            .paths(paths)
    }

    private fun createOperationMapping(rootPath: String, method: Method): OperationMapping? {
        val mapping = method.mappingInfo() ?: return null
        val path = joinPaths(rootPath, mapping.path)
        val operation = Operation()
            .operationId(method.name)
            .responses(ApiResponses().addApiResponse("200", responseFor(method.returnType)))

        method.parameters.forEach { parameter ->
            parameter.toOpenApiParameter(path)?.let(operation::addParametersItem)
            parameter.toOpenApiRequestBody()?.let(operation::requestBody)
        }

        return OperationMapping(mapping.httpMethod, path, operation)
    }

    private fun responseFor(returnType: Class<*>): ApiResponse {
        val response = ApiResponse().description("OK")
        if (returnType != Void.TYPE && returnType != Unit::class.java) {
            response.content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(schemaFor(returnType))
                )
            )
        }
        return response
    }

    private fun JavaParameter.toOpenApiParameter(path: String): Parameter? {
        getAnnotation(PathVariable::class.java)?.let {
            val name = it.value.ifBlank { it.name }.ifBlank { pathVariableName(path) ?: name }
            return Parameter()
                .name(name)
                .`in`("path")
                .required(true)
                .schema(schemaFor(type))
        }

        getAnnotation(RequestParam::class.java)?.let {
            val name = it.value.ifBlank { it.name }.ifBlank { name }
            return Parameter()
                .name(name)
                .`in`("query")
                .required(it.required)
                .schema(schemaFor(type))
        }

        getAnnotation(RequestHeader::class.java)?.let {
            val name = it.value.ifBlank { it.name }.ifBlank { name }
            return Parameter()
                .name(name)
                .`in`("header")
                .required(it.required)
                .schema(schemaFor(type))
        }

        return null
    }

    private fun JavaParameter.toOpenApiRequestBody(): OpenApiRequestBody? {
        getAnnotation(RequestBody::class.java) ?: return null
        return OpenApiRequestBody()
            .required(true)
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(schemaFor(type))
                )
            )
    }

    private fun schemaFor(type: Class<*>): Schema<*> {
        return when {
            type == String::class.java -> StringSchema()
            type == Boolean::class.java || type == java.lang.Boolean::class.java -> BooleanSchema()
            type == Int::class.java || type == Integer::class.java ||
                type == Long::class.java || type == java.lang.Long::class.java -> IntegerSchema()
            type == Float::class.java || type == java.lang.Float::class.java ||
                type == Double::class.java || type == java.lang.Double::class.java -> NumberSchema()
            Collection::class.java.isAssignableFrom(type) -> ArraySchema().items(ObjectSchema())
            type.isArray -> ArraySchema().items(schemaFor(type.componentType))
            else -> objectSchemaFor(type)
        }
    }

    private fun objectSchemaFor(type: Class<*>): ObjectSchema {
        val schema = ObjectSchema()
        type.declaredFields
            .filterNot { it.isSynthetic }
            .filterNot { it.name.startsWith("$") }
            .forEach { field ->
                schema.addProperties(field.name, schemaFor(field.type))
            }
        return schema
    }

    private fun Method.mappingInfo(): MethodMapping? {
        getAnnotation(GetMapping::class.java)?.let { return MethodMapping("GET", it.firstPath()) }
        getAnnotation(PostMapping::class.java)?.let { return MethodMapping("POST", it.firstPath()) }
        getAnnotation(PutMapping::class.java)?.let { return MethodMapping("PUT", it.firstPath()) }
        getAnnotation(DeleteMapping::class.java)?.let { return MethodMapping("DELETE", it.firstPath()) }
        getAnnotation(RequestMapping::class.java)?.let {
            val httpMethod = it.method.firstOrNull()?.name ?: "GET"
            return MethodMapping(httpMethod, it.firstPath())
        }
        return null
    }

    private fun RequestMapping.firstPath(): String = path.firstOrNull() ?: value.firstOrNull().orEmpty()
    private fun GetMapping.firstPath(): String = path.firstOrNull() ?: value.firstOrNull().orEmpty()
    private fun PostMapping.firstPath(): String = path.firstOrNull() ?: value.firstOrNull().orEmpty()
    private fun PutMapping.firstPath(): String = path.firstOrNull() ?: value.firstOrNull().orEmpty()
    private fun DeleteMapping.firstPath(): String = path.firstOrNull() ?: value.firstOrNull().orEmpty()

    private fun joinPaths(rootPath: String, methodPath: String): String {
        val parts = listOf(rootPath, methodPath)
            .map { it.trim('/') }
            .filter { it.isNotBlank() }
        return "/" + parts.joinToString("/")
    }

    private fun pathVariableName(path: String): String? {
        val start = path.indexOf('{')
        val end = path.indexOf('}', start + 1)
        return if (start >= 0 && end > start) path.substring(start + 1, end) else null
    }

    private data class MethodMapping(val httpMethod: String, val path: String)

    private data class OperationMapping(
        val httpMethod: String,
        val path: String,
        val operation: Operation
    ) {
        fun applyTo(pathItem: PathItem) {
            when (httpMethod) {
                "GET" -> pathItem.get(operation)
                "POST" -> pathItem.post(operation)
                "PUT" -> pathItem.put(operation)
                "DELETE" -> pathItem.delete(operation)
                else -> pathItem.get(operation)
            }
        }
    }
}
