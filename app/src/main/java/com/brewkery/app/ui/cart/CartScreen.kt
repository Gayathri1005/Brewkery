package com.brewkery.app.ui.cart

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
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
import com.brewkery.app.ui.components.CircleIconButton
import com.brewkery.app.ui.components.DashedDivider
import com.brewkery.app.ui.components.GradientButton
import com.brewkery.app.ui.components.NetworkImage
import com.brewkery.app.ui.components.PriceText
import com.brewkery.app.ui.components.QuantityStepper
import com.brewkery.app.ui.components.brewCard
import com.brewkery.app.ui.components.toMoney
import com.brewkery.app.ui.theme.BrewColors
import com.brewkery.app.viewmodel.CartLine
import com.brewkery.app.viewmodel.CartUiState
import java.util.Locale

@Composable
fun CartScreen(
    state: CartUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onIncrease: (Long) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onPlaceOrder: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack, Modifier.align(Alignment.CenterStart))
            Text(
                "YOUR CART",
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp,
                letterSpacing = 0.5.sp, color = BrewColors.Dark
            )
            Text(
                "Clear Cart",
                modifier = Modifier.align(Alignment.CenterEnd).clickable(onClick = onClear).padding(4.dp),
                color = BrewColors.Pink, fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
        }

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.lines.isEmpty()) {
                item { EmptyCart() }
            } else {
                items(state.lines, key = { it.lineId }) { line ->
                    CartLineCard(line, onIncrease, onDecrease, onRemove)
                }
            }
        }

        Column(Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            SummaryCard(state)
            Spacer(Modifier.height(14.dp))
            GradientButton(
                text = "Place Order Now  •  ${state.totals.total.toMoney()}",
                onClick = onPlaceOrder,
                enabled = state.lines.isNotEmpty(),
                icon = Icons.Filled.Lock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyCart() {
    Column(
        Modifier.brewCard().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.ShoppingCart, null, tint = Color(0xFFEBD3C7), modifier = Modifier.size(38.dp))
        Text("Your in-memory cart is empty.", color = BrewColors.TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onIncrease: (Long) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    Row(Modifier.brewCard().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NetworkImage(
            line.item.imageUrl, line.item.displayName,
            Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    line.item.displayName, modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrewColors.Dark
                )
                Icon(
                    Icons.Filled.Close, "Remove ${line.item.displayName}",
                    tint = BrewColors.TextSecondary,
                    modifier = Modifier.size(18.dp).clickable { onRemove(line.lineId) }
                )
            }
            if (line.optionsSummary.isNotBlank()) {
                Text(line.optionsSummary, fontSize = 12.sp, color = BrewColors.TextSecondary)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriceText(line.lineTotal.toMoney(), fontSize = 15)
                QuantityStepper(
                    quantity = line.quantity,
                    onDecrease = { onDecrease(line.lineId) },
                    onIncrease = { onIncrease(line.lineId) },
                    buttonSize = 32.dp
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(state: CartUiState) {
    val totals = state.totals
    Column(Modifier.brewCard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryRow("Subtotal", totals.subtotal.toMoney())
        SummaryRow("Delivery Fee", totals.deliveryFee.toMoney())
        SummaryRow("Est. Tax (${String.format(Locale.US, "%.1f", state.taxRatePercent)}%)", totals.tax.toMoney())
        DashedDivider(Modifier.padding(vertical = 2.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrewColors.Dark)
            PriceText(totals.total.toMoney(), fontSize = 20)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = BrewColors.TextSecondary)
        Text(
            value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
            fontSize = 13.sp, color = BrewColors.Dark, textAlign = TextAlign.End
        )
    }
}
