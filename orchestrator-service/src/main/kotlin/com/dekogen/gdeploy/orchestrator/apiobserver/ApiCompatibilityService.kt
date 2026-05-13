package com.dekogen.gdeploy.orchestrator.apiobserver

import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import org.openapitools.openapidiff.core.OpenApiCompare
import org.springframework.stereotype.Service

@Service
class ApiCompatibilityService {
    fun checkCompatibility(client: OpenAPI, server: OpenAPI): Boolean {
        val diff = OpenApiCompare.fromSpecifications(copyOpenApi(client), copyOpenApi(server))
        return diff.isCompatible
    }

    fun checkDifferent(a: OpenAPI, b: OpenAPI): Boolean {
        return OpenApiCompare.fromSpecifications(copyOpenApi(a), copyOpenApi(b)).isDifferent
    }

    private fun copyOpenApi(openAPI: OpenAPI): OpenAPI {
        val mapper = Json.mapper()
        val apiStr = mapper.writerFor(OpenAPI::class.java).writeValueAsString(openAPI)
        return mapper.readValue(apiStr)
    }
}