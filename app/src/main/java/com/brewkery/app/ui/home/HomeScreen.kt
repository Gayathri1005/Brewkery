package com.brewkery.app.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brewkery.app.data.model.Category
import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.Meta
import com.brewkery.app.ui.components.BadgePill
import com.brewkery.app.ui.components.CircleIconButton
import com.brewkery.app.ui.components.ErrorState
import com.brewkery.app.ui.components.NetworkImage
import com.brewkery.app.ui.components.PriceText
import com.brewkery.app.ui.components.brewCard
import com.brewkery.app.ui.components.toMoney
import com.brewkery.app.ui.theme.BrewColors
import com.brewkery.app.viewmodel.CartUiState
import com.brewkery.app.viewmodel.MenuUiState
import com.brewkery.app.viewmodel.MenuViewModel
import com.brewkery.app.viewmodel.Order
import com.brewkery.app.viewmodel.filterItems

@Composable
fun HomeScreen(
    menuViewModel: MenuViewModel,
    cartState: CartUiState,
    activeOrder: Order?,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onTrackClick: () -> Unit
) {
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()
    val query by menuViewModel.query.collectAsStateWithLifecycle()
    val selectedCategory by menuViewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val state = uiState

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp,
                bottom = if (cartState.itemCount > 0) 104.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HomeTopBar(cartState.itemCount, onCartClick) }

            val meta = (state as? MenuUiState.Success)?.meta
            if (activeOrder != null || meta != null) {
                item { StatusBanner(activeOrder, meta, onTrackClick) }
            }

            item { SearchField(query, menuViewModel::onQueryChange) }

            when (state) {
                MenuUiState.Loading -> items(4) { SkeletonCard() }

                is MenuUiState.Error -> item {
                    Box(Modifier.brewCard()) { ErrorState(state.message, onRetry = menuViewModel::load) }
                }

                is MenuUiState.Success -> {
                    item {
                        CategoryRow(state.categories, selectedCategory, menuViewModel::onCategorySelected)
                    }
                    val visible = filterItems(state.items, selectedCategory, query)
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                "No items match your search.",
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = BrewColors.TextSecondary
                            )
                        }
                    } else {
                        items(visible, key = { it.id }) { item ->
                            MenuItemCard(item, onClick = { onItemClick(item.id) })
                        }
                    }
                }
            }
        }

        if (cartState.itemCount > 0) {
            CartBar(
                count = cartState.itemCount,
                subtotal = cartState.totals.subtotal,
                onClick = onCartClick,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
    }
}

@Composable
private fun HomeTopBar(cartCount: Int, onCartClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(BrewColors.Dark),
            contentAlignment = Alignment.Center
        ) {
            Text("BK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Fresh Roast & Bakes", fontSize = 13.sp, color = BrewColors.TextSecondary)
            Text("Brewkery Artisans", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BrewColors.Dark)
        }
        Box(contentAlignment = Alignment.TopEnd) {
            CircleIconButton(Icons.Filled.ShoppingBag, "Open cart", onCartClick, size = 44.dp)
            Box(
                Modifier.offset(x = 4.dp, y = (-4).dp).size(20.dp).clip(CircleShape).background(BrewColors.Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(cartCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusBanner(activeOrder: Order?, meta: Meta?, onTrackClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val isOrder = activeOrder != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BrewColors.BannerBg)
            .border(1.dp, BrewColors.Border, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isOrder) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(BrewColors.Green))
            } else {
                Icon(Icons.Filled.TwoWheeler, null, tint = BrewColors.Pink, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isOrder) "ACTIVE ORDER" else "STORE INFO",
                    color = if (isOrder) BrewColors.Green else BrewColors.Accent,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp
                )
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFE5483A)))
            }
            if (activeOrder != null) {
                Text("Active Order #${activeOrder.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrewColors.Dark)
                val eta = activeOrder.arrivalMinutes?.let { " (Arriving in $it mins)" }.orEmpty()
                Text("Preparing$eta", fontSize = 12.sp, color = BrewColors.TextSecondary)
            } else {
                val time = meta?.estimatedDeliveryTime?.replace(" - ", " – ")
                Text(
                    if (time != null) "Delivery in $time" else "Delivery available",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrewColors.Dark
                )
                meta?.deliveryFee?.let {
                    Text("${it.toMoney()} flat fee", fontSize = 12.sp, color = BrewColors.TextSecondary)
                }
            }
        }
        if (isOrder) {
            BannerButton("Track", filled = true, onClick = onTrackClick)
        } else {
            BannerButton("Open", filled = false, onClick = {})
        }
    }
}

@Composable
private fun BannerButton(text: String, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Text(
        text = text,
        modifier = Modifier
            .clip(shape)
            .background(if (filled) BrewColors.Green else Color.White)
            .then(if (filled) Modifier else Modifier.border(1.dp, BrewColors.Border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = if (filled) Color.White else BrewColors.Dark,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .brewCard(shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = BrewColors.TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Search roast, cold brew, pastry...", color = BrewColors.TextSecondary, fontSize = 15.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = BrewColors.Dark, fontSize = 15.sp),
                cursorBrush = SolidColor(BrewColors.Accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CategoryRow(categories: List<Category>, selectedId: String?, onSelect: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CategoryChip("All Items", selected = selectedId == null) { onSelect(null) } }
        items(categories, key = { it.id.orEmpty() }) { category ->
            CategoryChip(category.chipText, selected = selectedId == category.id) { onSelect(category.id) }
        }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text = text,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) BrewColors.Dark else Color.White)
            .then(if (selected) Modifier else Modifier.border(1.dp, BrewColors.Border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = if (selected) Color.White else BrewColors.TextSecondary,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 15.sp,
        maxLines = 1
    )
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier.brewCard().clickable(onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NetworkImage(
            url = item.imageUrl,
            contentDescription = item.displayName,
            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item.badge?.takeIf { it.isNotBlank() }?.let { BadgePill(it) }
            Text(item.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrewColors.Dark)
            item.rating?.let { rating ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = BrewColors.Star, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    val reviews = item.reviewCount?.let { " ($it)" }.orEmpty()
                    Text("$rating$reviews", fontSize = 12.sp, color = BrewColors.TextSecondary)
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                PriceText(item.price.toMoney(), fontSize = 14)
                Text(
                    "+ Customize",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrewColors.Accent)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartBar(count: Int, subtotal: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BrewColors.Dark)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(BrewColors.Accent),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("View Your Cart", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            Text(subtotal.toMoney(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text("Proceed to Checkout →", color = BrewColors.Amber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    val block = Color(0xFFEBDDD4).copy(alpha = alpha)
    Row(Modifier.brewCard().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(block))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.width(70.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.fillMaxWidth(0.7f).height(18.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.width(60.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(block))
        }
    }
}
