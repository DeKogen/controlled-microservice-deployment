package com.dekogen.gdeploy.orchestrator.apiobserver

import com.dekogen.gdeploy.orchestrator.CloudService
import org.springframework.data.annotation.Id

data class ApiVersion(
    @Id
    val id: String?,
    val service: CloudService
)
