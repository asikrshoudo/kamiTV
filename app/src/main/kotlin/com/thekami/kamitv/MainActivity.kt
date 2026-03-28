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
import com.thekami.kamitv.update.UpdateDialog
import com.thekami.kamitv.update.UpdateViewModel

class MainActivity : ComponentActivity() {
    private val pairingViewModel: PairingViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateViewModel.checkForUpdate()
        setContent {
            KamiTVTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }
                val updateState by updateViewModel.state.collectAsState()

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

                UpdateDialog(
                    state = updateState,
                    onUpdate = { updateViewModel.downloadAndInstall(it) },
                    onDismiss = { updateViewModel.dismiss() },
                )
            }
        }
    }
}
