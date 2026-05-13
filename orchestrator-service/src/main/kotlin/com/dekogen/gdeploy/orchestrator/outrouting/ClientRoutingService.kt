package com.dekogen.gdeploy.orchestrator.outrouting

import com.dekogen.gdeploy.orchestrator.CloudService
import io.fabric8.kubernetes.api.model.ObjectMeta
import me.snowdrop.istio.api.networking.v1alpha3.*
import me.snowdrop.istio.client.IstioClient
import org.springframework.stereotype.Service


@Service
class ClientRoutingService(
    private val namespace: String,
    private val istioClient: IstioClient
) {
    fun routeSource(source: CloudService, target: CloudService) {
        val filterName = serviceAsFilterName(source)
        val filter =
            istioClient.v1alpha3EnvoyFilter().inNamespace(namespace).withName(filterName).get() ?: newFilter(source)
        addRouting(filter, target)
        istioClient.v1alpha3EnvoyFilter().inNamespace(namespace).createOrReplace(filter)
    }

    private fun newFilter(source: CloudService): EnvoyFilter {
        return EnvoyFilter().apply {
            metadata = ObjectMeta().apply {
                name = serviceAsFilterName(source)
                namespace = this@ClientRoutingService.namespace
            }
            spec = EnvoyFilterSpec().apply {
                workloadSelector = WorkloadSelector().apply {
                    labels = mapOf(
                        "app" to source.service,
                        "version" to source.version
                    )
                }
                configPatches = listOf(
                    EnvoyConfigObjectPatch().apply {
                        applyTo = ApplyTo.HTTP_FILTER
                        match = EnvoyConfigObjectMatch().apply {
                            context = PatchContext.SIDECAR_OUTBOUND
                        }
                        patch = Patch().apply {
                            operation = Operation.INSERT_BEFORE
                            value = mutableMapOf(
                                "name" to "envoy.lua",
                                "typed_config" to mutableMapOf<String, Any>(
                                    "@type" to "type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua",
                                    "inlineCode" to EnvoyScriptUtils.emptyRoutingCode()
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    private fun addRouting(filter: EnvoyFilter, target: CloudService) {
        val code =
            (filter.spec.configPatches.single().patch.value["typed_config"]!! as MutableMap<*, *>)["inlineCode"]!! as String
        val newCode = EnvoyScriptUtils.addUniqueRouteToMap(code, target)
        (filter.spec.configPatches.single().patch.value["typed_config"]!! as MutableMap<String, Any>)["inlineCode"] = newCode
    }

    private fun serviceAsFilterName(service: CloudService): String {
        return "version-routing-${service.service}-${service.version}"
    }
}