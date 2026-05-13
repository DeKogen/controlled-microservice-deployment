package com.dekogen.gdeploy.orchestrator.deployment

import io.fabric8.kubernetes.client.*
import io.fabric8.kubernetes.client.dsl.FunctionCallable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import okhttp3.OkHttpClient

class DefaultGranularClient : BaseClient, NamespacedGranularClient {

    constructor() : super()

    constructor(configuration: Config) : super(configuration)

    constructor(httpClient: OkHttpClient, configuration: Config) : super(httpClient, configuration)


    override fun inNamespace(name: String?): NamespacedGranularClient {
        val updated = ConfigBuilder(configuration)
            .withNamespace(namespace)
            .build()

        return DefaultGranularClient(getHttpClient(), updated)
    }

    override fun inAnyNamespace(): NamespacedGranularClient {
        return inNamespace(null)
    }

    override fun withRequestConfig(requestConfig: RequestConfig?): FunctionCallable<NamespacedGranularClient> {
        return WithRequestCallable(this, requestConfig)
    }

    override fun v1alpha1VirtualDeployment(): MixedOperation<VirtualDeployment, VirtualDeploymentList, Resource<VirtualDeployment>> {
        return VirtualDeploymentOperationImpl(httpClient, configuration)
    }

    private companion object {
        init {
            KubernetesDeserializer.registerProvider(GranularResourceMappingProvider())
        }
    }
}