package com.wolfyproxy

import android.util.Log
import java.io.DataOutputStream

object RootProxyManager {

    private const val TAG = "RootProxyManager"

    /**
     * Configura el proxy HTTP global.
     * Intenta usar varias keys para ser compatible con más ROMs.
     */
    fun setHttpProxy(ip: String, port: String): Boolean {
        val commands = arrayOf(
            "settings put global http_proxy $ip:$port",
            "settings put global global_http_proxy_host $ip",
            "settings put global global_http_proxy_port $port"
        )
        val ok = runSuCommands(commands)
        Log.d(TAG, "setHttpProxy($ip:$port) → $ok. Valor actual: ${getCurrentProxyRaw()}")
        return ok
    }

    /**
     * Elimina el proxy HTTP global.
     * Borra varias posibles keys y además pone http_proxy en :0 por si alguna ROM lo usa así.
     */
    fun clearHttpProxy(): Boolean {
        val commands = arrayOf(
            "settings delete global http_proxy",
            "settings delete global global_http_proxy_host",
            "settings delete global global_http_proxy_port",
            // Algunas ROMs lo consideran “off” si está en :0
            "settings put global http_proxy :0"
        )
        val ok = runSuCommands(commands)
        Log.d(TAG, "clearHttpProxy() → $ok. Valor actual: ${getCurrentProxyRaw()}")
        return ok
    }

    /**
     * Ejecuta varios comandos con una única sesión 'su'.
     */
    private fun runSuCommands(commands: Array<String>): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            for (cmd in commands) {
                Log.d(TAG, "Ejecutando (su): $cmd")
                os.writeBytes("$cmd\n")
            }
            os.writeBytes("exit\n")
            os.flush()

            val exitCode = process.waitFor()
            Log.d(TAG, "SU exitCode = $exitCode")
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error ejecutando comandos su: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene el valor crudo de 'settings get global http_proxy' usando su -c.
     * Solo para debug/log.
     */
    fun getCurrentProxyRaw(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "settings get global http_proxy"))
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            val exitCode = process.waitFor()
            Log.d(TAG, "getCurrentProxyRaw() exitCode=$exitCode, output='$output'")
            output
        } catch (e: Exception) {
            Log.e(TAG, "Error en getCurrentProxyRaw: ${e.message}", e)
            "error"
        }
    }
}
