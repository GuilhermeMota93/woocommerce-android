package com.woocommerce.android.ui.products

import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
import com.woocommerce.android.extensions.addIfNotEmpty
import com.woocommerce.android.extensions.filterNotEmpty
import com.woocommerce.android.model.Product
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductDescriptionEditor
import com.woocommerce.android.ui.products.models.ProductProperty
import com.woocommerce.android.ui.products.models.ProductProperty.ComplexProperty
import com.woocommerce.android.ui.products.models.ProductProperty.Editable
import com.woocommerce.android.ui.products.models.ProductPropertyCard
import com.woocommerce.android.ui.products.models.ProductPropertyCard.Type.PRIMARY
import com.woocommerce.android.viewmodel.ResourceProvider

class AddProductDetailCardBuilder(
    private val viewModel: ProductDetailViewModel,
    private val resources: ResourceProvider) {
    fun buildPropertyCards(): List<ProductPropertyCard> {
        val cards = mutableListOf<ProductPropertyCard>()

        cards.addIfNotEmpty(getPrimaryCard())

        return cards
    }

    fun updateCards(description: String) {
        getPrimaryCard(updatedDescription = description)
    }

    private fun getPrimaryCard(updatedDescription: String = ""): ProductPropertyCard {
        return ProductPropertyCard(
            type = PRIMARY,
            properties = listOf(
                title(),
                description(updatedDescription)
            ).filterNotEmpty()
        )
    }

    private fun title(): ProductProperty =
        Editable(
            hint = R.string.product_add_details_placeholder_title,
            text = "",
            onTextChanged = viewModel::onProductTitleChanged
        )

    private fun description(updatedDescription: String): ProductProperty? {
        return when {
            updatedDescription.isNotEmpty() -> {
                ComplexProperty(
                    title = R.string.product_description,
                    value = updatedDescription,
                    showTitle = true
                ) {
                    viewModel.onEditProductCardClicked(
                        ViewProductDescriptionEditor(
                            updatedDescription, resources.getString(R.string.product_description)
                        ),
                        PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
                    )
                }
            }
            else -> {
                ComplexProperty(
                    value = resources.getString(R.string.product_add_details_placeholder_description),
                    showTitle = false
                ) {
                    viewModel.onEditProductCardClicked(
                        ViewProductDescriptionEditor(
                            "", resources.getString(R.string.product_description)
                        ),
                        PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
                    )
                }
            }
        }
    }
}
