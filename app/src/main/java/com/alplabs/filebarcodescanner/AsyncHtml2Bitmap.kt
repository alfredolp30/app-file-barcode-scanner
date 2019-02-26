package com.alplabs.filebarcodescanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AsyncHtml2Bitmap(context: Context, listener: Listener) : AsyncTask<Uri, Unit, Bitmap?>() {

    interface Listener {
        fun onFinishHtml2Bitmap(bitmap: Bitmap?)
    }

    private val weakContext = WeakReference(context)

    private val weakListener = WeakReference(listener)

    override fun doInBackground(vararg params: Uri?): Bitmap? {
        val uri = params[0]

        val ctx = weakContext.get()

        return if (uri != null && ctx != null) {
            val htmlData = weakContext.get()?.contentResolver?.openInputStream(uri)?.reader()?.readText() ?: ""

            Html2Bitmap.Builder().setContext(ctx)
                .setContent(WebViewContent.html(htmlData))
                .build().bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)

        weakListener.get()?.onFinishHtml2Bitmap(result)
    }
}