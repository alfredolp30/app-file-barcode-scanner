package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseBarcodeDetector(listener: Listener) {

    private var countUris: Int = 0
    private val barcodeModels = mutableListOf<BarcodeModel>()
    private val weakListener = WeakReference(listener)

    interface Listener {
        fun onDetectorSuccess(barcodeModels: List<BarcodeModel>)
        fun onDetectorFailure()
    }

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
        .build()

    private val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)


    fun scanner(context: Context, uris: List<Uri>) {

        countUris = uris.count()

        barcodeModels.clear()

        uris.forEach { uri ->

            try {

                val image = FirebaseVisionImage.fromFilePath(context, uri)

                detector.detectInImage(image)
                    .addOnSuccessListener { barcodes ->

                        CALog.d("SCANNER_BARCODE", "" + barcodes)

                        barcodes.mapTo(barcodeModels) { BarcodeModel(it.displayValue ?: "") }

                        if (--countUris == 0) {
                            weakListener.get()?.onDetectorSuccess(barcodeModels)
                        }
                    }

                    .addOnFailureListener {

                        CALog.e( "SCANNER_BARCODE", it.message)

                        if (--countUris == 0) {
                            weakListener.get()?.onDetectorFailure()
                        }
                    }

            } catch (e: Exception) {

                CALog.e("scannerBarcode", "Error", e)

            }
        }




    }
}