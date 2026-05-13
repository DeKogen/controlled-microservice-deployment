package com.dekogen.gdeploy.orchestrator.deployment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Plural
import io.fabric8.kubernetes.model.annotation.Version

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "exportTo", "gateways", "hosts", "http", "tcp", "tls")
//@IstioKind(name = "VirtualDeployment", plural = "virtualdeployments")
//@IstioApiVersion("granular-deployment.dekogen.com/v1alpha1")
@Version("v1alpha1")
@Group("granular-deployment.dekogen.com")
@Plural("virtualdeployments")
class VirtualDeploymentSpec {
    @JsonProperty("versions")
    private var versions: List<String> = emptyList()

    @JsonProperty("deployment")
    private var deployment: DeploymentSpec = DeploymentSpec()

    @JsonProperty("boundService")
    private var boundService: String = ""

    fun setVersions(versions: List<String>) {
        this.versions = versions
    }

    fun getVersions() = versions

    fun setDeployment(deployment: DeploymentSpec) {
        this.deployment = deployment
    }

    fun getDeployment() = deployment

    fun setBoundService(boundService: String) {
        this.boundService = boundService
    }

    fun getBoundService() = boundService
}