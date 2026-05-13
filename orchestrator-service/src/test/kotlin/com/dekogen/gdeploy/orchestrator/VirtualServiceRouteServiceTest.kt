package com.dekogen.gdeploy.orchestrator

import io.fabric8.kubernetes.client.Config
import me.snowdrop.istio.client.DefaultIstioClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class VirtualServiceRouteServiceTest {

    @Test
    @Disabled("Requires access to the gd-demo-shop Istio cluster")
    fun routeAll() {
        val istio = DefaultIstioClient(Config.autoConfigure(null))
        VirtualServiceRouteService("gd-demo-shop", istio).routeAll()

    }

    @Test
    fun route() {
    }
}
