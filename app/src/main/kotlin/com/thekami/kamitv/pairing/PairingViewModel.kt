package com.thekami.kamitv.pairing

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thekami.kamitv.server.NetworkUtils
import com.thekami.kamitv.server.NsdBroadcaster
import com.thekami.kamitv.server.PinManager
import com.thekami.kamitv.server.ServerEvent
import com.thekami.kamitv.server.WebSocketServer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PairingUiState(
    val pin: String = "",
    val ipAddress: String = "",
    val port: Int = 8765,
    val qrBitmap: Bitmap? = null,
    val connectedClients: Int = 0,
)

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    val server = WebSocketServer(port = 8765)
    private val nsd = NsdBroadcaster(application)

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _onClientConnected = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val onClientConnected: SharedFlow<String> = _onClientConnected

    init {
        val ip = NetworkUtils.getLocalIpAddress(application)
        val pin = PinManager.getPin()
        server.start()
        nsd.start(server.port)
        _uiState.update {
            it.copy(
                pin = pin,
                ipAddress = ip,
                port = server.port,
                qrBitmap = buildQr(ip, server.port, pin),
            )
        }
        viewModelScope.launch {
            server.connectedClients.collect { count ->
                _uiState.update { it.copy(connectedClients = count) }
            }
        }
        viewModelScope.launch {
            server.events.collect { event ->
                if (event is ServerEvent.ClientConnected)
                    _onClientConnected.emit(event.sessionId)
            }
        }
    }

    fun regeneratePin() {
        val pin = PinManager.regenerate()
        val s = _uiState.value
        _uiState.update { it.copy(pin = pin, qrBitmap = buildQr(s.ipAddress, s.port, pin)) }
    }

    private fun buildQr(ip: String, port: Int, pin: String): Bitmap =
        QRUtils.generate("""{"ip":"$ip","port":$port,"pin":"$pin","app":"kamiTV"}""")

    override fun onCleared() {
        super.onCleared()
        server.stop()
        nsd.stop()
    }
}
