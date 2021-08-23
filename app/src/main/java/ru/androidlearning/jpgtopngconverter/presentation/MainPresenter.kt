package ru.androidlearning.jpgtopngconverter.presentation

import android.graphics.Bitmap
import android.net.Uri
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.plusAssign
import moxy.MvpPresenter
import ru.androidlearning.jpgtopngconverter.data.ImageProcessor
import ru.androidlearning.jpgtopngconverter.scheduler.WorkSchedulers
import java.io.File

class MainPresenter(
    private val schedulers: WorkSchedulers,
    private val imageProcessor: ImageProcessor
) : MvpPresenter<MainView>() {
    private val disposables = CompositeDisposable()
    private var jpegImage: Bitmap? = null
    private var jpegUri: Uri? = null
    private var pngUri: Uri? = null
    private var conversionDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.initButtons()
    }

    fun openJpeg() {
        viewState.checkPermissionAndOpenJpeg()
    }

    fun loadJpegToBitmap(uri: Uri?) {
        uri?.let { jpegUri ->
            this.jpegUri = jpegUri
            disposables +=
                imageProcessor.getBitmapFromUri(jpegUri)
                    .subscribeOn(schedulers.threadIO())
                    .observeOn(schedulers.threadMain())
                    .subscribe(
                        this::onSuccessLoadImage,
                        viewState::showReadImageError
                    )
        }
    }

    private fun onSuccessLoadImage(image: Bitmap) {
        this.jpegImage = image
        viewState.showBitmapImage(image)
        viewState.setEnabledSaveToPNGButton(true)
    }

    fun savePng() {
        viewState.savePng(
            jpegUri?.let { jpegUri ->
                imageProcessor.getFileNameByUri(jpegUri)
                    ?.let { jpegFileName ->
                        File(jpegFileName).nameWithoutExtension
                    }
            }
        )
    }

    fun convertBitmapToPng(pngUri: Uri) {
        this.pngUri = pngUri
        jpegImage?.let { image ->
            startConversionVisualization()
            conversionDisposable =
                imageProcessor.convertBitmapToPng(inputBitmap = image, outputUri = pngUri)
                    .subscribeOn(schedulers.threadIO())
                    .observeOn(schedulers.threadMain())
                    .doOnDispose(this::onDispose)
                    .subscribe(
                        this::onCompleteConversations,
                        this::onErrorConversations
                    )
            conversionDisposable?.addTo(disposables)
        }
    }

    fun cancelConversion() {
        conversionDisposable?.dispose()
    }

    private fun onDispose() {
        endConversionVisualization()
        viewState.showCancelConversionMessage()
        deletePngFile()
    }

    private fun onCompleteConversations() {
        endConversionVisualization()
        viewState.showSuccessConversionMessage()
    }

    private fun onErrorConversations(error: Throwable) {
        endConversionVisualization()
        viewState.showConversionError(error)
        deletePngFile()
    }

    private fun deletePngFile() {
        pngUri?.let {
            disposables +=
                imageProcessor.deleteFileByUri(it)
                    .subscribeOn(schedulers.threadIO())
                    .observeOn(schedulers.threadMain())
                    .subscribe()
        }
    }

    private fun startConversionVisualization() {
        viewState.apply {
            setEnabledOpenJpegButton(false)
            setEnabledSaveToPNGButton(false)
            setVisibleProgressIndicator(true)
            setVisibleCancelButton(true)
        }
    }

    private fun endConversionVisualization() {
        viewState.apply {
            setEnabledOpenJpegButton(true)
            setEnabledSaveToPNGButton(true)
            setVisibleProgressIndicator(false)
            setVisibleCancelButton(false)
        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
