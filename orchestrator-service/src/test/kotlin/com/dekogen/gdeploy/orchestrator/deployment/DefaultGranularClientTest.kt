package com.dekogen.gdeploy.orchestrator.deployment

import com.dekogen.gdeploy.orchestrator.resName
import io.fabric8.kubernetes.client.Config
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DefaultGranularClientTest {

    @Test
    @Disabled("Requires access to a Kubernetes cluster with VirtualDeployment resources")
    fun v1alpha1VirtualDeployment() {
        val client = DefaultGranularClient(Config.autoConfigure(null))

        client.v1alpha1VirtualDeployment().inAnyNamespace().list().items.forEach {
            println("${it.resName()} replicas: ${it.getSpec().getDeployment().replicas}")
        }
    }
}
