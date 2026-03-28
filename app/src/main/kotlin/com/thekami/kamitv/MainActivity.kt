package com.thekami.kamitv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.thekami.kamitv.home.HomeScreen
import com.thekami.kamitv.pairing.PairingScreen
import com.thekami.kamitv.pairing.PairingViewModel
import com.thekami.kamitv.ui.theme.KamiTVTheme

class MainActivity : ComponentActivity() {
    private val pairingViewModel: PairingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KamiTVTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }
                if (sessionId == null) {
                    PairingScreen(
                        onConnected = { sessionId = it },
                        viewModel = pairingViewModel,
                    )
                } else {
                    HomeScreen(
                        server = pairingViewModel.server,
                        connectedSessionId = sessionId!!,
                        onDisconnected = { sessionId = null },
                    )
                }
            }
        }
    }
}
