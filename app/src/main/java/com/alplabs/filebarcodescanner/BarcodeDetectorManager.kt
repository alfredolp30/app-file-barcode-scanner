package com.alplabs.filebarcodescanner

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import android.widget.EditText
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.scanner.AsyncHtml2Bitmap
import com.alplabs.filebarcodescanner.scanner.AsyncPdf2Bitmap
import com.alplabs.filebarcodescanner.scanner.DetectorService
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2020-01-27.
 * Copyright Universo Online 2020. All rights reserved.
 */
class BarcodeDetectorManager(context: Context, listener: Listener):
    AsyncHtml2Bitmap.Listener,
    AsyncPdf2Bitmap.Listener,
    DetectorService.Listener {

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)
    private var detector: DetectorService? = null

    interface Listener {
        fun onBarcodeScannerSuccess(barcodeModels: List<BarcodeModel>)
        fun onBarcodeScannerError(error: Error? = null)
    }

    fun start(uri: Uri) {

        val ctx = weakContext.get() ?: return

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(ctx.contentResolver.getType(uri))

        CALog.i("FILE_REQ_SUCCESS", "Uri: $uri extension: $extension")

        when (extension) {
            "pdf" -> {
                pdf2Bitmap(uri)
            }

            "jpeg", "png", "jpg", "bmp" -> {
                scannerBarcode(listOf(uri))
            }

            "htm" -> {
                html2Bitmap(uri)
            }

            else -> {
                CALog.e("FILE_REQ_SUCCESS", "Not supported file extension")
            }
        }
    }


    private fun pdf2Bitmap(pdfUri: Uri, password: String? = null) {
        val ctx = weakContext.get()

        if (ctx != null) {
            AsyncPdf2Bitmap(ctx, this).execute(AsyncPdf2Bitmap.WorkPdf(uri = pdfUri, password = password))
        } else {
            weakListener.get()?.onBarcodeScannerError()
        }

    }

    override fun onFinishPdf2Bitmap(uris: List<Uri>) {
        scannerBarcode(uris)
    }

    override fun onRequiredPassword(uri: Uri, fileName: String) {
        val ctx = weakContext.get() ?: return

        val alert = AlertDialog.Builder(ctx)
        alert.setTitle(ctx.getString(R.string.required_password, fileName))

        val view = LayoutInflater.from(ctx).inflate(R.layout.alert_password, null, false)

        val edtTextPass = view.findViewById<EditText>(R.id.edtTextPass)

        alert.setView(view)

        alert.setPositiveButton(R.string.confirm) { _, _ ->
            edtTextPass.clearComposingText()

            val password = edtTextPass.text.toString().trim()

            pdf2Bitmap(uri, password)
        }

        alert.setNegativeButton(ctx.getString(R.string.cancel)) { _, _ ->
            weakListener.get()?.onBarcodeScannerError(Error(ctx.getString(R.string.cancel_digit_password)))
        }

        alert.show()
    }


    private fun html2Bitmap(htmlUri: Uri) {
        val ctx = weakContext.get()

        if (ctx != null) {
            AsyncHtml2Bitmap(ctx, this).execute(htmlUri)
        } else {
            weakListener.get()?.onBarcodeScannerError()
        }

    }

    override fun onFinishHtml2Bitmap(uri: Uri?) {

        if (uri != null) {
            scannerBarcode(listOf(uri))
        } else {
            weakListener.get()?.onBarcodeScannerError()
        }
    }


    private fun scannerBarcode(uris: List<Uri>) {
        val ctx = weakContext.get()

        if (uris.isNotEmpty() && ctx != null) {
            detector = DetectorService(ctx, listener = this, uris = ArrayDeque(uris))
            detector?.start()
        } else {
            weakListener.get()?.onBarcodeScannerError()
        }
    }

    override fun onFinishDetectorService(barcodeModels: List<BarcodeModel>) {
        weakListener.get()?.onBarcodeScannerSuccess(barcodeModels)
    }
}