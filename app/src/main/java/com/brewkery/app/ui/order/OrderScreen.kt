package com.brewkery.app.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.ui.components.brewCard
import com.brewkery.app.ui.theme.BrewColors
import com.brewkery.app.viewmodel.Order

@Composable
fun OrderScreen(order: Order?, onBackToMenu: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.5f))

        Box(
            Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BrewColors.AccentSoft)
                .border(2.dp, BrewColors.Accent, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("☕", fontSize = 38.sp) }
        Spacer(Modifier.height(12.dp))
        Text(
            "ORDER DISPATCHED", color = BrewColors.Accent, fontSize = 12.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp
        )
        Text("Brewing in Progress!", fontSize = 26.sp, fontWeight = FontWeight.Black, color = BrewColors.Dark)
        Spacer(Modifier.height(4.dp))
        Text("Your ticket was dispatched to our barista.", fontSize = 14.sp, color = BrewColors.TextSecondary, textAlign = TextAlign.Center)

        Spacer(Modifier.weight(1f))

        if (order != null) TicketCard(order) else Text("No active order.", color = BrewColors.TextSecondary)

        Spacer(Modifier.weight(1.4f))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BrewColors.Dark)
                .clickable(onClick = onBackToMenu),
            contentAlignment = Alignment.Center
        ) {
            Text("Back to Menu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun TicketCard(order: Order) {
    Column(Modifier.brewCard(RoundedCornerShape(20.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ORDER TICKET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrewColors.TextSecondary, letterSpacing = 0.4.sp)
                Text("#${order.id}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrewColors.Dark)
            }
            Text(
                "PREPARING",
                modifier = Modifier.clip(CircleShape).background(BrewColors.BadgeBg).padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color(0xFFA9742F), fontWeight = FontWeight.Bold, fontSize = 12.sp
            )
        }
        Divider()
        InfoRow("Estimated Wait:", order.estimatedWait.replace(Regex("\\bmins?\\b"), "minutes"), BrewColors.Accent)
        InfoRow("Items Ordered:", "${order.itemCount} Item(s)", BrewColors.Dark)
        Divider()
        Column {
            Text("Status:", fontSize = 12.sp, color = BrewColors.TextSecondary)
            Text("Barista accepted your order!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrewColors.Green)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 15.sp, color = BrewColors.TextSecondary)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(BrewColors.Border))
}
