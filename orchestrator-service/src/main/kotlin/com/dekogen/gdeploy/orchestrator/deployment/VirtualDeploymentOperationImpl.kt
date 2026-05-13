package com.dekogen.gdeploy.orchestrator.deployment

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.dsl.base.HasMetadataOperation
import io.fabric8.kubernetes.client.dsl.base.OperationContext
import okhttp3.OkHttpClient

class VirtualDeploymentOperationImpl :
    HasMetadataOperation<VirtualDeployment, VirtualDeploymentList, Resource<VirtualDeployment>> {

    constructor(client: OkHttpClient?, config: Config?) : this(
        OperationContext().withOkhttpClient(client).withConfig(config)
    )

    constructor(context: OperationContext) : super(
        context.withApiGroupName("granular-deployment.dekogen.com")
            .withApiGroupVersion("v1alpha1")
            .withPlural("virtualdeployments")
    ) {
        type = VirtualDeployment::class.java
        listType = VirtualDeploymentList::class.java
    }

    override fun newInstance(context: OperationContext): VirtualDeploymentOperationImpl {
        return VirtualDeploymentOperationImpl(context)
    }

}