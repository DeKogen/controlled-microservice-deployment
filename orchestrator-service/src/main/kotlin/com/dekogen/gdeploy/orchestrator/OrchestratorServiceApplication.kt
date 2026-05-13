package com.dekogen.gdeploy.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = ["com.dekogen.gdeploy"])
class OrchestratorServiceApplication

fun main(args: Array<String>) {
    runApplication<OrchestratorServiceApplication>(*args)
}
