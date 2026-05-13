package com.dekogen.gdeploy.orchestrator.apiobserver

import com.dekogen.gdeploy.orchestrator.CloudService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface ApiUriGenerator {
    fun apiUri(service: CloudService, namespace: String): String
}

@Service
@Profile("dev")
class GatewayApiUriGenerator : ApiUriGenerator {
    override fun apiUri(service: CloudService, namespace: String) =
        "http://gd-shop.example.local/${service.service.split("-").last()}/meta/api"
}

@Service
@Profile(value = ["default", "kubernetes"])
class KubernetesApiUriGenerator : ApiUriGenerator {
    override fun apiUri(service: CloudService, namespace: String) =
        "http://${service.service}.$namespace/meta/api"
}
