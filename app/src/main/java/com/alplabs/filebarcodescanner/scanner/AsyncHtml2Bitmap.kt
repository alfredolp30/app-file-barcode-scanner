package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AsyncHtml2Bitmap(context: Context, listener: Listener) : AsyncTask<Uri, Unit, Uri>() {

    interface Listener {
        fun onFinishHtml2Bitmap(uri: Uri)
    }

    private val weakContext = WeakReference(context)

    private val weakListener = WeakReference(listener)

    override fun doInBackground(vararg params: Uri?): Uri? {
        val uri = params[0]

        val ctx = weakContext.get()
        var outputUri: Uri? = null

        if (uri != null && ctx != null) {
            val htmlData = weakContext.get()?.contentResolver?.openInputStream(uri)?.reader()?.readText() ?: ""

            val bitmap = Html2Bitmap.Builder()
                .setContext(ctx)
                .setContent(WebViewContent.html(htmlData))
                .build().bitmap?.copy(Bitmap.Config.ARGB_8888, false)

            val file = File(ctx.cacheDir, "temp1.png")

            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())

            outputUri = Uri.fromFile(file)
        }

        return outputUri
    }

    override fun onPostExecute(result: Uri?) {
        super.onPostExecute(result)

        if (result != null) {
            weakListener.get()?.onFinishHtml2Bitmap(result)
        }
    }
}