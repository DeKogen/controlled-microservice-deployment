package com.dekogen.gdeploy.integration

import com.dekogen.gdeploy.apimarkup.RestApi
import org.apache.juli.logging.LogFactory
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpMessageConverterExtractor
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method


class RemoteInvocationHandler(
    private val apiDeclaration: RestApi,
    private val rootPath: String
) : InvocationHandler {
    private val log = LogFactory.getLog(this::class.java)

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        val targetUri =
            UriComponentsBuilder.fromUriString("http://${apiDeclaration.value}").replacePath(null)
                .pathSegment(rootPath, getCrudMapping(method))
        val httpMethod = getHttpMethod(method)

        val restTemplate = RestTemplate()

        val urlParamMap = HashMap<String, Any?>()
        var requestCallback: RequestCallback = restTemplate.acceptHeaderRequestCallback(method.returnType)
        val extractor = HttpMessageConverterExtractor<Any?>(method.returnType, restTemplate.messageConverters)

        for ((i, param) in method.parameters.withIndex()) {
            val pValue = args[i]

            param.getAnnotation(PathVariable::class.java)?.let {
                if (it.value.isEmpty()) {
                    urlParamMap[param.name] = pValue
                } else {
                    urlParamMap[it.value] = pValue
                }
            }

            param.getAnnotation(RequestParam::class.java)?.let {
                targetUri.queryParam(it.value, pValue)
            }

            param.getAnnotation(RequestBody::class.java)?.let {
                requestCallback = restTemplate.httpEntityCallback<Any?>(pValue, method.returnType)
            }
        }

        log.info("Proxy call $httpMethod ${targetUri.toUriString()} to version ${apiDeclaration.version}")
        return restTemplate.execute(targetUri.build().toString(), httpMethod, requestCallback, extractor, urlParamMap)
    }

    private fun getCrudMapping(handlerMethod: Method): String =
        handlerMethod.getAnnotation(RequestMapping::class.java)?.value?.first() ?: handlerMethod.getAnnotation(
            GetMapping::class.java
        )?.value?.first() ?: handlerMethod.getAnnotation(PostMapping::class.java)?.value?.first()
        ?: handlerMethod.getAnnotation(PutMapping::class.java)?.value?.first() ?: handlerMethod.getAnnotation(
            DeleteMapping::class.java
        )?.value?.first() ?: throw IllegalStateException()

    private fun getHttpMethod(handlerMethod: Method) = when {
        handlerMethod.isAnnotationPresent(RequestMapping::class.java) ->
            HttpMethod.valueOf(handlerMethod.getAnnotation(RequestMapping::class.java).method.first().toString())
        handlerMethod.isAnnotationPresent(GetMapping::class.java) -> HttpMethod.GET
        handlerMethod.isAnnotationPresent(PostMapping::class.java) -> HttpMethod.POST
        handlerMethod.isAnnotationPresent(PutMapping::class.java) -> HttpMethod.PUT
        handlerMethod.isAnnotationPresent(DeleteMapping::class.java) -> HttpMethod.DELETE
        else -> throw IllegalStateException()
    }
}