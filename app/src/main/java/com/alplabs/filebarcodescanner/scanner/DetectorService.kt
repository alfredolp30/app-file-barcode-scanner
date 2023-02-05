package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import com.alplabs.filebarcodescanner.model.BarcodeModel
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-06-07.
 * Copyright Universo Online 2019. All rights reserved.
 */
class DetectorService(context: Context, listener: Listener, val uris: Queue<Uri>):
    WorkerFirebaseBarcodeDetector.Listener  {

    private val weakListener = WeakReference(listener)
    private val totalBarcodeModels = mutableListOf<BarcodeModel>()
    private val threadFirebaseBarcodeUriDetector = WorkerFirebaseBarcodeDetector(context, listener = this)

    interface Listener {
        fun onFinishDetectorService(barcodeModels: List<BarcodeModel>)
    }


    fun start() {
        if (uris.isNotEmpty()) {

            val uri = uris.remove()

            threadFirebaseBarcodeUriDetector.start(uri)

        } else {
            weakListener.get()?.onFinishDetectorService(totalBarcodeModels)
        }

    }


    override fun onDetectorFinish(barcodeModel: BarcodeModel?) {

        barcodeModel?.let { model ->
            totalBarcodeModels.add(model)
        }

        start()

    }
}

