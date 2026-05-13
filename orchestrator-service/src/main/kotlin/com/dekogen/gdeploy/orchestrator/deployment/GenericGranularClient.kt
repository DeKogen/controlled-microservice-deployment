package com.dekogen.gdeploy.orchestrator.deployment

import io.fabric8.kubernetes.client.Client
import io.fabric8.kubernetes.client.dsl.AnyNamespaceable
import io.fabric8.kubernetes.client.dsl.Namespaceable
import io.fabric8.kubernetes.client.dsl.RequestConfigurable

interface GenericGranularClient<C : Client> : Client, GranularClient,
    Namespaceable<C>,
    AnyNamespaceable<C>,
    RequestConfigurable<C> {
}