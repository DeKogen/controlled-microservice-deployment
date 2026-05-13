package com.dekogen.gdeploy.orchestrator.outrouting

import com.dekogen.gdeploy.orchestrator.CloudService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EnvoyScriptUtilsTest {

    @Test
    fun listRoutes() {
        println(EnvoyScriptUtils.listRoutes(EnvoyScriptUtils.luaRoutingMap(listOf("a", "b"))))
        println(EnvoyScriptUtils.listRoutes(EnvoyScriptUtils.luaRoutingMap(listOf("a"))))
    }

    @Test
    fun locateRoutingMap() {
        val code = EnvoyScriptUtils.luaRoutingMap(listOf("a", "b")) + EnvoyScriptUtils.luaRoutingCode

        println(EnvoyScriptUtils.locateRoutingMap(code))
    }

    @Test
    fun routingCode() {
        var code = EnvoyScriptUtils.emptyRoutingCode()
        code = EnvoyScriptUtils.addUniqueRouteToMap(code, CloudService("a", "1"))
        code = EnvoyScriptUtils.addUniqueRouteToMap(code, CloudService("b", "1"))
        code = EnvoyScriptUtils.addUniqueRouteToMap(code, CloudService("a", "2"))
//        code = EnvoyScriptUtils.removeRouteFromCode(code, CloudService("a", "1"))
        println(code)
    }
}