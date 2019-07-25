package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
private class FirebaseBarcodeDetector {

    companion object {

        private val detector : FirebaseVisionBarcodeDetector by lazy {

            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ITF)
                .build()

            FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        }
    }

    fun scanner(context: Context, uri: Uri, callback: (List<BarcodeModel>) -> Unit) {

        val barcodeModels = mutableListOf<BarcodeModel>()

        try {

            val image = FirebaseVisionImage.fromFilePath(context, uri)

            detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->

                    CALog.d("SCANNER_BARCODE", "" + barcodes)

                    barcodes
                        .filter{ InvoiceChecker(it.displayValue ?: "").isValid }
                        .mapTo(barcodeModels) {
                            BarcodeModel(it.displayValue!!, InvoiceChecker(it.displayValue!!).isCollection)
                        }

                    callback.invoke(barcodeModels)

                }.addOnFailureListener { exception ->

                    CALog.e( "SCANNER_BARCODE", exception.message)

                    callback.invoke(barcodeModels)
                }

        } catch (e: Exception) {

            CALog.e("scannerBarcode", "Error", e)

            callback.invoke(barcodeModels)
        }
    }


    fun scanner(buffer: ByteBuffer, width: Int, height: Int, callback: (List<BarcodeModel>) -> Unit) {

        val barcodeModels = mutableListOf<BarcodeModel>()

        try {

            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(width) // 480x360 is typically sufficient for
                .setHeight(height) // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_270)
                .build()

            val image = FirebaseVisionImage.fromByteBuffer(buffer, metadata)

            detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->

                    CALog.d("SCANNER_BARCODE", "" + barcodes)

                    barcodes
                        .filter{ InvoiceChecker(it.displayValue ?: "").isValid }
                        .mapTo(barcodeModels) {
                            BarcodeModel(it.displayValue!!, InvoiceChecker(it.displayValue!!).isCollection)
                        }

                    callback.invoke(barcodeModels)

                }.addOnFailureListener { exception ->

                    CALog.e( "SCANNER_BARCODE", exception.message)

                    callback.invoke(barcodeModels)
                }

        } catch (e: Exception) {

            CALog.e("scannerBarcode", "Error", e)

            callback.invoke(barcodeModels)
        }
    }
}


class AsyncFirebaseBarcodeUriDetector(context: Context, listener: Listener)
    : AsyncTask< Uri, Unit, Unit? >() {

    private val weakListener = WeakReference(listener)
    private val weakContext = WeakReference(context)

    private val detector = FirebaseBarcodeDetector()

    override fun doInBackground(vararg params: Uri?): Unit? {

        val uri = params[0]
        val ctx = weakContext.get()


        if (ctx != null && uri != null) {
            detector.scanner(ctx, uri) { barcodeModels ->
                weakListener.get()?.onDetectorFinish(barcodeModels)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModels: List<BarcodeModel>)
    }

}


class AsyncFirebaseBarcodeBufferDetector(
    listener: Listener,
    private val width: Int,
    private val height: Int
):
    AsyncTask< ByteBuffer, Unit, Unit? >() {

    private val weakListener = WeakReference(listener)

    private val detector = FirebaseBarcodeDetector()

    override fun doInBackground(vararg params: ByteBuffer?): Unit? {

        val buffer = params[0]


        buffer?.let {
            detector.scanner(it, width, height) { barcodeModels ->
                weakListener.get()?.onDetectorFinish(barcodeModels)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModels: List<BarcodeModel>)
    }
}