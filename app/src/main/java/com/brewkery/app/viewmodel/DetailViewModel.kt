package com.brewkery.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.MilkOption
import com.brewkery.app.data.model.SizeOption
import com.brewkery.app.data.repository.AppContainer
import com.brewkery.app.data.repository.MenuRepository
import com.brewkery.app.data.repository.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val item: MenuItem? = null,
    val size: SizeOption? = null,
    val milk: MilkOption? = null,
    val sugar: String? = null,
    val quantity: Int = 1
) {
    /** Base price plus the selected size and milk/spread extras. */
    val unitPrice: Double
        get() = CartCalculator.unitPrice(item?.price ?: 0.0, size?.extra, milk?.extra)
    val totalPrice: Double get() = CartCalculator.round2(unitPrice * quantity)
}

class DetailViewModel(
    private val repository: MenuRepository,
    private val itemId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            repository.getItem(itemId).fold(
                onSuccess = { item ->
                    // Pre-select the first option of each group, as shown in the design.
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        item = item,
                        size = item.sizeOptions.firstOrNull(),
                        milk = item.milkOptions.firstOrNull(),
                        sugar = item.sugarLevels.firstOrNull()
                    )
                },
                onFailure = {
                    _uiState.value = DetailUiState(isLoading = false, error = it.toUserMessage())
                }
            )
        }
    }

    fun selectSize(size: SizeOption) = _uiState.update { it.copy(size = size) }
    fun selectMilk(milk: MilkOption) = _uiState.update { it.copy(milk = milk) }
    fun selectSugar(sugar: String) = _uiState.update { it.copy(sugar = sugar) }
    fun increaseQuantity() = _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(MAX_QUANTITY)) }
    fun decreaseQuantity() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    companion object {
        private const val MAX_QUANTITY = 20

        fun factory(itemId: Int) = viewModelFactory {
            initializer { DetailViewModel(AppContainer.repository, itemId) }
        }
    }
}
