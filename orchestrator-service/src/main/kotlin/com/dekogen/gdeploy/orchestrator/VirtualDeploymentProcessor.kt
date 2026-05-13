package com.dekogen.gdeploy.orchestrator

import com.dekogen.gdeploy.orchestrator.deployment.GranularClient
import com.dekogen.gdeploy.orchestrator.deployment.VirtualDeployment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.Watcher
import me.snowdrop.istio.api.networking.v1beta1.DestinationRule
import me.snowdrop.istio.api.networking.v1beta1.DestinationRuleSpec
import me.snowdrop.istio.api.networking.v1beta1.Subset
import me.snowdrop.istio.client.IstioClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import kotlin.random.Random

@Service
class VirtualDeploymentProcessor(
    private val granularClient: GranularClient,
    private val kubernetesClient: KubernetesClient,
    private val istioClient: IstioClient,
    private val namespace: String,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun startWatch() {
        granularClient.v1alpha1VirtualDeployment().inNamespace(namespace)
            .watch(object : SimpleWatcher<VirtualDeployment> {
                override fun eventReceived(action: Watcher.Action, resource: VirtualDeployment) {
                    log.debug("${resource.resName()} Watched $action")
                    if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED) {
                        val requiredVersions = resource.getSpec().getVersions()
                        val all = kubernetesClient.apps().deployments().inNamespace(namespace).list().items
                        val actual = all
                            .filter { it.metadata.name.startsWith(resource.metadata.name) }
                            .filter { "granular-deployment" in it.metadata.labels }

                        val actualNames = actual.map { it.metadata.name }
                        log.debug("Listed deployments $actualNames")

                        val missing =
                            requiredVersions.filter { !startsWithAny(actualNames, "${resource.metadata.name}-$it") }
                        log.debug("Missing versions $missing")

                        val createdMissing = missing.map { createVersionedDeployment(resource, it) }

                        createdMissing.forEach {
                            kubernetesClient.apps().deployments().inNamespace(namespace).createOrReplace(it)
                        }

                        val serviceName = resource.getSpec().getBoundService()
                        val destinationRule =
                            istioClient.v1beta1DestinationRule().inNamespace(namespace).withName(serviceName).get()
                                ?: DestinationRule().apply {
                                    metadata = ObjectMeta()
                                    metadata.name = serviceName
                                    metadata.namespace = resource.metadata.namespace
                                    metadata.labels = hashMapOf("granular-deployment" to "1")

                                    spec = DestinationRuleSpec()
                                    spec.subsets = arrayListOf()
                                }

                        val presentVersionRules = destinationRule.spec.subsets.map { it.name }
                        val missingVersionRules = requiredVersions - presentVersionRules
                        val addedRules = missingVersionRules.map {
                            Subset().apply {
                                name = it
                                labels = hashMapOf("version" to it)
                            }
                        }
                        log.debug("Added destination rules $missingVersionRules")
                        destinationRule.spec.subsets.addAll(addedRules)
                        if (addedRules.isNotEmpty()) {
                            istioClient.v1beta1DestinationRule().inNamespace(namespace).createOrReplace(destinationRule)
                        }
                    }
                }
            })
    }

    private fun startsWithAny(list: List<String>, key: String): Boolean {
        return list.filter { it.startsWith(key) }.any()
    }

    private fun createVersionedDeployment(virtualDeployment: VirtualDeployment, version: String): Deployment {
        val spec = objectMapper.readValue<DeploymentSpec>(
            objectMapper.writeValueAsString(
                virtualDeployment.getSpec().getDeployment()
            )
        )
        spec.selector.matchLabels["version"] = version
        spec.template.metadata.labels["version"] = version
        spec.template.spec.containers.onEach {
            it.image = "${it.image}:$version"
        }

        val deployment = Deployment()
        deployment.spec = spec
        deployment.metadata = ObjectMeta()
        deployment.metadata.labels = hashMapOf("granular-deployment" to "1")
        deployment.metadata.namespace = virtualDeployment.metadata.namespace
        deployment.metadata.name = "${getVersionedName(virtualDeployment, version)}-${getRandomQualifier()}"

        return deployment
    }

    private fun getVersionedName(virtualDeployment: VirtualDeployment, version: String) =
        "${virtualDeployment.metadata.name}-$version"

    private fun getRandomQualifier(): String {
        return Random.nextInt(60_466_176, Int.MAX_VALUE).toString(36)
    }
}
