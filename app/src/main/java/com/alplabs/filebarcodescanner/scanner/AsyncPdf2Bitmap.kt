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
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfPasswordException


/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AsyncPdf2Bitmap(context: Context, listener: Listener) :
    AsyncTask<AsyncPdf2Bitmap.WorkPdf, Unit, AsyncPdf2Bitmap.DataResult>() {

    interface Listener {
        fun onFinishPdf2Bitmap(uris: List<Uri>)
        fun onRequiredPassword(uri: Uri, fileName: String)
    }

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)

    data class WorkPdf(
        val uri: Uri,
        val password: String?
    )


    data class DataResult(
        val sourceUri: Uri,
        var fileName: String,
        val uris: MutableList<Uri>,
        var requiredPassword: Boolean
    )

    override fun doInBackground(vararg params: WorkPdf?): DataResult {
        CALog.d("AsyncPdf2Bitmap", "init")

        val workPdf = params[0] as WorkPdf

        val dataResult = DataResult(workPdf.uri, "", mutableListOf(), false)

        weakContext.get()?.let { ctx ->

            try {

                val fd = ctx.contentResolver.openFileDescriptor(workPdf.uri, "r")
                val fileName = getFileName(workPdf.uri, ctx)

                CALog.i("FILENAME", fileName)

                dataResult.fileName = fileName

                val core = PdfiumCore(ctx)

                val pdfDocument = core.newDocument(fd, workPdf.password)

                val pageCount = core.getPageCount(pdfDocument)

                CALog.i("PDF2Bitmap", "page count $pageCount")

                for (page in 0 until pageCount) {

                    val file = File(ctx.cacheDir, "${fileName.md5()}-$page.png")

                    if (file.exists()) {
                        dataResult.uris.add(Uri.fromFile(file))
                        CALog.i("PDF2Bitmap", "file exits with name $fileName and page $page")
                        continue
                    }

                    CALog.d("PDF2Bitmap", "init page $page")

                    core.openPage(pdfDocument, page)

                    val width = core.getPageWidth(pdfDocument, page)
                    val height = core.getPageHeight(pdfDocument, page)

                    val bitmap = Bitmap.createBitmap(
                        width, height,
                        Bitmap.Config.ARGB_8888
                    )

                    core.renderPageBitmap(
                        pdfDocument, bitmap, page, 0, 0,
                        width, height
                    )

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())

                    dataResult.uris.add(Uri.fromFile(file))
                }

                core.closeDocument(pdfDocument)

            } catch (ex: PdfPasswordException) {

                dataResult.requiredPassword = true

            } catch (th: Throwable) {

                CALog.e("PDF2Bitmap", "error convert", th)
            }
        }

        return dataResult
    }


    override fun onPostExecute(result: DataResult?) {
        super.onPostExecute(result)

        if (result?.requiredPassword == true) {
            weakListener.get()?.onRequiredPassword(result.sourceUri, result.fileName)
        } else {
            weakListener.get()?.onFinishPdf2Bitmap(result?.uris ?: listOf())
        }
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