package com.woocommerce.android.model

import androidx.annotation.StringRes
import com.woocommerce.android.R
import com.woocommerce.android.extensions.formatToString
import com.woocommerce.android.ui.products.ProductType

interface IProduct {
    val length: Float
    val width: Float
    val height: Float
    val weight: Float
    val type: ProductType
    val isVirtual: Boolean

    /**
     * Formats the [Product] weight with the given [weightUnit]
     * for display purposes.
     * Eg: 12oz
     */
    fun getWeightWithUnits(weightUnit: String?): String {
        return if (weight > 0) {
            "${weight.formatToString()}${weightUnit ?: ""}"
        } else ""
    }

    /**
     * Formats the [Product] size (length, width, height) with the given [dimensionUnit]
     * if all the dimensions are available.
     * Eg: 12 x 15 x 13 in
     */
    fun getSizeWithUnits(dimensionUnit: String?): String {
        val hasLength = length > 0
        val hasWidth = width > 0
        val hasHeight = height > 0
        val unit = dimensionUnit ?: ""
        return if (hasLength && hasWidth && hasHeight) {
            "${length.formatToString()} " +
                "x ${width.formatToString()} " +
                "x ${height.formatToString()} $unit"
        } else if (hasWidth && hasHeight) {
            "${width.formatToString()} x ${height.formatToString()} $unit"
        } else {
            ""
        }.trim()
    }

    @StringRes
    fun getProductTypeFormattedForDisplay(): Int {
        return when (this.type) {
            ProductType.SIMPLE -> {
                if (this.isVirtual) R.string.product_type_virtual
                else R.string.product_type_physical
            }
            ProductType.VARIABLE -> R.string.product_type_variable
            ProductType.GROUPED -> R.string.product_type_grouped
            ProductType.EXTERNAL -> R.string.product_type_external
        }
    }
}
