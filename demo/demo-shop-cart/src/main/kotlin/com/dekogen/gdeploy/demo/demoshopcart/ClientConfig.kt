package com.dekogen.gdeploy.demo.demoshopcart

import com.dekogen.gdeploy.demo.api.coupling.CartProductApi
import com.dekogen.gdeploy.integration.RemoteClientFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientConfig {
    @Bean
    fun remoteTestRemote(remoteClientFactory: RemoteClientFactory): CartProductApi =
        remoteClientFactory.createClient()
}