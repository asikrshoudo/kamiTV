package com.thekami.kamitv.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.*
import com.thekami.kamitv.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    state: UpdateState,
    onUpdate: (UpdateInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val show = state is UpdateState.Available
            || state is UpdateState.Downloading
            || state is UpdateState.ReadyToInstall

    if (!show) return

    Dialog(onDismissRequest = { if (state is UpdateState.Available) onDismiss() }) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, Outline, RoundedCornerShape(20.dp))
                .padding(32.dp)
                .widthIn(min = 420.dp, max = 560.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                when (state) {
                    is UpdateState.Available -> {
                        Text("🆕  Update Available",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        Text("Version ${state.info.latestVersion} is ready to install.",
                            fontSize = 15.sp, color = OnSurface)
                        if (state.info.releaseNotes.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceVariant)
                                    .padding(14.dp)
                            ) {
                                Text(state.info.releaseNotes,
                                    fontSize = 13.sp, color = OnSurfaceDim, lineHeight = 20.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onUpdate(state.info) },
                                colors = ButtonDefaults.colors(
                                    containerColor = Primary,
                                    focusedContainerColor = Primary,
                                    contentColor = Background,
                                    focusedContentColor = Background,
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(10.dp)),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            ) { Text("Update Now", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.colors(
                                    containerColor = SurfaceVariant,
                                    focusedContainerColor = SurfaceVariant,
                                    contentColor = OnSurface,
                                    focusedContentColor = OnSurface,
                                ),
                                shape = ButtonDefaults.shape(RoundedCornerShape(10.dp)),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            ) { Text("Later") }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Text("⬇  Downloading Update…",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Primary,
                            trackColor = SurfaceVariant,
                        )
                        Text("Please wait, do not turn off the TV.",
                            fontSize = 14.sp, color = OnSurfaceDim)
                    }
                    is UpdateState.ReadyToInstall -> {
                        Text("✅  Ready to Install",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        Text("The installer will open shortly.",
                            fontSize = 15.sp, color = OnSurface)
                    }
                    else -> {}
                }
            }
        }
    }
}
