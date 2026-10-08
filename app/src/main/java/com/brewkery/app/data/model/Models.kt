package com.brewkery.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTOs mirror https://vivekshah138.github.io/Brewkery/data.json.
 * Every field is nullable because Gson bypasses Kotlin defaults; helper
 * properties expose safe, non-null values to the UI.
 */
data class MenuResponse(
    val meta: Meta? = null,
    val categories: List<Category>? = null,
    val items: List<MenuItem>? = null
)

data class Meta(
    val app: String? = null,
    val tagline: String? = null,
    @SerializedName("currency_symbol") val currencySymbol: String? = null,
    @SerializedName("delivery_fee") val deliveryFee: Double? = null,
    @SerializedName("tax_rate_percent") val taxRatePercent: Double? = null,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: String? = null
)

data class Category(
    val id: String? = null,
    val name: String? = null,
    val icon: String? = null,
    @SerializedName("item_count") val itemCount: Int? = null
) {
    val chipText: String
        get() = listOfNotNull(icon?.takeIf { it.isNotBlank() }, name).joinToString(" ")
}

data class SizeOption(
    val id: String? = null,
    val label: String? = null,
    @SerializedName("extra_price") val extraPrice: Double? = null
) {
    val title: String get() = label.orEmpty()
    val extra: Double get() = extraPrice ?: 0.0
}

data class MilkOption(
    val id: String? = null,
    val name: String? = null,
    @SerializedName("extra_price") val extraPrice: Double? = null
) {
    val title: String get() = name.orEmpty()
    val extra: Double get() = extraPrice ?: 0.0
}

data class Customizations(
    val sizes: List<SizeOption>? = null,
    @SerializedName("sugar_levels") val sugarLevels: List<String>? = null,
    @SerializedName("milk_options") val milkOptions: List<MilkOption>? = null
)

data class MenuItem(
    val id: Int = 0,
    @SerializedName("category_id") val categoryId: String? = null,
    val name: String? = null,
    val tagline: String? = null,
    val description: String? = null,
    @SerializedName("base_price") val basePrice: Double? = null,
    val rating: Double? = null,
    @SerializedName("review_count") val reviewCount: Int? = null,
    @SerializedName("prep_time") val prepTime: String? = null,
    val calories: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    val badge: String? = null,
    val ingredients: List<String>? = null,
    val customizations: Customizations? = null
) {
    val displayName: String get() = name.orEmpty()
    val price: Double get() = basePrice ?: 0.0
    val ingredientList: List<String> get() = ingredients.orEmpty().filter { it.isNotBlank() }
    val sizeOptions: List<SizeOption>
        get() = customizations?.sizes.orEmpty().filter { !it.label.isNullOrBlank() }
    val milkOptions: List<MilkOption>
        get() = customizations?.milkOptions.orEmpty().filter { !it.name.isNullOrBlank() }
    val sugarLevels: List<String>
        get() = customizations?.sugarLevels.orEmpty().filter { it.isNotBlank() }
}
