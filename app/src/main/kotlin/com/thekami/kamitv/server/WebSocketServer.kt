package com.thekami.kamitv.server

import android.util.Log
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

private const val TAG = "WebSocketServer"

sealed class ServerEvent {
    data class ClientConnected(val sessionId: String) : ServerEvent()
    data class ClientDisconnected(val sessionId: String) : ServerEvent()
    data class CommandReceived(val sessionId: String, val type: String, val payload: JsonObject) : ServerEvent()
}

class WebSocketServer(val port: Int = 8765) {
    private var engine: ApplicationEngine? = null
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketSession>()

    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients

    private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ServerEvent> = _events

    fun start() {
        if (engine != null) return
        engine = embeddedServer(CIO, port = port) {
            install(WebSockets) {
                pingPeriod = 20.seconds
                timeout = 40.seconds
                maxFrameSize = 50 * 1024 * 1024L
                masking = false
            }
            routing {
                webSocket("/ws") { handleSession() }
            }
        }.start(wait = false)
        Log.i(TAG, "Started on port $port")
    }

    fun stop() {
        engine?.stop(500, 1000)
        engine = null
        sessions.clear()
        _connectedClients.value = 0
    }

    private suspend fun DefaultWebSocketSession.handleSession() {
        val first = runCatching { incoming.receive() }.getOrNull() ?: return
        if (first !is Frame.Text) { close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "bad frame")); return }

        val pin = runCatching {
            Json.parseToJsonElement(first.readText()).jsonObject["pin"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()

        if (pin == null || !PinManager.verify(pin)) {
            send(Frame.Text("""{"type":"error","message":"Invalid PIN"}"""))
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid PIN"))
            return
        }

        val sessionId = UUID.randomUUID().toString().take(8)
        sessions[sessionId] = this
        _connectedClients.value = sessions.size
        send(Frame.Text("""{"type":"connected","sessionId":"$sessionId"}"""))
        _events.emit(ServerEvent.ClientConnected(sessionId))

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val json = runCatching { Json.parseToJsonElement(frame.readText()).jsonObject }.getOrNull() ?: continue
                    val type = json["type"]?.jsonPrimitive?.contentOrNull ?: continue
                    _events.emit(ServerEvent.CommandReceived(sessionId, type, json))
                }
            }
        } finally {
            sessions.remove(sessionId)
            _connectedClients.value = sessions.size
            _events.emit(ServerEvent.ClientDisconnected(sessionId))
        }
    }

    suspend fun broadcast(message: String) {
        sessions.values.forEach { it.runCatching { send(Frame.Text(message)) } }
    }
}
