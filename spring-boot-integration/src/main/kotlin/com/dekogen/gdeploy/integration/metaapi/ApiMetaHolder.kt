package com.dekogen.gdeploy.integration.metaapi

import com.dekogen.gdeploy.apimarkup.RestApi
import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController

@Component
class ApiMetaHolder(
    private val metaFactory: MetaFactory
) : ApplicationListener<ContextRefreshedEvent> {
    private val log = LoggerFactory.getLogger(this::class.java)
    var clientMappings: Map<String, Pair<InternalApiMapping, OpenAPI>> = emptyMap()
    var serverMapping: Map<String, Pair<InternalApiMapping, OpenAPI>> = emptyMap()

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val beans = event.applicationContext.getBeansWithAnnotation(RestApi::class.java).values

        val clientBeans = beans.filter { !isController(it) }
        clientMappings = getBeansOpenApis(clientBeans)
        log.info("Registered openAPI client mappings ${clientMappings.keys.joinToString(", ")}")

        val serverBeans = beans.filter { isController(it) }
        serverMapping = getBeansOpenApis(serverBeans)
        log.info("Registered openAPI server mappings ${serverMapping.keys.joinToString(", ")}")
    }

    private fun getBeansOpenApis(beans: List<Any>): Map<String, Pair<InternalApiMapping, OpenAPI>> {
        return beans.associate {
            val apiClass = findApiClass(it::class.java)
            val host = apiClass.getAnnotation(RestApi::class.java).value
            apiClass.simpleName to (InternalApiMapping(
                apiClass,
                host
            ) to metaFactory.createOpenApiForClient(apiClass))
        }
    }

    private fun isController(api: Any): Boolean {
        val apiClass = findApiClass(api::class.java)
        val fields = apiClass.declaredFields.filterNot { it.isSynthetic }
        return fields.isNotEmpty() && fields.map {
            it.isAccessible = true
            val fieldValue = it.get(api) ?: return@map false
            AnnotationUtils.findAnnotation(fieldValue::class.java, RestController::class.java) != null
        }.all { it }
    }

    private fun findApiClass(clazz: Class<*>): Class<*> {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            if (current.isAnnotationPresent(RestApi::class.java)) {
                return current
            }
            current = current.superclass
        }
        return clazz
    }
}
