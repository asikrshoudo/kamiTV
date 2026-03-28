package com.thekami.kamitv.pairing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import com.thekami.kamitv.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PairingScreen(
    onConnected: (String) -> Unit,
    viewModel: PairingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onClientConnected.collect { onConnected(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 72.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Text("kamiTV", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnBackground)

                Text(
                    "Open kami-remote on your phone and scan\nthe QR code — or enter the PIN manually.",
                    fontSize = 17.sp, color = OnSurfaceDim, lineHeight = 26.sp,
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("PIN CODE")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.pin.forEach { PinDigit(it.toString()) }
                    }
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = viewModel::regeneratePin,
                        colors = ButtonDefaults.colors(
                            containerColor = SurfaceVariant,
                            focusedContainerColor = Primary,
                            contentColor = OnSurface,
                            focusedContentColor = Background,
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(10.dp)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text("Generate new PIN", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionLabel("TV ADDRESS")
                    Text(
                        "${state.ipAddress}:${state.port}",
                        fontSize = 16.sp, color = OnSurface, fontFamily = FontFamily.Monospace,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(9.dp).clip(CircleShape)
                            .background(if (state.connectedClients > 0) Connected else Disconnected)
                    )
                    Text(
                        if (state.connectedClients > 0) "${state.connectedClients} device connected"
                        else "Waiting for connection…",
                        fontSize = 14.sp, color = OnSurfaceDim,
                    )
                }
            }

            // Right — QR
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.qrBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier.size(236.dp).clip(RoundedCornerShape(20.dp))
                            .background(Color.White).padding(14.dp),
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Pairing QR",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                } ?: Box(
                    modifier = Modifier.size(236.dp).clip(RoundedCornerShape(20.dp))
                        .background(Surface),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
                Text("Scan with kami-remote", fontSize = 13.sp, color = OnSurfaceDim)
            }
        }

        Text("kamiTV v1.0", fontSize = 12.sp, color = Outline,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp))
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PinDigit(digit: String) {
    Box(
        modifier = Modifier.size(width = 54.dp, height = 68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(digit, fontSize = 30.sp, fontWeight = FontWeight.Bold,
            color = Primary, fontFamily = FontFamily.Monospace)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        color = OnSurfaceDim, letterSpacing = 2.sp)
}
