package com.brewkery.app

import com.brewkery.app.viewmodel.CartCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class CartCalculatorTest {
    private val delta = 0.001

    @Test
    fun `totals use 8 percent tax and 2_50 delivery`() {
        val totals = CartCalculator.calculate(listOf(4.85, 4.55))
        assertEquals(9.40, totals.subtotal, delta)
        assertEquals(0.75, totals.tax, delta)          // 9.40 * 0.08 = 0.752
        assertEquals(2.50, totals.deliveryFee, delta)
        assertEquals(12.65, totals.total, delta)       // 9.40 + 2.50 + 0.75
    }

    @Test
    fun `round subtotal produces exact tax and total`() {
        val totals = CartCalculator.calculate(listOf(10.0))
        assertEquals(0.80, totals.tax, delta)
        assertEquals(13.30, totals.total, delta)
    }

    @Test
    fun `empty cart has zero payable total`() {
        val totals = CartCalculator.calculate(emptyList())
        assertEquals(0.0, totals.subtotal, delta)
        assertEquals(0.0, totals.tax, delta)
        assertEquals(0.0, totals.total, delta)
        assertEquals(2.50, totals.deliveryFee, delta)
    }

    @Test
    fun `unit price adds size and milk extras`() {
        assertEquals(6.00, CartCalculator.unitPrice(4.85, 0.65, 0.50), delta)
        assertEquals(4.85, CartCalculator.unitPrice(4.85, null, 0.0), delta)
    }
}
