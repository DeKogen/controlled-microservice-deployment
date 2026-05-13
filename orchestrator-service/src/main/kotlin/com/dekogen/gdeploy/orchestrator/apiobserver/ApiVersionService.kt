package com.dekogen.gdeploy.orchestrator.apiobserver

import com.dekogen.gdeploy.orchestrator.CloudService
import io.swagger.v3.oas.models.OpenAPI
import me.snowdrop.istio.client.IstioClient
import org.apache.juli.logging.LogFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiVersionService(
    private val namespace: String,
    private val apiVersionRepository: ApiVersionRepository,
    private val istioClient: IstioClient,
    private val apiCompatibilityService: ApiCompatibilityService,
    private val apiUriGenerator: ApiUriGenerator
) {
    private val log = LogFactory.getLog(this::class.java)

    fun generateClientRoutes(): List<List<VersionedServiceRouting>> {
        val points = listDestinationsByHost()

        val serverApis = points
            .map { getServerApi(it.key, it.value) }

        val clientApis = points
            .flatMap { service -> service.value.map { CloudService(service.key, it) } }
            .map { getClientApi(it) }

        return clientApis.map { routeClient(it, serverApis) }
    }

    private fun routeClient(
        client: VersionedClientApi,
        servers: List<VersionedServerApi>
    ): List<VersionedServiceRouting> {
        return client.clientApi
            .map {
                val bestServer = findBestServer(it.key, it.value, servers)
                    ?: throw IllegalStateException("Can't match dependency for service ${client.service} api ${it.key}")
                VersionedServiceRouting(client.service, bestServer)
            }
    }

    private fun getServerApi(host: String, versions: List<String>): VersionedServerApi {
        val apis = versions.associateWith { fullApiForService(CloudService(host, it), "server") }
        val serverApi = VersionedServerApi(host, apis)
        return collapseOlderVersionForSameApis(serverApi)
    }

    private fun getClientApi(service: CloudService): VersionedClientApi {
        val api = fullApiForService(service, "client")
        return VersionedClientApi(service, api)
    }

    private fun findBestServer(
        apiName: String,
        clientSpec: OpenAPI,
        servers: List<VersionedServerApi>
    ): CloudService? {
        for (server in servers) {
            val sortedVersions = server.sortedVersions()

            for (version in sortedVersions.entries) {
                val fullServiceApi = version.value
                if (apiName in fullServiceApi && apiCompatibilityService
                        .checkCompatibility(clientSpec, fullServiceApi[apiName]!!)
                ) {
                    log.debug("Compatibility check: for client $apiName server ${server.host}:${version.key} is compatible")
                    return CloudService(server.host, version.key)
                } else {
                    log.debug("Compatibility check: for client $apiName server ${server.host}:${version.key} is not compatible")
                }
            }
        }

        return null
    }

    private fun collapseOlderVersionForSameApis(api: VersionedServerApi): VersionedServerApi {
        val versions = api.sortedVersions().toList().reversed()
        val collapsedVersions = HashMap<String, FullServiceApi>()
        for (i in 0 until versions.size - 1) {
            val olderVersion = versions[i]
            val newerVersion = versions[i + 1]

            val sameApiSet = olderVersion.second.keys.toHashSet() == newerVersion.second.keys.toHashSet()
            if (sameApiSet) {
                val apiSet = olderVersion.second.keys
                for (apiName in apiSet) {
                    val apisDifferent = apiCompatibilityService.checkDifferent(
                        olderVersion.second[apiName]!!,
                        newerVersion.second[apiName]!!
                    )
                    if (apisDifferent) {
                        log.debug("Service versions with same api ${api.host} ${olderVersion.first} and ${newerVersion.first}")
                        collapsedVersions[versions[i].first] = versions[i].second
                        break
                    }
                }
            }
        }
        collapsedVersions[versions.last().first] = versions.last().second
        log.debug("Collapsed service ${api.host} server versions from ${api.hostVersions.keys} to ${collapsedVersions.keys}")

        return VersionedServerApi(api.host, collapsedVersions)
    }

    private fun fullApiForService(service: CloudService, type: String): FullServiceApi {
        return listApiForService(service, type)
            .associateWith { getOpenApiForService(service, type, it) }
    }

    private fun listApiForService(service: CloudService, type: String): List<String> {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add { request, body, execution ->
            request.headers.set("app-version", service.version)
            execution.execute(request, body)
        }

        log.debug("Querying api list for ${service.service}.$namespace ${service.version} $type")
        return restTemplate.getForObject(
            "${apiUriGenerator.apiUri(service, namespace)}/$type/",
            Array<String>::class.java
        )!!
            .asList()

    }

    private fun getOpenApiForService(service: CloudService, type: String, name: String): OpenAPI {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add { request, body, execution ->
            request.headers.set("app-version", service.version)
            execution.execute(request, body)
        }

        log.debug("Querying specification for ${service.service}.$namespace ${service.version} $type/$name")
        return restTemplate.getForObject(
            "${apiUriGenerator.apiUri(service, namespace)}/$type/$name",
            OpenAPI::class.java
        )!!
    }

    private fun listDestinationsByHost(): Map<String, List<String>> {
        return istioClient.v1beta1DestinationRule().inNamespace(namespace).list().items
            .associate { it.spec.host to it.spec.subsets.map { it.name } }
    }
}