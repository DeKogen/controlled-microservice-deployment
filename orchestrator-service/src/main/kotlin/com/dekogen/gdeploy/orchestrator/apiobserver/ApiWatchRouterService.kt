package com.dekogen.gdeploy.orchestrator.apiobserver

import com.dekogen.gdeploy.orchestrator.SimpleWatcher
import com.dekogen.gdeploy.orchestrator.outrouting.ClientRoutingService
import com.dekogen.gdeploy.orchestrator.resName
import io.fabric8.kubernetes.api.model.ListOptions
import io.fabric8.kubernetes.client.Watcher
import me.snowdrop.istio.api.networking.v1beta1.DestinationRule
import me.snowdrop.istio.client.IstioClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import jakarta.annotation.PostConstruct

@Service
class ApiWatchRouterService(
    private val istioClient: IstioClient,
    private val apiVersionService: ApiVersionService,
    private val namespace: String,
    private val clientRoutingService: ClientRoutingService
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val watchExecutor = Executors.newCachedThreadPool(CustomizableThreadFactory("k8s-watch-pool-"))
    private val retryExecutor = Executors.newScheduledThreadPool(4, CustomizableThreadFactory("k8s-retry-pool-"))

    @PostConstruct
    fun startWatch() {
        log.info("Registered watch over DestinationRule")
        watchExecutor.submit { watchDestinationRuleUpdates() }
    }

    private fun watchDestinationRuleUpdates() {
//        while (true) {
        val newRoutes = apiVersionService.generateClientRoutes().flatten()
        log.info("Initializing applying routes \n${newRoutes.joinToString("\n")}")
        newRoutes.forEach {
            clientRoutingService.routeSource(it.source, it.destination)
        }

        try {
            val destinationRuleVersion =
                istioClient.v1beta1DestinationRule().inNamespace(namespace).list().metadata.resourceVersion
            val listOptions = ListOptions().apply {
                resourceVersion = destinationRuleVersion
            }

            istioClient.v1beta1DestinationRule().inNamespace(namespace)
                .watch(listOptions, object : SimpleWatcher<DestinationRule> {
                    override fun eventReceived(action: Watcher.Action, resource: DestinationRule) {
                        handleDestinationUpdate(action, resource)
                    }
                })
        } catch (e: Exception) {
            log.error("error", e)
            Thread.sleep(1000)
        }
//        }
    }

    private fun handleDestinationUpdate(action: Watcher.Action, destinationRule: DestinationRule) {
        log.debug("${destinationRule.resName()} Watched $action")

        if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED || action == Watcher.Action.DELETED) {
            fullUpdate()
        }
    }

    private fun fullUpdate() {
        val newRoutes =try {
            apiVersionService.generateClientRoutes().flatten()
        } catch (e: IllegalStateException) {
            retryExecutor.schedule({ fullUpdate() }, 30, TimeUnit.SECONDS)
            log.warn(e.message)
            return
        }

        log.info("Destination updated, applying routes \n${newRoutes.joinToString("\n")}")
        newRoutes.forEach {
            clientRoutingService.routeSource(it.source, it.destination)
        }
    }
}
