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
