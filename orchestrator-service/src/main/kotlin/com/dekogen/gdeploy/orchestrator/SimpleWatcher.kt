package com.dekogen.gdeploy.orchestrator

import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.WatcherException

interface SimpleWatcher<T> : Watcher<T> {
    //    override fun onClose(cause: KubernetesClientException?) {
//    }
    override fun onClose(cause: WatcherException?) {
    }
}