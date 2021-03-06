package com.woocommerce.android.ui.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.woocommerce.android.R
import com.woocommerce.android.di.GlideApp
import com.woocommerce.android.model.Product
import com.woocommerce.android.ui.products.ProductStockStatus.InStock
import com.woocommerce.android.ui.products.ProductStockStatus.OnBackorder
import com.woocommerce.android.ui.products.ProductStockStatus.OutOfStock
import com.woocommerce.android.ui.products.ProductType.VARIABLE
import kotlinx.android.synthetic.main.product_list_item.view.*
import org.wordpress.android.util.FormatUtils
import org.wordpress.android.util.HtmlUtils
import org.wordpress.android.util.PhotonUtils

class ProductItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(
        parent.context
    ).inflate(R.layout.product_list_item, parent, false)
) {
    private val imageSize = parent.context.resources.getDimensionPixelSize(R.dimen.image_minor_100)
    private val bullet = "\u2022"
    private val statusColor = ContextCompat.getColor(parent.context, R.color.product_status_fg_other)
    private val statusPendingColor = ContextCompat.getColor(parent.context, R.color.product_status_fg_pending)

    fun bind(product: Product) {
        val context = itemView.context

        itemView.productName.text = if (product.name.isEmpty()) {
            context.getString(R.string.untitled)
        } else {
            HtmlUtils.fastStripHtml(product.name)
        }

        val stockAndStatus = getProductStockStatusText(context, product)
        with(itemView.productStockAndStatus) {
            if (stockAndStatus != null) {
                visibility = View.VISIBLE
                text = HtmlCompat.fromHtml(
                    stockAndStatus,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } else {
                visibility = View.GONE
            }
        }

        val firstImage = product.firstImageUrl
        val size: Int
        if (firstImage.isNullOrEmpty()) {
            size = imageSize / 2
            itemView.productImage.setImageResource(R.drawable.ic_product)
        } else {
            size = imageSize
            val imageUrl = PhotonUtils.getPhotonImageUrl(firstImage, imageSize, imageSize)
            GlideApp.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .into(itemView.productImage)
        }
        itemView.productImage.layoutParams.apply {
            height = size
            width = size
        }
    }

    fun setOnDeleteClickListener(
        product: Product,
        onItemDeleted: (product: Product) -> Unit
    ) {
        with(itemView.product_btnDelete) {
            isVisible = true
            setOnClickListener { onItemDeleted.invoke(product) }
        }
    }

    private fun getProductStockStatusText(
        context: Context,
        product: Product
    ): String? {
        val statusHtml = product.status?.let {
            when {
                it == ProductStatus.PENDING -> {
                    "<font color=$statusPendingColor>${product.status.toLocalizedString(context)}</font>"
                }
                it != ProductStatus.PUBLISH -> {
                    "<font color=$statusColor>${product.status.toLocalizedString(context)}</font>"
                }
                else -> {
                    null
                }
            }
        }

        val stock = when (product.stockStatus) {
            InStock -> {
                if (product.type == VARIABLE) {
                    if (product.numVariations > 0) {
                        context.getString(
                            R.string.product_stock_status_instock_with_variations,
                            product.numVariations
                        )
                    } else {
                        context.getString(R.string.product_stock_status_instock)
                    }
                } else {
                    if (product.stockQuantity > 0) {
                        context.getString(
                            R.string.product_stock_count,
                            FormatUtils.formatInt(product.stockQuantity)
                        )
                    } else {
                        context.getString(R.string.product_stock_status_instock)
                    }
                }
            }
            OutOfStock -> {
                context.getString(R.string.product_stock_status_out_of_stock)
            }
            OnBackorder -> {
                context.getString(R.string.product_stock_status_on_backorder)
            }
            else -> {
                product.stockStatus.value
            }
        }

        return if (statusHtml != null) "$statusHtml $bullet $stock" else stock
    }
}
