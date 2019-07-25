package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import com.alplabs.filebarcodescanner.model.BarcodeModel
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 2019-06-07.
 * Copyright Universo Online 2019. All rights reserved.
 */
class DetectorService(listener: Listener) {

    val weakReference = WeakReference(listener)
    val totalBarcodeModels = mutableListOf<BarcodeModel>()
    val listeners = mutableListOf<AsyncFirebaseBarcodeUriDetector.Listener>()

    interface Listener {
        fun onFinishDetectorService(barcodeModels: List<BarcodeModel>)
    }


    fun start(context: Context, uris: List<Uri>) {


        uris.forEach { uri ->

            val listener = object : AsyncFirebaseBarcodeUriDetector.Listener {

                override fun onDetectorFinish(barcodeModels: List<BarcodeModel>) {
                    totalBarcodeModels.addAll(barcodeModels)
                    listeners.remove(this)

                    if (listeners.isEmpty()) {
                        weakReference.get()?.onFinishDetectorService(totalBarcodeModels)
                    }
                }

            }

            AsyncFirebaseBarcodeUriDetector(context, listener).execute(uri)

            listeners.add(listener)
        }

    }
}

