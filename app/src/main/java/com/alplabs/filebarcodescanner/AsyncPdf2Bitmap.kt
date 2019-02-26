package com.alplabs.filebarcodescanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.shockwave.pdfium.PdfiumCore
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AsyncPdf2Bitmap(context: Context, listener: Listener) : AsyncTask<Uri, Unit, List<Bitmap>>() {

    interface Listener {
        fun onFinishPdf2Bitmap(bitmaps: List<Bitmap>)
    }

    private val weakContext = WeakReference(context)

    private val weakListener = WeakReference(listener)

    override fun doInBackground(vararg params: Uri?): List<Bitmap> {
        val uri = params[0]

        val ctx = weakContext.get()
        val bitmaps = mutableListOf<Bitmap>()

        if (uri != null && ctx != null) {
            val fd = ctx.contentResolver.openFileDescriptor(uri, "r")
            val core = PdfiumCore(ctx)

            try {
                val pdfDocument = core.newDocument(fd)

                val pageCount = core.getPageCount(pdfDocument)

                for (page in 0 until pageCount) {

                    core.openPage(pdfDocument, page)

                    val width = core.getPageWidthPoint(pdfDocument, page)
                    val height = core.getPageHeightPoint(pdfDocument, page)

                    val bitmap = Bitmap.createBitmap(
                        width, height,
                        Bitmap.Config.ARGB_8888
                    )

                    core.renderPageBitmap(
                        pdfDocument, bitmap, page, 0, 0,
                        width, height
                    )

                    bitmaps.add(bitmap)
                }

                core.closeDocument(pdfDocument)

            } catch (e: Exception) {
                Log.e("pdf2Bitmap", "Error convert", e)
            }
        }

        return bitmaps
    }


    override fun onPostExecute(result: List<Bitmap>?) {
        super.onPostExecute(result)

        if (result != null) {
            weakListener.get()?.onFinishPdf2Bitmap(result)
        }

    }

}