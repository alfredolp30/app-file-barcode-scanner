package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext
import android.os.Looper
import com.google.j2objc.annotations.Weak


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
private class FirebaseBarcodeDetector {

    private val detector : FirebaseVisionBarcodeDetector by lazy {

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ITF)
            .build()

        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    private val detectorDate by lazy { FirebaseTextDateDetector() }

    fun scanner(context: Context, uri: Uri, callback: (BarcodeModel?) -> Unit) {

        try {

            val image = FirebaseVisionImage.fromFilePath(context, uri)
            scanner(image, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", th.message, th)

            callback.invoke(null)
        }

    }


    fun scanner(buffer: ByteBuffer, width: Int, height: Int, callback: (BarcodeModel?) -> Unit) {

        try {

            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_270)
                .build()

            val image = FirebaseVisionImage.fromByteBuffer(buffer, metadata)
            scanner(image, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", "Error", th)

            callback.invoke(null)
        }
    }


    private fun scanner(image: FirebaseVisionImage, callback: (BarcodeModel?) -> Unit) {

        detector.detectInImage(image)
            .addOnSuccessListener { firebaseBarcodes ->

                CALog.d("SCANNER_BARCODE", "" + firebaseBarcodes)

                val barcode = firebaseBarcodes.firstOrNull()?.rawValue

                if (barcode != null) {
                    val checker = InvoiceChecker(barcode)

                    if (checker.isValid) {

                        if (checker.isCollection) {

                            detectorDate.scanner(image) { date ->
                                callback.invoke(BarcodeModel(barcode, date))
                            }

                        } else {
                            callback.invoke(BarcodeModel(barcode, null))
                        }

                    } else {
                        callback.invoke(null)
                    }
                } else {
                    callback.invoke(null)
                }

            }
            .addOnFailureListener { ex ->

                CALog.e("SCANNER_BARCODE", ex.message, ex)

                callback.invoke(null)
            }
    }
}



class ThreadFirebaseBarcodeUriDetector(context: Context, listener: Listener) {
    interface Listener {
        fun onDetectorFinish(barcodeModel: BarcodeModel?)
    }

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)
    private val handler = Handler(Looper.getMainLooper())


    fun start(uri: Uri) {

        Thread {
            val ctx = weakContext.get()

            if (ctx == null) {
                handler.post {
                    weakListener.get()?.onDetectorFinish(barcodeModel = null)
                }
                return@Thread
            }

            FirebaseBarcodeDetector().scanner(ctx, uri) { barcodeModel ->
                handler.post {
                    weakListener.get()?.onDetectorFinish(barcodeModel)
                }
            }

        }.run()
    }

}


class AsyncFirebaseBarcodeBufferDetector(
    val listener: Listener,
    private val width: Int,
    private val height: Int
):
    AsyncTask<ByteBuffer, Unit, Unit?>() {

    private val detector = FirebaseBarcodeDetector()

    override fun doInBackground(vararg params: ByteBuffer?): Unit? {

        val buffer = params[0]


        buffer?.let {
            detector.scanner(it, width, height) { barcodeModel ->
                listener.onDetectorFinish(barcodeModel)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModel: BarcodeModel?)
    }
}