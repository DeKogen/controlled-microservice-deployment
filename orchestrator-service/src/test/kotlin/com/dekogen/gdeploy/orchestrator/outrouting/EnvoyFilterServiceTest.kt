package com.dekogen.gdeploy.orchestrator.outrouting

import com.dekogen.gdeploy.orchestrator.CloudService
import com.dekogen.gdeploy.orchestrator.KubernetesConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class EnvoyFilterServiceTest {

    @Test
    @Disabled("Requires access to the gd-demo-shop Istio cluster")
    fun routeSource() {
        val service = ClientRoutingService(KubernetesConfig().namespace(), KubernetesConfig().istioClient())
        service.routeSource(CloudService("demo-shop-cart", "v1"), CloudService("demo-shop-product", "v2"))
    }
}
