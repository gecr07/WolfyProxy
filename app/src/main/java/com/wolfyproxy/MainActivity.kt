package com.wolfyproxy

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wolfyproxy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PREF_NAME = "wolfyproxy_prefs"
    private val KEY_IP = "ip"
    private val KEY_PORT = "port"
    private val KEY_ENABLED = "enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadProxyConfig()


        binding.btnStartProxy.setOnClickListener {
            val ip = binding.etIp.text.toString().trim()
            val port = binding.etPort.text.toString().trim()

            if (ip.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "Ingresa IP y puerto primero ⚠️", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProxyConfig(ip, port)

            Toast.makeText(this, "Solicitando root para configurar proxy…", Toast.LENGTH_SHORT).show()

            Thread {
                val ok = RootProxyManager.setHttpProxy(ip, port)
                val current = RootProxyManager.getCurrentProxyRaw()

                runOnUiThread {
                    if (ok) {
                        setEnabled(true)
                        Toast.makeText(
                            this,
                            "Proxy configurado: $ip:$port ✅\nsettings=http_proxy='$current'",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al configurar proxy. ¿Root ok?\nsettings=http_proxy='$current' ❌",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }


        binding.btnStopProxy.setOnClickListener {

            Toast.makeText(this, "Solicitando root para quitar proxy…", Toast.LENGTH_SHORT).show()

            Thread {
                val ok = RootProxyManager.clearHttpProxy()
                val current = RootProxyManager.getCurrentProxyRaw()

                runOnUiThread {
                    if (ok) {
                        setEnabled(false)
                        Toast.makeText(
                            this,
                            "Proxy eliminado 🧹\nsettings=http_proxy='$current'",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al quitar proxy. Revisa root.\nsettings=http_proxy='$current' ❌",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }

        // Salir
        binding.btnExit.setOnClickListener {
            finish()
        }
    }


    private fun saveProxyConfig(ip: String, port: String) {
        val sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_IP, ip)
            .putString(KEY_PORT, port)
            .apply()
    }

    // Carga IP, puerto y estado al abrir la app
    private fun loadProxyConfig() {
        val sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ip = sp.getString(KEY_IP, "") ?: ""
        val port = sp.getString(KEY_PORT, "") ?: ""
        val enabled = sp.getBoolean(KEY_ENABLED, false)

        binding.etIp.setText(ip)
        binding.etPort.setText(port)
        updateStatus(enabled)
    }


    private fun setEnabled(enabled: Boolean) {
        val sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
        updateStatus(enabled)
    }


    private fun updateStatus(enabled: Boolean) {
        if (enabled) {
            binding.tvStatus.text = "Proxy ACTIVO"
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            binding.tvStatus.text = "Proxy DESACTIVADO"
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }
    }
}
