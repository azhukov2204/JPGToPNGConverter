package ru.androidlearning.jpgtopngconverter.data

import android.graphics.Bitmap
import android.net.Uri
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ImageProcessor {
    fun getBitmapFromUri(uri: Uri): Single<Bitmap>
    fun convertBitmapToPng(inputBitmap: Bitmap, outputUri: Uri): Completable
    fun getFileNameByUri(uri: Uri): String?
    fun deleteFileByUri(uri: Uri): Completable
}
