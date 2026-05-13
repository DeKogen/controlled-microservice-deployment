package com.dekogen.gdeploy.orchestrator.outrouting

import com.dekogen.gdeploy.orchestrator.CloudService
import org.intellij.lang.annotations.Language

object EnvoyScriptUtils {
    fun removeRouteFromCode(luaCode: String, service: CloudService): String {
        val map = locateRoutingMap(luaCode)
        val routes = listRoutes(map)

        if (routes.none { it.contains(service.service) }) {
            return luaCode
        }
        val newRoutes = routes.filter { !it.contains(service.service) }
        val newMap = luaRoutingMap(newRoutes)
        return newMap + luaRoutingCode
    }

    fun addUniqueRouteToMap(luaCode: String, service: CloudService): String {
        val resCode = removeRouteFromCode(luaCode, service)
        val map = locateRoutingMap(resCode)
        val routes = listRoutes(map) + serviceAsRoute(service)
        return luaRoutingMap(routes) + luaRoutingCode
    }

    fun emptyRoutingCode(): String {
        return luaRoutingMap(listOf()) + luaRoutingCode
    }

    fun listRoutes(routingMap: String): List<String> {
        return routingMap.split("\n")
            .drop(1)
            .dropLast(2)
            .map { it.replace(",", "") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun locateRoutingMap(luaCode: String): String {
        val begin = luaCode.indexOf("routingMap")
        val end = luaCode.indexOf("};", begin)
        return luaCode.substring(begin, end + 3)
    }

    fun serviceAsRoute(service: CloudService): String {
        return "[\"${service.service}\"] = \"${service.version}\""
    }

    fun luaRoutingMap(routes: List<String>) = """
        routingMap = {
            ${routes.joinToString(",\n            ")}
        };
        
    """.trimIndent()

    @Language("Lua")
    val luaRoutingCode = """
        function envoy_on_request(request_handle)
            host = request_handle:headers():get(":authority")
            version = routingMap[host]
            if version then
                request_handle:headers():add("app-version", version);
            else
                request_handle:logWarn("No version mapping for host " .. host);
            end
        end
        
    """.trimIndent()
}