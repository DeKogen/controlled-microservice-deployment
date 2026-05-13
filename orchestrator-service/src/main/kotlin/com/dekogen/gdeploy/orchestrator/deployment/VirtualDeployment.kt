package com.dekogen.gdeploy.orchestrator.deployment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Plural
import io.fabric8.kubernetes.model.annotation.Version

@Group("granular-deployment.dekogen.com")
@Version("v1alpha1")
@Plural("virtualdeployments")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "spec", "status")
class VirtualDeployment(
) : HasMetadata, Namespaced {

    @JsonProperty("apiVersion")
    private var apiVersion = "granular-deployment.dekogen.com/v1alpha1"

    @JsonProperty("kind")
    private var kind = "VirtualDeployment"

    @JsonProperty("metadata")
    private var metadata: ObjectMeta? = null

    @JsonProperty("spec")
    private var spec: VirtualDeploymentSpec? = null

    @JsonProperty("apiVersion")
    override fun getApiVersion(): String {
        return apiVersion
    }

    @JsonProperty("apiVersion")
    override fun setApiVersion(apiVersion: String) {
        this.apiVersion = apiVersion
    }

    @JsonProperty("kind")
    override fun getKind(): String {
        return kind
    }

    @JsonProperty("kind")
    fun setKind(kind: String) {
        this.kind = kind
    }

    @JsonProperty("metadata")
    override fun getMetadata(): ObjectMeta {
        return metadata!!
    }

    @JsonProperty("metadata")
    override fun setMetadata(metadata: ObjectMeta?) {
        this.metadata = metadata
    }

    @JsonProperty("spec")
    fun getSpec(): VirtualDeploymentSpec {
        return spec!!
    }

    @JsonProperty("spec")
    fun setSpec(spec: VirtualDeploymentSpec?) {
        this.spec = spec
    }
}