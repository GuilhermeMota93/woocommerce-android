package com.woocommerce.android.ui.products

import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
import com.woocommerce.android.extensions.addIfNotEmpty
import com.woocommerce.android.extensions.fastStripHtml
import com.woocommerce.android.extensions.filterNotEmpty
import com.woocommerce.android.ui.products.ProductDetailViewModel.AddNewProduct
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
    fun buildPropertyCards(addNewProduct: AddNewProduct): List<ProductPropertyCard> {
        val cards = mutableListOf<ProductPropertyCard>()

        cards.addIfNotEmpty(getPrimaryCard(addNewProduct))

        return cards
    }

    private fun getPrimaryCard(addNewProduct: AddNewProduct): ProductPropertyCard {
        return ProductPropertyCard(
            type = PRIMARY,
            properties = listOf(
                addNewProduct.title(),
                addNewProduct.description()
            ).filterNotEmpty()
        )
    }

    private fun AddNewProduct.title(): ProductProperty {
        val title = this.title?.fastStripHtml() ?: ""
        return Editable(
            hint = R.string.product_add_details_placeholder_title,
            text = title,
            onTextChanged = viewModel::onProductTitleChanged
        )
    }

    private fun AddNewProduct.description(): ProductProperty? {
        val productDescription = this.description ?: return defaultDescriptionCard()

        return ComplexProperty(
            R.string.product_description,
            productDescription,
            showTitle = true) { descriptionAction(productDescription) }
    }

    private fun defaultDescriptionCard(): ComplexProperty {
        val placeholder = resources.getString(R.string.product_add_details_placeholder_description)
        return ComplexProperty(
            value = placeholder,
            showTitle = false) { descriptionAction() }
    }

    private fun descriptionAction(description: String = "") {
        viewModel.onEditProductCardClicked(
            ViewProductDescriptionEditor(
                description,
                resources.getString(R.string.product_description)
            ),
            PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
        )
    }
}
