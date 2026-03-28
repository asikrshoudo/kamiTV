package com.thekami.kamitv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.thekami.kamitv.home.HomeScreen
import com.thekami.kamitv.pairing.PairingScreen
import com.thekami.kamitv.ui.theme.KamiTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KamiTVTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }
                if (sessionId == null) {
                    PairingScreen(onConnected = { sessionId = it })
                } else {
                    HomeScreen(connectedSessionId = sessionIdcat > app/src/main/kotlin/com/thekami/kamitv/server/NsdBroadcaster.kt << 'EOF'
package com.thekami.kamitv.server

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NsdBroadcaster(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var listener: NsdManager.RegistrationListener? = null

    fun start(port: Int) {
        val info = NsdServiceInfo().apply {
            serviceName = "KamiTV"
            serviceType = "_kamiTV._tcp."
            setPort(port)
        }
        listener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(i: NsdServiceInfo, e: Int) = Log.e("NSD", "Failed: $e")
            override fun onUnregistrationFailed(i: NsdServiceInfo, e: Int) {}
            override fun onServiceRegistered(i: NsdServiceInfo) = Log.i("NSD", "Registered: ${i.serviceName}")
            override fun onServiceUnregistered(i: NsdServiceInfo) {}
        }
        nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
    }

    fun stop() {
        listener?.let { runCatching { nsdManager.unregisterService(it) } }
        listener = null
    }
}
EOF, onDisconnected = { sessionId = null })
                }
            }
        }
    }
}
