package com.thekami.kamitv.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import com.thekami.kamitv.server.WebSocketServer
import com.thekami.kamitv.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    server: WebSocketServer,
    connectedSessionId: String,
    onDisconnected: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(server)),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onDisconnected.collect { onDisconnected() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 72.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("kamiTV", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Connected))
                Text(
                    "Remote connected · #$connectedSessionId",
                    fontSize = 14.sp, color = OnSurface,
                )
            }
        }

        // Tab bar
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tab.entries.forEach { tab ->
                val selected = state.currentTab == tab
                Button(
                    onClick = { viewModel.selectTab(tab) },
                    colors = ButtonDefaults.colors(
                        containerColor = if (selected) Primary else SurfaceVariant,
                        focusedContainerColor = Primary,
                        contentColor = if (selected) Background else OnSurface,
                        focusedContentColor = Background,
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(10.dp)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = when (tab) {
                            Tab.REMOTE      -> "🎮  Remote"
                            Tab.KEYBOARD    -> "⌨  Keyboard"
                            Tab.FILES       -> "🗂  Files"
                            Tab.SCREENSHARE -> "📺  Screen Share"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface),
        ) {
            when (state.currentTab) {
                Tab.REMOTE      -> RemotePanel(lastKey = state.lastKey)
                Tab.KEYBOARD    -> KeyboardPanel(typedText = state.typedText)
                Tab.FILES       -> ComingSoonPanel("🗂", "Files")
                Tab.SCREENSHARE -> ComingSoonPanel("📺", "Screen Share")
            }
        }
    }
}

// ── Remote panel ────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RemotePanel(lastKey: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        SectionLabel("D-PAD INPUT")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DpadKey("▲", lastKey == "up")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DpadKey("◀", lastKey == "left")
                DpadKey("OK", lastKey == "select", isCenter = true)
                DpadKey("▶", lastKey == "right")
            }
            DpadKey("▼", lastKey == "down")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DpadKey("⬅  Back", lastKey == "back", wide = true)
            DpadKey("⌂  Home", lastKey == "home", wide = true)
        }

        if (lastKey.isNotEmpty()) {
            Text(
                "last: $lastKey",
                fontSize = 13.sp,
                color = OnSurfaceDim,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun DpadKey(
    label: String,
    active: Boolean,
    isCenter: Boolean = false,
    wide: Boolean = false,
) {
    val shape = if (isCenter) CircleShape else RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .then(
                when {
                    wide     -> Modifier.width(130.dp).height(48.dp)
                    isCenter -> Modifier.size(64.dp)
                    else     -> Modifier.size(56.dp)
                }
            )
            .clip(shape)
            .background(if (active) Primary else SurfaceVariant)
            .border(1.dp, if (active) Primary else Outline, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = if (isCenter) 13.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Background else OnSurface,
        )
    }
}

// ── Keyboard panel ───────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun KeyboardPanel(typedText: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionLabel("KEYBOARD INPUT")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariant)
                .border(1.dp, Outline, RoundedCornerShape(12.dp))
                .padding(20.dp),
        ) {
            if (typedText.isEmpty()) {
                Text(
                    "Waiting for input from phone…",
                    fontSize = 18.sp, color = OnSurfaceDim,
                )
            } else {
                Text(
                    typedText,
                    fontSize = 22.sp,
                    color = OnBackground,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Text(
            "Type on kami-remote — text appears here in real time. Send \"backspace\" or \"clear\" to edit.",
            fontSize = 13.sp, color = OnSurfaceDim,
        )
    }
}

// ── Coming soon ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ComingSoonPanel(icon: String, label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(icon, fontSize = 56.sp)
            Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Text("Coming soon", fontSize = 14.sp, color = OnSurfaceDim)
        }
    }
}

// ── Shared ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = OnSurfaceDim,
        letterSpacing = 2.sp,
    )
}
