package com.woocommerce.android.ui.products

import com.woocommerce.android.R
import com.woocommerce.android.R.drawable
import com.woocommerce.android.R.string
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRICE_SETTINGS_TAPPED
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_TYPE_TAPPED
import com.woocommerce.android.extensions.addIfNotEmpty
import com.woocommerce.android.extensions.fastStripHtml
import com.woocommerce.android.extensions.filterNotEmpty
import com.woocommerce.android.extensions.isSet
import com.woocommerce.android.ui.products.ProductDetailViewModel.AddNewProduct
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductDescriptionEditor
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductDetailTypes
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductPricing
import com.woocommerce.android.ui.products.ProductPricingViewModel.PricingData
import com.woocommerce.android.ui.products.models.ProductProperty
import com.woocommerce.android.ui.products.models.ProductProperty.ComplexProperty
import com.woocommerce.android.ui.products.models.ProductProperty.Editable
import com.woocommerce.android.ui.products.models.ProductProperty.PropertyGroup
import com.woocommerce.android.ui.products.models.ProductPropertyCard
import com.woocommerce.android.ui.products.models.ProductPropertyCard.Type.PRIMARY
import com.woocommerce.android.ui.products.models.ProductPropertyCard.Type.SECONDARY
import com.woocommerce.android.ui.products.models.SiteParameters
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.util.PriceUtils
import com.woocommerce.android.viewmodel.ResourceProvider

class AddProductDetailCardBuilder(
    private val viewModel: ProductDetailViewModel,
    private val resources: ResourceProvider,
    private val currencyFormatter: CurrencyFormatter,
    private val parameters: SiteParameters
) {
    fun buildPropertyCards(addNewProduct: AddNewProduct): List<ProductPropertyCard> {
        val cards = mutableListOf<ProductPropertyCard>()

        cards.addIfNotEmpty(getPrimaryCard(addNewProduct))

        cards.addIfNotEmpty(getSimpleProductCard(addNewProduct))

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

    private fun getSimpleProductCard(addNewProduct: AddNewProduct): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                addNewProduct.price(),
                addNewProduct.productType()
            ).filterNotEmpty()
        )
    }

    /**
     * PRIMARY cards
     * */

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

    /**
     * SECONDARY cards
     * */

    private fun AddNewProduct.productType(): ProductProperty? {
        val productType = resources.getString(this.getProductTypeFormattedForDisplay())
        return ComplexProperty(
            string.product_type,
            resources.getString(string.product_detail_product_type_hint, productType),
            drawable.ic_gridicons_product
        ) {
            viewModel.onEditProductCardClicked(
                ViewProductDetailTypes,
                PRODUCT_DETAIL_VIEW_PRODUCT_TYPE_TAPPED
            )
        }
    }

    private fun AddNewProduct.price(): ProductProperty {
        // If we have pricing info, show price & sales price as a group,
        // otherwise provide option to add pricing info for the product
        if (this.productPricing == null) {
            return PropertyGroup(
                title = string.product_price,
                properties = mapOf()
            )
        }

        val pricingGroup = PriceUtils.getPriceGroup(
            parameters,
            resources,
            currencyFormatter,
            productPricing.regularPrice,
            productPricing.salePrice,
            productPricing.isSaleScheduled,
            productPricing.saleStartDateGmt,
            productPricing.saleEndDateGmt
        )

        return PropertyGroup(
            string.product_price,
            pricingGroup,
            drawable.ic_gridicons_money,
            showTitle = this.productPricing.regularPrice.isSet()
        ) {
            viewModel.onEditProductCardClicked(
                ViewProductPricing(
                    PricingData(
                    productPricing.taxClass,
                    productPricing.taxStatus,
                    productPricing.isSaleScheduled,
                    productPricing.saleStartDateGmt,
                    productPricing.saleEndDateGmt,
                    productPricing.regularPrice,
                    productPricing.salePrice
                )
                ),
                PRODUCT_DETAIL_VIEW_PRICE_SETTINGS_TAPPED
            )
        }
    }
}
