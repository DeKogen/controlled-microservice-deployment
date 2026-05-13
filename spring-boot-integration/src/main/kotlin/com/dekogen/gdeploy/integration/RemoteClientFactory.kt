package com.dekogen.gdeploy.integration

import com.dekogen.gdeploy.apimarkup.RestApi
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Proxy


@Component
class RemoteClientFactory {
    final inline fun <reified T : Any> createClient(): T = createClient(T::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createClient(clazz: Class<T>): T {
        val constructor = clazz.constructors.first()

        val annotation = clazz.getAnnotation(RestApi::class.java)!!

        val params = constructor.parameters
            .map { parameter ->
                val paramClass = parameter.type

                val loader = paramClass.classLoader
                val interfaces = arrayOf(paramClass)
                val rootPath = paramClass.getAnnotation(RequestMapping::class.java)?.value?.first() ?: "/"

                Proxy.newProxyInstance(loader, interfaces, RemoteInvocationHandler(annotation, rootPath))
            }
            .toTypedArray()

        return constructor.newInstance(*params) as T
    }
}
