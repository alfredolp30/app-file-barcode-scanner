package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.provider.DocumentsContract
import android.util.DisplayMetrics
import android.util.Log
import com.alplabs.filebarcodescanner.metrics.CALog
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.lang.ref.WeakReference
import android.provider.OpenableColumns
import java.math.BigInteger
import java.security.MessageDigest
import android.provider.MediaStore
import com.alplabs.filebarcodescanner.extension.md5


/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AsyncPdf2Bitmap(context: Context, listener: Listener) : AsyncTask<Uri, Unit, List<Uri>>() {

    interface Listener {
        fun onFinishPdf2Bitmap(uris: List<Uri>)
    }

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)

    override fun doInBackground(vararg params: Uri?): List<Uri> {
        CALog.d("AsyncPdf2Bitmap", "init")

        val uri = params[0]

        val ctx = weakContext.get()
        val outputUris = mutableListOf<Uri>()

        if (uri != null && ctx != null) {

            val fileName = getFileName(uri, ctx)
            val fd = ctx.contentResolver.openFileDescriptor(uri, "r")
            val core = PdfiumCore(ctx)

            CALog.i("FILENAME", fileName)

            try {
                val pdfDocument = core.newDocument(fd)

                val pageCount = core.getPageCount(pdfDocument)

                CALog.i("PDF2Bitmap", "page count $pageCount")

                for (page in 0 until pageCount) {

                    val file = File(ctx.cacheDir, "${fileName.md5()}-$page.png")

                    if (file.exists()) {
                        outputUris.add(Uri.fromFile(file))
                        CALog.i("PDF2Bitmap", "file exits with name $fileName and page $page")
                        continue
                    }

                    CALog.d("PDF2Bitmap", "init page $page")

                    core.openPage(pdfDocument, page)

                    val width = core.getPageWidth(pdfDocument, page)
                    val height = core.getPageHeight(pdfDocument, page)

                    val metrics = DisplayMetrics()
                    metrics.setToDefaults()

                    val bitmap = Bitmap.createBitmap(
                        metrics,
                        width, height,
                        Bitmap.Config.ARGB_8888
                    )

                    core.renderPageBitmap(
                        pdfDocument, bitmap, page, 0, 0,
                        width, height
                    )

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())

                    outputUris.add(Uri.fromFile(file))
                }

                core.closeDocument(pdfDocument)

            } catch (e: Exception) {
                CALog.e("PDF2Bitmap", "error convert", e)
            }
        }

        return outputUris
    }


    override fun onPostExecute(result: List<Uri>?) {
        super.onPostExecute(result)

        weakListener.get()?.onFinishPdf2Bitmap(result ?: listOf())
    }

    private fun getFileName(uri: Uri, context: Context): String {
        var fileName = ""
        val scheme = uri.scheme


        if (scheme == "file") {
            fileName = uri.lastPathSegment ?: ""

        } else if (scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                CALog.e("PDF2Bitmap", "cursor error", e)
            }

            cursor?.close()
        }

        return fileName
    }
}