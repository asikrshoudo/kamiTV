package com.thekami.kamitv.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thekami.kamitv.server.ServerEvent
import com.thekami.kamitv.server.WebSocketServer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

enum class Tab { REMOTE, KEYBOARD, FILES, SCREENSHARE }

data class HomeUiState(
    val currentTab: Tab = Tab.REMOTE,
    val lastKey: String = "",
    val typedText: String = "",
    val connectedClients: Int = 1,
)

class HomeViewModel(private val server: WebSocketServer) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _onDisconnected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onDisconnected: SharedFlow<Unit> = _onDisconnected

    init {
        viewModelScope.launch {
            server.connectedClients.collect { count ->
                _uiState.update { it.copy(connectedClients = count) }
                if (count == 0) _onDisconnected.emit(Unit)
            }
        }
        viewModelScope.launch {
            server.events.collect { event ->
                when (event) {
                    is ServerEvent.ClientDisconnected -> _onDisconnected.emit(Unit)
                    is ServerEvent.CommandReceived -> handleCommand(event)
                    else -> {}
                }
            }
        }
    }

    private fun handleCommand(event: ServerEvent.CommandReceived) {
        when (event.type) {
            "key" -> {
                val key = event.payload["key"]?.jsonPrimitive?.contentOrNull ?: return
                _uiState.update { it.copy(lastKey = key, currentTab = Tab.REMOTE) }
            }
            "text" -> {
                val text = event.payload["text"]?.jsonPrimitive?.contentOrNull ?: return
                _uiState.update { it.copy(typedText = it.typedText + text, currentTab = Tab.KEYBOARD) }
            }
            "backspace" -> _uiState.update { it.copy(typedText = it.typedText.dropLast(1)) }
            "clear"     -> _uiState.update { it.copy(typedText = "") }
        }
    }

    fun selectTab(tab: Tab) = _uiState.update { it.copy(currentTab = tab) }

    companion object {
        fun factory(server: WebSocketServer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(server) as T
        }
    }
}
