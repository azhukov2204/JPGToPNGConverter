package ru.androidlearning.jpgtopngconverter.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.FileDescriptor
import java.io.FileOutputStream

class ImageProcessorImpl(private val context: Context) : ImageProcessor {
    private val contentResolver: ContentResolver = context.contentResolver

    override fun getBitmapFromUri(uri: Uri): Single<Bitmap> =
        Single.fromCallable {
            contentResolver.openFileDescriptor(uri, "r").use { parcelFileDescriptor ->
                val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
                BitmapFactory.decodeFileDescriptor(fileDescriptor)
            }
        }

    override fun convertBitmapToPng(inputBitmap: Bitmap, outputUri: Uri): Completable =
        Completable.fromAction {
            try {
                Thread.sleep(5000)  //"эмуляция" длительного выполнения задачи
                contentResolver.openFileDescriptor(outputUri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { outputStream ->
                        inputBitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, outputStream)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    override fun getFileNameByUri(uri: Uri): String? {
        var displayName: String? = null
        contentResolver.query(uri, null, null, null, null, null)
            ?.use {
                if (it.moveToFirst()) {
                    displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        return displayName
    }

    override fun deleteFileByUri(uri: Uri): Completable = Completable.fromAction {
        DocumentFile.fromSingleUri(context, uri)?.delete()
    }

    companion object {
        const val PNG_QUALITY = 100
    }
}
