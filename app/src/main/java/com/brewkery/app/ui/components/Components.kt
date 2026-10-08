package com.brewkery.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.brewkery.app.ui.theme.BrewColors
import java.util.Locale

fun Double.toMoney(): String = "\$" + String.format(Locale.US, "%.2f", this)

/** White rounded card with the thin warm border used across the design. */
fun Modifier.brewCard(shape: Shape = RoundedCornerShape(16.dp)): Modifier =
    this.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, BrewColors.Border, shape)

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = BrewColors.Dark
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, BrewColors.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun BadgePill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BrewColors.BadgeBg)
            .padding(horizontal = 6.dp, vertical = 1.dp),
        color = BrewColors.BadgeText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp
    )
}

@Composable
fun PriceText(text: String, modifier: Modifier = Modifier, fontSize: Int = 14) {
    Text(
        text = text,
        modifier = modifier,
        color = BrewColors.Accent,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(BrewColors.Accent, BrewColors.AccentDark)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 36.dp
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BrewColors.Border, shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(Icons.Filled.Remove, "Decrease quantity", onDecrease, buttonSize)
        Text(
            text = quantity.toString(),
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BrewColors.Dark
        )
        StepperButton(Icons.Filled.Add, "Increase quantity", onIncrease, buttonSize)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, onClick: () -> Unit, size: Dp) {
    Box(
        modifier = Modifier.size(size).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = BrewColors.Dark, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color(0xFFEBCFC0),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
    }
}

/** Coil image with graceful loading/error placeholders. */
@Composable
fun NetworkImage(url: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { ImagePlaceholder(showSpinner = true) },
        error = { ImagePlaceholder(showSpinner = false) }
    )
}

@Composable
private fun ImagePlaceholder(showSpinner: Boolean) {
    Box(Modifier.fillMaxSize().background(BrewColors.AccentSoft), contentAlignment = Alignment.Center) {
        if (showSpinner) {
            CircularProgressIndicator(Modifier.size(20.dp), color = BrewColors.Accent, strokeWidth = 2.dp)
        } else {
            Text("☕", fontSize = 24.sp)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("Oops!", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrewColors.Dark)
        Text(message, color = BrewColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(
            text = "Retry",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(BrewColors.Accent)
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
