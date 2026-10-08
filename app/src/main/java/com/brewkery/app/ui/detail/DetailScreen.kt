package com.brewkery.app.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.ui.components.CircleIconButton
import com.brewkery.app.ui.components.ErrorState
import com.brewkery.app.ui.components.GradientButton
import com.brewkery.app.ui.components.NetworkImage
import com.brewkery.app.ui.components.PriceText
import com.brewkery.app.ui.components.QuantityStepper
import com.brewkery.app.ui.components.brewCard
import com.brewkery.app.ui.components.toMoney
import com.brewkery.app.ui.theme.BrewColors
import com.brewkery.app.viewmodel.DetailUiState
import com.brewkery.app.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onAddToCart: (DetailUiState) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isFavorite by rememberSaveable { mutableStateOf(true) }
    val item = state.item

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            CircleIconButton(
                Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack,
                Modifier.align(Alignment.CenterStart)
            )
            Text(
                "ITEM CUSTOMIZER",
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp,
                color = BrewColors.Dark
            )
            CircleIconButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favourite",
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier.align(Alignment.CenterEnd),
                tint = BrewColors.Pink
            )
        }

        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(color = BrewColors.Accent)
            }
            item == null -> ErrorState(
                message = state.error ?: "This item could not be loaded.",
                onRetry = viewModel::load,
                modifier = Modifier.weight(1f)
            )
            else -> {
                DetailContent(item, state, viewModel, Modifier.weight(1f))
                DetailBottomBar(state, viewModel, onAddToCart)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    item: MenuItem,
    state: DetailUiState,
    viewModel: DetailViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp))) {
            NetworkImage(item.imageUrl, item.displayName, Modifier.fillMaxSize())
            item.badge?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99140B07))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = BrewColors.Amber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                item.displayName,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = BrewColors.Dark
            )
            PriceText(state.unitPrice.toMoney(), fontSize = 20)
        }

        item.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, fontSize = 15.sp, lineHeight = 23.sp, color = BrewColors.TextSecondary)
        }

        if (item.ingredientList.isNotEmpty()) {
            Text(
                "KEY INGREDIENTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = BrewColors.TextSecondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.ingredientList.forEach { IngredientChip(it) }
            }
        }

        if (item.sizeOptions.isNotEmpty()) {
            OptionSection("Size Selection") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.sizeOptions.forEach { size ->
                        val selected = size == state.size
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .optionBox(selected)
                                .clickable { viewModel.selectSize(size) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                size.title, fontSize = 13.sp, textAlign = TextAlign.Center,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) BrewColors.Accent else BrewColors.TextSecondary
                            )
                            Text(
                                "+" + size.extra.toMoney(), fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                color = if (selected) BrewColors.Accent else BrewColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (item.milkOptions.isNotEmpty()) {
            OptionSection("Milk Options / Spreads") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.milkOptions.forEach { milk ->
                        val selected = milk == state.milk
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .optionBox(selected)
                                .clickable { viewModel.selectMilk(milk) }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                milk.title, modifier = Modifier.weight(1f), fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) BrewColors.Accent else BrewColors.TextSecondary
                            )
                            Text(
                                "+" + milk.extra.toMoney(), fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                color = if (selected) BrewColors.Accent else BrewColors.Dark
                            )
                        }
                    }
                }
            }
        }

        if (item.sugarLevels.isNotEmpty()) {
            OptionSection("Sugar Levels / Serving") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.sugarLevels.forEach { level ->
                        val selected = level == state.sugar
                        val shape = RoundedCornerShape(12.dp)
                        Text(
                            level,
                            modifier = Modifier
                                .clip(shape)
                                .background(if (selected) BrewColors.Dark else Color.White)
                                .then(if (selected) Modifier else Modifier.border(1.dp, BrewColors.Border, shape))
                                .clickable { viewModel.selectSugar(level) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else BrewColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.optionBox(selected: Boolean): Modifier {
    val shape = RoundedCornerShape(10.dp)
    return this
        .clip(shape)
        .background(if (selected) BrewColors.AccentSoft else Color.White)
        .border(1.dp, if (selected) BrewColors.Accent else BrewColors.Border, shape)
}

@Composable
private fun OptionSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.brewCard().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrewColors.Dark)
        content()
    }
}

@Composable
private fun IngredientChip(text: String) {
    val shape = RoundedCornerShape(10.dp)
    Text(
        text,
        modifier = Modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BrewColors.Border, shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        fontSize = 12.sp,
        color = BrewColors.TextSecondary
    )
}

@Composable
private fun DetailBottomBar(
    state: DetailUiState,
    viewModel: DetailViewModel,
    onAddToCart: (DetailUiState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(PaddingValues(horizontal = 16.dp, vertical = 12.dp)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuantityStepper(
            quantity = state.quantity,
            onDecrease = viewModel::decreaseQuantity,
            onIncrease = viewModel::increaseQuantity,
            buttonSize = 40.dp
        )
        GradientButton(
            text = "Add to Cart  •  ${state.totalPrice.toMoney()}",
            onClick = { onAddToCart(state) },
            modifier = Modifier.weight(1f)
        )
    }
}
