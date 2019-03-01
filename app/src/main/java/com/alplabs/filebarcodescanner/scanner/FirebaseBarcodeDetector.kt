package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
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

    var countUris: Int = 0

    val barcodesModel = mutableListOf<BarcodeModel>()

    val weakListener = WeakReference(listener)

    interface Listener {
        fun onDetectorSuccess(barcodesModel: List<BarcodeModel>)
        fun onDetectorFailure()
    }

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
        .build()

    private val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)


    fun scanner(context: Context, uris: List<Uri>) {

        countUris = uris.count()

        barcodesModel.clear()

        uris.forEach { uri ->

            try {

                val image = FirebaseVisionImage.fromFilePath(context, uri)

                detector.detectInImage(image)
                    .addOnSuccessListener { barcodes ->

                        Log.d("SCANNER_BARCODE", "" + barcodes)

                        barcodes.mapTo(barcodesModel) { BarcodeModel(it.rawValue ?: "") }

                        if (--countUris == 0) {
                            weakListener.get()?.onDetectorSuccess(barcodesModel)
                        }
                    }

                    .addOnFailureListener {
                        Log.e( "SCANNER_BARCODE", it.message)


                        if (--countUris == 0) {
                            weakListener.get()?.onDetectorFailure()
                        }
                    }

            } catch (e: Exception) {

                Log.e("scannerBarcode", "Error", e)

            }
        }




    }
}