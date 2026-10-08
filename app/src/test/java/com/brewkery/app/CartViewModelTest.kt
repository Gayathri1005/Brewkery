package com.brewkery.app

import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.MilkOption
import com.brewkery.app.data.model.SizeOption
import com.brewkery.app.viewmodel.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CartViewModelTest {
    private val delta = 0.001
    private val latte = MenuItem(id = 1, name = "Latte", basePrice = 4.85)
    private val grande = SizeOption("sz_medium", "Grande", 0.65)
    private val almond = MilkOption("m_almond", "Almond", 0.50)

    private fun viewModel() = CartViewModel { "BK-12345" }

    @Test
    fun `adding item with extras updates totals`() {
        val vm = viewModel()
        vm.addToCart(latte, grande, almond, "50% Mild", quantity = 2)
        val state = vm.uiState.value
        assertEquals(2, state.itemCount)
        assertEquals(12.00, state.totals.subtotal, delta)   // (4.85 + 0.65 + 0.50) * 2
        assertEquals(0.96, state.totals.tax, delta)
        assertEquals(15.46, state.totals.total, delta)
    }

    @Test
    fun `identical customisations merge into one line`() {
        val vm = viewModel()
        vm.addToCart(latte, grande, almond, "50% Mild", 1)
        vm.addToCart(latte, grande, almond, "50% Mild", 1)
        assertEquals(1, vm.uiState.value.lines.size)
        assertEquals(2, vm.uiState.value.lines.first().quantity)
    }

    @Test
    fun `increase and decrease change quantity and decrease at one removes`() {
        val vm = viewModel()
        vm.addToCart(latte, null, null, null, 1)
        val id = vm.uiState.value.lines.first().lineId
        vm.increase(id)
        assertEquals(2, vm.uiState.value.itemCount)
        vm.decrease(id)
        vm.decrease(id)
        assertTrue(vm.uiState.value.lines.isEmpty())
    }

    @Test
    fun `placing order clears cart and creates active order`() {
        val vm = viewModel()
        assertNull(vm.placeOrder())
        vm.addToCart(latte, null, null, null, 3)
        val order = vm.placeOrder()
        assertNotNull(order)
        assertEquals("BK-12345", order!!.id)
        assertEquals(3, order.itemCount)
        assertTrue(vm.uiState.value.lines.isEmpty())
        assertEquals(order, vm.activeOrder.value)
    }
}
