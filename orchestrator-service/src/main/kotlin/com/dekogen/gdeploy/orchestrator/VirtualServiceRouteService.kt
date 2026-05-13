package com.dekogen.gdeploy.orchestrator

import me.snowdrop.istio.api.networking.v1beta1.*
import me.snowdrop.istio.client.IstioClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VirtualServiceRouteService(
    private val namespace: String,
    private val istioClient: IstioClient
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun routeAll(): List<VirtualService> {
        return istioClient
            .v1beta1VirtualService()
            .inNamespace(namespace)
            .list().items
            .mapNotNull { route(it) }
    }

    fun route(service: VirtualService): VirtualService? {
        routeService(service)?.let {
            return istioClient.v1beta1VirtualService().inNamespace(it.metadata.namespace).createOrReplace(it)
        }
        return null
    }

    private fun routeService(service: VirtualService): VirtualService? {
        if (isBorderService(service)) {
            log.debug("${service.resName()} Not routing as border service")
            return null
        }

        val destinations = listDestinationsByHost()

        val defaultHost = findDefaultHost(service)
        val hostDestinations = destinations[defaultHost] ?: emptyList()
        val headerDestinations = listVersionRoutings(service, defaultHost)
        val missingDestinations = hostDestinations - headerDestinations.map { it.first }
        val obsoleteDestinations = headerDestinations.filter { it.first !in hostDestinations }.map { it.second }

        if (missingDestinations.isNotEmpty() || obsoleteDestinations.isNotEmpty()) {
            addRoutings(service, defaultHost, missingDestinations)
            removeRoutings(service, obsoleteDestinations)
            return service
        }
        return null
    }

    private fun isBorderService(service: VirtualService) = service.spec.gateways.isNotEmpty()

    private fun findDefaultHost(service: VirtualService): String {
        val defaultHost = service.spec.http.single { it.match.isEmpty() }.route.single().destination.host
        log.debug("${service.resName()} default host $defaultHost")
        return defaultHost
    }

    private fun listVersionRoutings(service: VirtualService, defaultHost: String): List<Pair<String, HTTPRoute>> {
        val result = ArrayList<Pair<String, HTTPRoute>>()

        for (httpRoute in service.spec.http) {
            val destination = findSingleDestination(httpRoute)

            destination?.let {
                val host = destination.host
                val subset = destination.subset

                val headerSubSet = findSingleHeaderMatch(httpRoute)
                if (host == defaultHost && subset != null && subset == headerSubSet) {
                    result.add(subset to httpRoute)
                }
            }
        }

        return result
    }

    private fun findSingleDestination(httpRouting: HTTPRoute): Destination? {
        httpRouting.match ?: return null

        return try {
            httpRouting.route.single().destination
        } catch (e: Exception) {
            null
        }
    }

    private fun findSingleHeaderMatch(httpRouting: HTTPRoute): String? {
        httpRouting.match ?: return null

        return try {
            return (httpRouting.match.single().headers["app-version"]?.matchType as ExactMatchType?)?.exact
        } catch (e: Exception) {
            null
        }
    }

    private fun listDestinationsByHost(): Map<String, List<String>> {
        return istioClient.v1beta1DestinationRule().inNamespace(namespace).list().items
            .associate { it.spec.host to it.spec.subsets.map { it.name } }
    }

    private fun addRoutings(
        service: VirtualService,
        defaultHost: String,
        missingDestinations: List<String>
    ) {
        val routes = missingDestinations.map {
            log.debug("${service.resName()} is missing routing $it for host $defaultHost")
            buildVersionHeaderRouting(defaultHost, it)
        }

        service.spec.http = routes + service.spec.http
    }

    private fun buildVersionHeaderRouting(host: String, version: String): HTTPRoute {
        return HTTPRoute().apply {
            match = listOf(
                HTTPMatchRequest().apply {
                    headers = mapOf("app-version" to StringMatch(ExactMatchType(version)))
                }
            )
            route = listOf(
                HTTPRouteDestination().apply {
                    destination = Destination().apply {
                        this.host = host
                        subset = version
                    }
                }
            )
        }
    }

    private fun removeRoutings(
        service: VirtualService,
        destinations: List<HTTPRoute>
    ) {
        destinations.forEach {
            log.debug("${service.resName()} has unmapped routing $it")
            service.spec.http.remove(it)
        }
    }
}