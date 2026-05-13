package com.dekogen.gdeploy.integration.metaapi

import com.dekogen.gdeploy.apimarkup.RestApi
import org.slf4j.LoggerFactory
import org.springdoc.webmvc.api.OpenApiWebMvcResource
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController

@Component
class ApiMetaHolder(
    private val metaFactory: MetaFactory
) : ApplicationListener<ContextRefreshedEvent> {
    private val log = LoggerFactory.getLogger(this::class.java)
    lateinit var clientMappings: Map<String, Pair<InternalApiMapping, OpenApiWebMvcResource>>
    lateinit var serverMapping: Map<String, Pair<InternalApiMapping, OpenApiWebMvcResource>>

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val beans = event.applicationContext.getBeansWithAnnotation(RestApi::class.java).values

        val clientBeans = beans.filter { !isController(it) }
        clientMappings = getBeansOpenApis(clientBeans)
        log.info("Registered openAPI client mappings ${clientMappings.keys.joinToString(", ")}")

        val serverBeans = beans.filter { isController(it) }
        serverMapping = getBeansOpenApis(serverBeans)
        log.info("Registered openAPI server mappings ${serverMapping.keys.joinToString(", ")}")
    }

    private fun getBeansOpenApis(beans: List<Any>): Map<String, Pair<InternalApiMapping, OpenApiWebMvcResource>> {
        return beans.associate {
            val host = it::class.java.getAnnotation(RestApi::class.java).value
            it::class.java.simpleName to (InternalApiMapping(
                it::class.java,
                host
            ) to metaFactory.createOpenApiForClient(it::class.java))
        }
    }

    private fun isController(api: Any): Boolean {
        return api::class.java.declaredFields.map {
            it.isAccessible = true
            it.get(api)::class.java.isAnnotationPresent(RestController::class.java)
        }.all { it }
    }
}