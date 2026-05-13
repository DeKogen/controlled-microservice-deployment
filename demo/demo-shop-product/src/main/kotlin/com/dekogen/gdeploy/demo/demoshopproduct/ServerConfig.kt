package com.dekogen.gdeploy.demo.demoshopproduct

import com.dekogen.gdeploy.demo.api.coupling.CartProductApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServerConfig {
    @Bean
    fun remoteProductApi(productController: ProductController) = CartProductApi(productController)
}