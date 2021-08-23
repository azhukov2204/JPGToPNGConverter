package ru.androidlearning.jpgtopngconverter.data

import android.content.Context

object ImageProcessorFactory {
    fun create(context: Context): ImageProcessor = ImageProcessorImpl(context)
}
