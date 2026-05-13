package com.dekogen.gdeploy.demo.demoshopcart

import com.dekogen.gdeploy.demo.api.coupling.CartProductApi
import com.dekogen.gdeploy.demo.api.model.ProductDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cart")
class CartController(private val cartProductApi: CartProductApi) {
    @GetMapping("{id}")
    fun getCart(@PathVariable id: String): List<ProductDto> {
        return listOf(cartProductApi.productMapping.getProduct(id))
    }
}