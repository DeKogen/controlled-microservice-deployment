package com.dekogen.gdeploy.orchestrator.apiobserver

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

import org.springframework.web.client.RestTemplate

internal class ApiCompatibilityServiceTest {

    private val compatibilityService = ApiCompatibilityService()

    @Test
    @Disabled("Requires access to the gd-shop.example.local demo environment")
    fun checkCompatibility() {
        val server = fetchOpenApi("http://gd-shop.example.local/product/meta/api/server/CartProductApi")
        val client = fetchOpenApi("http://gd-shop.example.local/cart/meta/api/client/CartProductApi")

        assert(compatibilityService.checkCompatibility(client, server))
    }

    @Test
    fun compatibleWhenServerProvidesRequiredEndpoint() {
        val client = productApi()
        val server = productApi()

        assertTrue(compatibilityService.checkCompatibility(client, server))
    }

    @Test
    fun incompatibleWhenServerDoesNotProvideRequiredEndpoint() {
        val client = productApi("/api/v1/product/{id}")
        val server = productApi("/api/v1/items/{id}")

        assertFalse(compatibilityService.checkCompatibility(client, server))
    }

    @Test
    fun detectsDifferentOpenApiDescriptions() {
        val first = productApi("/api/v1/product/{id}")
        val second = productApi("/api/v1/items/{id}")

        assertTrue(compatibilityService.checkDifferent(first, second))
    }

    private fun fetchOpenApi(uri: String): OpenAPI {
        return RestTemplate().getForObject(uri, OpenAPI::class.java)!!
    }

    private fun productApi(path: String = "/api/v1/product/{id}"): OpenAPI {
        val productSchema = ObjectSchema()
            .addProperties("name", StringSchema())

        val response = ApiResponse()
            .description("OK")
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(productSchema)
                )
            )

        val operation = io.swagger.v3.oas.models.Operation()
            .operationId("getProduct")
            .addParametersItem(
                Parameter()
                    .name("id")
                    .`in`("path")
                    .required(true)
                    .schema(StringSchema())
            )
            .responses(ApiResponses().addApiResponse("200", response))

        return OpenAPI()
            .openapi("3.0.1")
            .info(Info().title("demo product api").version("1.0.0"))
            .paths(Paths().addPathItem(path, PathItem().get(operation)))
    }
}
