package com.dekogen.gdeploy.orchestrator

import com.dekogen.gdeploy.orchestrator.deployment.DefaultGranularClient
import com.dekogen.gdeploy.orchestrator.deployment.GranularClient
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import me.snowdrop.istio.client.DefaultIstioClient
import me.snowdrop.istio.client.IstioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KubernetesConfig {

    @Bean
    fun kubernetesClient(): KubernetesClient {
        return DefaultKubernetesClient(kubernetesConfig())
    }

    @Bean
    fun istioClient(): IstioClient {
        return DefaultIstioClient(kubernetesConfig())
    }

    @Bean
    fun granularClient(): GranularClient {
        return DefaultGranularClient(kubernetesConfig())
    }

    @Bean
    fun namespace(): String {
        return "gd-demo-shop"
    }

    private fun kubernetesConfig(): Config {
        return Config.autoConfigure(null)
    }
}
