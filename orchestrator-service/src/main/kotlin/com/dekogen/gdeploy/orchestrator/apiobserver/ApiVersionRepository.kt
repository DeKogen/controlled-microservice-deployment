package com.dekogen.gdeploy.orchestrator.apiobserver

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ApiVersionRepository : MongoRepository<ApiVersion, String> {
}