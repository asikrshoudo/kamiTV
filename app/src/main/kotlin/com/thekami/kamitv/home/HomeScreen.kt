package com.thekami.kamitv.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.thekami.kamitv.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    connectedSessionId: String,
    onDisconnected: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background)
            .padding(horizontal = 72.dp, vertical = 48.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("kamiTV", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = OnBackground)

                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(SurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Connected))
                    Text("Remote connected", fontSize = 14.sp, color = OnSurface)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FeatureCard("⌨", "Keyboard", "Type on TV", Modifier.weight(1f))
                FeatureCard("🗂", "Files", "Send from phone", Modifier.weight(1f))
                FeatureCard("📺", "Screen Share", "View TV on phone", Modifier.weight(1f))
                FeatureCard("🎮", "Remote", "D-pad & controls", Modifier.weight(1f))
            }
        }

        Text("session: $connectedSessionId", fontSize = 11.sp, color = Outline,
            modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FeatureCard(icon: String, title: String, subtitle: String, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Surface).padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, fontSize = 32.sp)
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
            Text(subtitle, fontSize = 13.sp, color = OnSurfaceDim)
        }
    }
}
