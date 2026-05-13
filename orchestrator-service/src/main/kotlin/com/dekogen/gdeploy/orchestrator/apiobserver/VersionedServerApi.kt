package com.dekogen.gdeploy.orchestrator.apiobserver

import com.dekogen.gdeploy.orchestrator.CloudService
import io.swagger.v3.oas.models.OpenAPI

typealias FullServiceApi = Map<String, OpenAPI>

data class VersionedServerApi(
    val host: String,
    val hostVersions: Map<String, FullServiceApi>
) {
    fun sortedVersions() = hostVersions.toSortedMap(Comparator.comparing<String, Int> { it.drop(1).toInt() }.reversed())

    fun versionsDeclaringApi(name: String): List<String> {
        return hostVersions.filter { name in it.value.keys }.map { it.key }
    }
}

data class VersionedClientApi(
    val service: CloudService,
    val clientApi: FullServiceApi
)

data class VersionedServiceRouting(val source: CloudService, val destination: CloudService)
