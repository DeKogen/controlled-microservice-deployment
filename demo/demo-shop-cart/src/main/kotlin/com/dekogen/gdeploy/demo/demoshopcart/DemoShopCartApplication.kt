package com.dekogen.gdeploy.demo.demoshopcart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.dekogen.gdeploy"])
class DemoShopCartApplication

fun main(args: Array<String>) {
    runApplication<DemoShopCartApplication>(*args)
}
