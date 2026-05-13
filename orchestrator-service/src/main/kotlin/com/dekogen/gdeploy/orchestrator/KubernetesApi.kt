package com.dekogen.gdeploy.orchestrator

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import io.fabric8.kubernetes.api.model.HasMetadata

fun dumpYaml(o: Any): String {
    return YAMLMapper().writeValueAsString(o)
}

fun HasMetadata.resName() =
    "${this.kind}[${this.metadata.namespace}/${this.metadata.name}:${this.metadata.resourceVersion}]"
