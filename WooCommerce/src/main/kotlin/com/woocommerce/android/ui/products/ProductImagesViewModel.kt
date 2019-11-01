package com.woocommerce.android.ui.products

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woocommerce.android.R
import com.woocommerce.android.annotations.OpenClassOnDebug
import com.woocommerce.android.di.UI_THREAD
import com.woocommerce.android.media.MediaUploadService
import com.woocommerce.android.media.MediaUploadService.Companion.OnProductMediaUploadEvent
import com.woocommerce.android.media.MediaUploadWrapper
import com.woocommerce.android.model.Product
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject
import javax.inject.Named

@OpenClassOnDebug
class ProductImagesViewModel @Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val productRepository: ProductImagesRepository,
    private val mediaUploadWrapper: MediaUploadWrapper
) : ScopedViewModel(mainDispatcher) {
    private var remoteProductId = 0L

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _showSnackbarMessage = SingleLiveEvent<Int>()
    val showSnackbarMessage: LiveData<Int> = _showSnackbarMessage

    private val _chooseProductImage = SingleLiveEvent<Product>()
    val chooseProductImage: LiveData<Product> = _chooseProductImage

    private val _captureProductImage = SingleLiveEvent<Product>()
    val captureProductImage: LiveData<Product> = _captureProductImage

    private val _isUploadingProductImage = MutableLiveData<Boolean>()
    val isUploadingProductImage: LiveData<Boolean> = _isUploadingProductImage

    private val _exit = SingleLiveEvent<Unit>()
    val exit: LiveData<Unit> = _exit

    init {
        EventBus.getDefault().register(this)
    }

    fun start(remoteProductId: Long) {
        this.remoteProductId = remoteProductId
        loadProduct()
        _isUploadingProductImage.value = MediaUploadService.isUploadingForProduct(remoteProductId)
    }

    override fun onCleared() {
        super.onCleared()
        productRepository.onCleanup()
        EventBus.getDefault().unregister(this)
    }

    fun loadProduct() {
        _product.value = productRepository.getProduct(remoteProductId)
    }

    fun onChooseImageClicked() {
        _chooseProductImage.value = product.value
    }

    fun onCaptureImageClicked() {
        _captureProductImage.value = product.value
    }

    fun uploadProductMedia(remoteProductId: Long, localImageUri: Uri) {
        // TODO: at some point we want to support uploading multiple product images
        if (MediaUploadService.isBusy()) {
            _showSnackbarMessage.value = R.string.product_image_already_uploading
            return
        }
        _isUploadingProductImage.value = true
        mediaUploadWrapper.uploadProductMedia(remoteProductId, localImageUri)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OnProductMediaUploadEvent) {
        _isUploadingProductImage.value = false
        if (event.isError) {
            _showSnackbarMessage.value = R.string.product_image_upload_error
        } else {
            loadProduct()
        }
    }
}
