package com.dekogen.gdeploy.integration.metaapi

import io.swagger.v3.oas.models.OpenAPI
import org.mockito.Mockito
import org.springdoc.core.customizers.OpenApiBuilderCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.customizers.ServerBaseUrlCustomizer
import org.springdoc.core.customizers.SpringDocCustomizers
import org.springdoc.core.providers.JavadocProvider
import org.springdoc.core.providers.SpringDocProviders
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.service.AbstractRequestService
import org.springdoc.core.service.GenericResponseService
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.OperationService
import org.springdoc.core.service.SecurityService
import org.springdoc.core.utils.PropertyResolverUtils
import org.springdoc.webmvc.api.OpenApiWebMvcResource
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.format.support.FormattingConversionService
import org.springframework.stereotype.Component
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping
import org.springframework.web.servlet.resource.ResourceUrlProvider
import java.lang.reflect.Proxy
import java.util.*

@Component
class MetaFactory(
    @Qualifier("mvcContentNegotiationManager") private val contentNegotiationManager: ContentNegotiationManager,
    @Qualifier("mvcConversionService") private val conversionService: FormattingConversionService,
    @Qualifier("mvcResourceUrlProvider") private val resourceUrlProvider: ResourceUrlProvider,
    private val enableWebMvcConfiguration: WebMvcAutoConfiguration.EnableWebMvcConfiguration,
    private val openAPI: Optional<OpenAPI>?,
    private val context: ApplicationContext,
    private val securityParser: SecurityService?,
    private val springDocConfigProperties: SpringDocConfigProperties?,
    private val propertyResolverUtils: PropertyResolverUtils?,
    private val openApiBuilderCustomisers: Optional<List<OpenApiBuilderCustomizer>>?,
    private val serverBaseUrlCustomizers: Optional<List<ServerBaseUrlCustomizer>>?,
    private val javadocProvider: Optional<JavadocProvider>?,
    private val requestBuilder: AbstractRequestService?,
    private val responseBuilder: GenericResponseService?,
    private val operationParser: OperationService?,
    private val springDocProviders: SpringDocProviders?,
    private val springDocCustomizers: SpringDocCustomizers?
) {
    fun createOpenApiForClient(clientClass: Class<*>): OpenApiWebMvcResource {
        val clientMappings = clientClass.constructors.first().parameters
            .map { parameter -> parameter.type }
            .toTypedArray()

        val namedFakeControllers = clientMappings.map { mapping ->
            val mname = mapping.simpleName

            val virtualControllerIdentity = Any()

            mname to Proxy.newProxyInstance(
                clientClass.classLoader,
                arrayOf(mapping, PseudoController::class.java)
            ) { proxy, method, args ->
                return@newProxyInstance when (method.name) {
                    "toString" -> mname
                    "equals" -> proxy == args[0]
                    "hashCode" -> virtualControllerIdentity.hashCode()
                    else -> null
                }
            }
        }.toList()


        val springMvcHandlerMapping = enableWebMvcConfiguration.requestMappingHandlerMapping(
            contentNegotiationManager,
            conversionService,
            resourceUrlProvider
        )

        val detectHandlerMethods = AbstractHandlerMethodMapping::class.java
            .getDeclaredMethod("detectHandlerMethods", Any::class.java)
        detectHandlerMethods.trySetAccessible()

        namedFakeControllers.onEach {
            detectHandlerMethods.invoke(springMvcHandlerMapping, it.second)
        }

        val fakeControllerMap = namedFakeControllers.associate { it.first to it.second }

        val openapi = OpenApiWebMvcResource(
            ObjectFactory {
                val contextMock = Mockito.mock(ApplicationContext::class.java)
                Mockito.`when`(contextMock.getBeansWithAnnotation(Mockito.any())).thenReturn(emptyMap())
                Mockito.`when`(contextMock.containsBean(Mockito.any())).thenReturn(false)
                Mockito.`when`(contextMock.getBean(PropertyResolverUtils::class.java))
                    .thenAnswer { context.getBean(PropertyResolverUtils::class.java) }

                val api = OpenAPIService(
                    openAPI,
                    securityParser,
                    springDocConfigProperties,
                    propertyResolverUtils,
                    openApiBuilderCustomisers,
                    serverBaseUrlCustomizers,
                    javadocProvider
                )
                api.setApplicationContext(contextMock)
                api.mappingsMap.clear()
                api.addMappings(fakeControllerMap)
                api
            },
            requestBuilder,
            responseBuilder,
            operationParser,
            springDocConfigProperties,
            springDocProviders,
            springDocCustomizers
        )

        return openapi
    }
}
