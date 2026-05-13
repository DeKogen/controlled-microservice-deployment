package com.dekogen.gdeploy.orchestrator.deployment

import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource

interface GranularDsl {
    fun v1alpha1VirtualDeployment(): MixedOperation<VirtualDeployment, VirtualDeploymentList, Resource<VirtualDeployment>>
}