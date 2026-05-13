package com.dekogen.gdeploy.demo.demoshopproduct

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.dekogen.gdeploy"])
class DemoShopProductApplication

fun main(args: Array<String>) {
    runApplication<DemoShopProductApplication>(*args)
}
