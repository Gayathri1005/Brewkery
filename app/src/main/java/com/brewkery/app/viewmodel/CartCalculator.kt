package com.brewkery.app.viewmodel

import java.math.BigDecimal
import java.math.RoundingMode

data class CartTotals(
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val total: Double
)

object CartCalculator {
    const val DEFAULT_DELIVERY_FEE = 2.50
    const val DEFAULT_TAX_RATE = 0.08

    fun unitPrice(basePrice: Double, vararg extras: Double?): Double =
        round2(basePrice + extras.sumOf { it ?: 0.0 })

    /**
     * Tax = subtotal x taxRate; Total = subtotal + delivery + tax.
     * An empty cart has no payable total (delivery fee is still reported for display).
     */
    fun calculate(
        lineTotals: List<Double>,
        deliveryFee: Double = DEFAULT_DELIVERY_FEE,
        taxRate: Double = DEFAULT_TAX_RATE
    ): CartTotals {
        if (lineTotals.isEmpty()) return CartTotals(0.0, deliveryFee, 0.0, 0.0)
        val subtotal = round2(lineTotals.sum())
        val tax = round2(subtotal * taxRate)
        return CartTotals(subtotal, deliveryFee, tax, round2(subtotal + deliveryFee + tax))
    }

    fun round2(value: Double): Double =
        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
}
