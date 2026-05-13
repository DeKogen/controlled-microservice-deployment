package com.dekogen.gdeploy.orchestrator.deployment

import io.fabric8.kubernetes.client.Client

interface GranularClient : Client, GranularDsl{
}