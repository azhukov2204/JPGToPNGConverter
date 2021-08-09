package ru.androidlearning.jpgtopngconverter.presentation

import android.graphics.Bitmap
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface MainView : MvpView {
    @AddToEndSingle
    fun initButtons()

    @AddToEndSingle
    fun showBitmapImage(image: Bitmap)

    @AddToEndSingle
    fun setEnabledSaveToPNGButton(isEnabled: Boolean)

    @AddToEndSingle
    fun setEnabledOpenJpegButton(isEnabled: Boolean)

    @AddToEndSingle
    fun setVisibleProgressIndicator(isVisible: Boolean)

    @AddToEndSingle
    fun setVisibleCancelButton(isVisible: Boolean)

    @OneExecution
    fun checkPermissionAndOpenJpeg()

    @OneExecution
    fun showReadImageError(error: Throwable)

    @OneExecution
    fun savePng(fileName: String?)

    @OneExecution
    fun showSuccessConversionMessage()

    @OneExecution
    fun showConversionError(error: Throwable)

    @OneExecution
    fun showCancelConversionMessage()
}
