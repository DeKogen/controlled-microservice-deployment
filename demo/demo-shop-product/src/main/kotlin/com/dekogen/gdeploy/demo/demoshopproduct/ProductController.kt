package com.dekogen.gdeploy.demo.demoshopproduct

import com.dekogen.gdeploy.demo.api.mapping.ProductMapping
import com.dekogen.gdeploy.demo.api.model.ProductDto
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController : ProductMapping {
    override fun getProduct(id: String): ProductDto {
        return ProductDto("Product-v3-$id")
    }
}