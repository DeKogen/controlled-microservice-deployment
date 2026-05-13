package com.dekogen.gdeploy.demo.api.mapping

import com.dekogen.gdeploy.demo.api.model.ProductDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping


@RequestMapping("api/v1/product")
interface ProductMapping {
    @GetMapping("{id}")
    fun getProduct(@PathVariable id: String): ProductDto
}
