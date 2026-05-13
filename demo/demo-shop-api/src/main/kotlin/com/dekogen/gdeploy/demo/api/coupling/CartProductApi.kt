package com.dekogen.gdeploy.demo.api.coupling

import com.dekogen.gdeploy.apimarkup.RestApi
import com.dekogen.gdeploy.demo.api.mapping.ProductMapping

@RestApi("demo-shop-product", version = "v1")
class CartProductApi(
    val productMapping: ProductMapping
)