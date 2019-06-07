package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaeBarcodeDetector {

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
}


class AsyncFirebaseBarcodeDetector(context: Context, listener: Listener)
    : AsyncTask< Uri, Unit, Unit? >() {

    val weakListener = WeakReference(listener)
    val weakContext = WeakReference(context)


    override fun doInBackground(vararg params: Uri?): Unit? {

        val uri = params[0]
        val ctx = weakContext.get()


        if (ctx != null && uri != null) {
            FirebaeBarcodeDetector().scanner(ctx, uri) { barcodeModels ->
                weakListener.get()?.onDetectorFinish(barcodeModels)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModels: List<BarcodeModel>)
    }

}