package com.dekogen.gdeploy.integration.metaapi

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Hidden
@RequestMapping("/meta/api")
class ApiMetaController(private val apiMetaHolder: ApiMetaHolder) {

    @GetMapping("client")
    fun listClients(): List<String> {
        return apiMetaHolder.clientMappings.keys.toList()
    }

    @GetMapping("client/{name}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getClient(@PathVariable name: String): OpenAPI {
        return apiMetaHolder.clientMappings[name]!!.second
    }

    @GetMapping("server")
    fun listServers(): List<String> {
        return apiMetaHolder.serverMapping.keys.toList()
    }

    @GetMapping("server/{name}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getServer(@PathVariable name: String): OpenAPI {
        return apiMetaHolder.serverMapping[name]!!.second
    }
}
