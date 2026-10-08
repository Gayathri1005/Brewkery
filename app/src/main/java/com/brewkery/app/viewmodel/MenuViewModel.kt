package com.brewkery.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brewkery.app.data.model.Category
import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.Meta
import com.brewkery.app.data.repository.AppContainer
import com.brewkery.app.data.repository.MenuRepository
import com.brewkery.app.data.repository.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MenuUiState {
    object Loading : MenuUiState
    data class Success(
        val meta: Meta?,
        val categories: List<Category>,
        val items: List<MenuItem>
    ) : MenuUiState
    data class Error(val message: String) : MenuUiState
}

fun filterItems(items: List<MenuItem>, categoryId: String?, query: String): List<MenuItem> {
    val q = query.trim()
    return items.filter { item ->
        (categoryId == null || item.categoryId == categoryId) &&
            (q.isEmpty() ||
                item.displayName.contains(q, ignoreCase = true) ||
                item.tagline.orEmpty().contains(q, ignoreCase = true) ||
                item.description.orEmpty().contains(q, ignoreCase = true))
    }
}

class MenuViewModel(private val repository: MenuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            repository.getMenu(forceRefresh = true).fold(
                onSuccess = { menu ->
                    _uiState.value = MenuUiState.Success(
                        meta = menu.meta,
                        categories = menu.categories.orEmpty(),
                        items = menu.items.orEmpty()
                    )
                },
                onFailure = { _uiState.value = MenuUiState.Error(it.toUserMessage()) }
            )
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategorySelected(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MenuViewModel(AppContainer.repository) }
        }
    }
}
