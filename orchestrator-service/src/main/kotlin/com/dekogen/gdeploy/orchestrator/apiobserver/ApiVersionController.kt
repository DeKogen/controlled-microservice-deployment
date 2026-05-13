package com.dekogen.gdeploy.orchestrator.apiobserver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/routing")
class ApiVersionController(
    private val apiVersionService: ApiVersionService
) {
    @GetMapping
    fun list(): List<List<VersionedServiceRouting>> {
        return apiVersionService.generateClientRoutes()
    }
}