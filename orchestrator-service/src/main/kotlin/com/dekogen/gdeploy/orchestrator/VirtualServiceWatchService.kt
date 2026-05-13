package com.dekogen.gdeploy.orchestrator

import io.fabric8.kubernetes.api.model.ListOptions
import io.fabric8.kubernetes.client.Watcher
import me.snowdrop.istio.api.networking.v1beta1.DestinationRule
import me.snowdrop.istio.api.networking.v1beta1.VirtualService
import me.snowdrop.istio.client.IstioClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import jakarta.annotation.PostConstruct

@Service
class VirtualServiceWatchService(
    private val istioClient: IstioClient,
    private val namespace: String,
    private val virtualServiceRouteService: VirtualServiceRouteService
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val selfInitiatedUpdates = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val watchExecutor = Executors.newCachedThreadPool(CustomizableThreadFactory("k8s-watch-pool-"))

    @PostConstruct
    fun startWatch() {
        log.info("Registered watch over VirtualService and DestinationRule")
        watchExecutor.submit { watchVirtualService() }
        watchExecutor.submit { watchDestinationRuleUpdates() }
    }

    private fun watchVirtualService() {
//        while (true) {
        try {
            val virtualServiceVersion =
                istioClient.v1beta1VirtualService().inNamespace(namespace).list().metadata.resourceVersion
            val listOptions = ListOptions().apply {
                resourceVersion = virtualServiceVersion
            }

            virtualServiceRouteService.routeAll().forEach {
                log.info("${it.resName()} rerouted")
                selfInitiatedUpdates.add(it.resName())
            }

            istioClient.v1beta1VirtualService().inNamespace(namespace)
                .watch(listOptions, object : SimpleWatcher<VirtualService> {
                    override fun eventReceived(action: Watcher.Action, resource: VirtualService) {
                        handleServiceUpdate(action, resource)
                    }
                })
        } catch (e: Exception) {
            log.error("error", e)
            Thread.sleep(1000)
        }
//        }
    }

    private fun watchDestinationRuleUpdates() {
//        while (true) {
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

    private fun handleServiceUpdate(action: Watcher.Action, service: VirtualService) {
        log.debug("${service.resName()} Watched $action")

        if (service.resName() in selfInitiatedUpdates) {
            selfInitiatedUpdates.remove(service.resName())
            log.debug("${service.resName()} Ignored self inflicting update")
            return
        }

        if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED) {
            virtualServiceRouteService.route(service)?.let {
                log.info("${it.resName()} rerouted")
                selfInitiatedUpdates.add(it.resName())
            }
        }
    }

    private fun handleDestinationUpdate(action: Watcher.Action, destinationRule: DestinationRule) {
        log.debug("${destinationRule.resName()} Watched $action")

        if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED || action == Watcher.Action.DELETED) {
            virtualServiceRouteService.routeAll().forEach {
                log.info("${it.resName()} rerouted")
                selfInitiatedUpdates.add(it.resName())
            }
        }
    }
}
