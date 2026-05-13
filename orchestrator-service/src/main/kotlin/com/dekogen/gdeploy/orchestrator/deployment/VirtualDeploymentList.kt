package com.dekogen.gdeploy.orchestrator.deployment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.api.model.ListMeta

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "items")
@JsonDeserialize(using = JsonDeserializer.None::class)
class VirtualDeploymentList : KubernetesResource, KubernetesResourceList<VirtualDeployment> {

    @JsonProperty("apiVersion")
    private var apiVersion = "granular-deployment.dekogen.com/v1alpha1"

    @JsonProperty("items")
    private var items: List<VirtualDeployment> = ArrayList()

    @JsonProperty("kind")
    private var kind = "VirtualDeploymentList"

    @JsonProperty("metadata")
    private var metadata: ListMeta? = null

    @JsonProperty("apiVersion")
    fun getApiVersion(): String {
        return apiVersion
    }

    @JsonProperty("apiVersion")
    fun setApiVersion(apiVersion: String) {
        this.apiVersion = apiVersion
    }

    @JsonProperty("items")
    override fun getItems(): List<VirtualDeployment> {
        return items
    }

    @JsonProperty("items")
    fun setItems(items: List<VirtualDeployment>) {
        this.items = items
    }

    @JsonProperty("kind")
    fun getKind(): String {
        return kind
    }

    @JsonProperty("kind")
    fun setKind(kind: String) {
        this.kind = kind
    }

    @JsonProperty("metadata")
    override fun getMetadata(): ListMeta? {
        return metadata
    }

    @JsonProperty("metadata")
    fun setMetadata(metadata: ListMeta?) {
        this.metadata = metadata
    }

}