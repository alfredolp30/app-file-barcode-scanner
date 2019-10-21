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
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

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

//    private var listener: AsyncFirebaseTextDateUriDetector.Listener? = null
//    private var task: AsyncFirebaseTextDateUriDetector? = null
    private val detectorDate = FirebaseTextDateDetector()

    fun scanner(context: Context, uri: Uri, callback: (BarcodeModel?) -> Unit) {

        try {

            val image = FirebaseVisionImage.fromFilePath(context, uri)

            detector.detectInImage(image)
                .addOnSuccessListener { firebaseBarcodes ->

                    CALog.d("SCANNER_BARCODE", "" + firebaseBarcodes)

                    val barcode = firebaseBarcodes.firstOrNull()?.rawValue

                    if (barcode != null) {
                        val checker = InvoiceChecker(barcode)

                        if (checker.isValid) {
                            val isCollection = checker.isCollection

                            if (checker.isCollection) {

                                detectorDate.scanner(context, uri) { date ->
                                    callback.invoke(BarcodeModel(barcode, isCollection, date))
                                }

                            } else {
                                callback.invoke(BarcodeModel(barcode, isCollection, null))
                            }

                        } else {
                            callback.invoke(null)
                        }
                    } else {
                        callback.invoke(null)
                    }

                }.addOnFailureListener { ex ->

                    CALog.e( "SCANNER_BARCODE", ex.message, ex)

                    callback.invoke(null)
                }

        } catch (ex: IOException) {

            CALog.e("scannerBarcode", ex.message, ex)

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

            detector.detectInImage(image)
                .addOnSuccessListener { firebaseBarcodes ->

                    CALog.d("SCANNER_BARCODE", "" + firebaseBarcodes)

                    val barcode = firebaseBarcodes.firstOrNull()?.rawValue

                    if (barcode != null) {
                        val checker = InvoiceChecker(barcode)

                        if (checker.isValid) {
                            val isCollection = checker.isCollection

                            callback.invoke(BarcodeModel(barcode, isCollection, null))
                        } else {
                            callback.invoke(null)
                        }
                    } else {
                        callback.invoke(null)
                    }

                }.addOnFailureListener { exception ->

                    CALog.e( "SCANNER_BARCODE", exception.message)

                    callback.invoke(null)
                }

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", "Error", th)

            callback.invoke(null)
        }
    }
}


class AsyncFirebaseBarcodeUriDetector(context: Context, val listener: Listener)
    : AsyncTask< Uri, Unit, Unit? >() {

    private val weakContext = WeakReference(context)

    private val detector = FirebaseBarcodeDetector()

    override fun doInBackground(vararg params: Uri?): Unit? {

        val uri = params[0]
        val ctx = weakContext.get()


        if (ctx != null && uri != null) {
            detector.scanner(ctx, uri) { barcodeModel ->
                listener.onDetectorFinish(barcodeModel)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModel: BarcodeModel?)
    }

}


class AsyncFirebaseBarcodeBufferDetector(
    val listener: Listener,
    private val width: Int,
    private val height: Int
):
    AsyncTask< ByteBuffer, Unit, Unit? >() {

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