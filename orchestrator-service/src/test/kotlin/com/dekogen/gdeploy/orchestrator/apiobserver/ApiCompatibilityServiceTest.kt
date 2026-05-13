package com.dekogen.gdeploy.orchestrator.apiobserver

import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.springframework.web.client.RestTemplate

internal class ApiCompatibilityServiceTest {

    @Test
    @Disabled("Requires access to the gd-shop.example.local demo environment")
    fun checkCompatibility() {
        val server = fetchOpenApi("http://gd-shop.example.local/product/meta/api/server/CartProductApi")
        val client = fetchOpenApi("http://gd-shop.example.local/cart/meta/api/client/CartProductApi")

        assert(ApiCompatibilityService().checkCompatibility(client, server))
    }

    @Test
    fun checkDifferent() {
    }

    private fun fetchOpenApi(uri: String): OpenAPI {
        return RestTemplate().getForObject(uri, OpenAPI::class.java)!!
    }
}
