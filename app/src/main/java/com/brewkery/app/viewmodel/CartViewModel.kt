package com.brewkery.app.viewmodel

import androidx.lifecycle.ViewModel
import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.Meta
import com.brewkery.app.data.model.MilkOption
import com.brewkery.app.data.model.SizeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt
import kotlin.random.Random

data class CartLine(
    val lineId: Long,
    val item: MenuItem,
    val size: SizeOption?,
    val milk: MilkOption?,
    val sugar: String?,
    val quantity: Int,
    val unitPrice: Double
) {
    val lineTotal: Double get() = CartCalculator.round2(unitPrice * quantity)

    val optionsSummary: String
        get() = listOfNotNull(size?.title, milk?.title, sugar).joinToString(" • ")
}

data class CartUiState(
    val lines: List<CartLine> = emptyList(),
    val totals: CartTotals = CartCalculator.calculate(emptyList()),
    val itemCount: Int = 0,
    val taxRatePercent: Double = CartCalculator.DEFAULT_TAX_RATE * 100
)

data class Order(
    val id: String,
    val itemCount: Int,
    val estimatedWait: String
) {
    /** Midpoint of a range such as "20 - 30 mins" -> 25. */
    val arrivalMinutes: Int?
        get() = Regex("\\d+").findAll(estimatedWait).map { it.value.toInt() }.toList()
            .takeIf { it.isNotEmpty() }?.average()?.roundToInt()
}

fun defaultOrderId(): String = "BK-${Random.nextInt(10_000, 100_000)}"

/** Activity-scoped, in-memory cart and active order. No persistence by design. */
class CartViewModel(private val orderIdGenerator: () -> String) : ViewModel() {

    constructor() : this(::defaultOrderId)

    private var deliveryFee = CartCalculator.DEFAULT_DELIVERY_FEE
    private var taxRate = CartCalculator.DEFAULT_TAX_RATE
    private var estimatedWait = "20 - 30 mins"
    private var nextLineId = 1L

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    /** Applies delivery fee, tax rate and ETA supplied by the API. */
    fun configure(meta: Meta?) {
        meta?.deliveryFee?.let { deliveryFee = it }
        meta?.taxRatePercent?.let { taxRate = it / 100.0 }
        meta?.estimatedDeliveryTime?.takeIf { it.isNotBlank() }?.let { estimatedWait = it }
        publish(_uiState.value.lines)
    }

    fun addToCart(item: MenuItem, size: SizeOption?, milk: MilkOption?, sugar: String?, quantity: Int) {
        val lines = _uiState.value.lines
        val existing = lines.firstOrNull {
            it.item.id == item.id && it.size?.id == size?.id && it.milk?.id == milk?.id && it.sugar == sugar
        }
        val updated = if (existing != null) {
            lines.map { if (it.lineId == existing.lineId) it.copy(quantity = it.quantity + quantity) else it }
        } else {
            lines + CartLine(
                lineId = nextLineId++,
                item = item,
                size = size,
                milk = milk,
                sugar = sugar,
                quantity = quantity,
                unitPrice = CartCalculator.unitPrice(item.price, size?.extra, milk?.extra)
            )
        }
        publish(updated)
    }

    fun increase(lineId: Long) =
        publish(_uiState.value.lines.map { if (it.lineId == lineId) it.copy(quantity = it.quantity + 1) else it })

    fun decrease(lineId: Long) {
        val lines = _uiState.value.lines
        val line = lines.firstOrNull { it.lineId == lineId } ?: return
        if (line.quantity <= 1) remove(lineId)
        else publish(lines.map { if (it.lineId == lineId) it.copy(quantity = it.quantity - 1) else it })
    }

    fun remove(lineId: Long) = publish(_uiState.value.lines.filterNot { it.lineId == lineId })

    fun clear() = publish(emptyList())

    /** Clears the cart and creates the active order. Returns null when the cart is empty. */
    fun placeOrder(): Order? {
        val state = _uiState.value
        if (state.lines.isEmpty()) return null
        val order = Order(
            id = orderIdGenerator(),
            itemCount = state.itemCount,
            estimatedWait = estimatedWait
        )
        _activeOrder.value = order
        clear()
        return order
    }

    private fun publish(lines: List<CartLine>) {
        _uiState.value = CartUiState(
            lines = lines,
            totals = CartCalculator.calculate(lines.map { it.lineTotal }, deliveryFee, taxRate),
            itemCount = lines.sumOf { it.quantity },
            taxRatePercent = taxRate * 100.0
        )
    }
}
