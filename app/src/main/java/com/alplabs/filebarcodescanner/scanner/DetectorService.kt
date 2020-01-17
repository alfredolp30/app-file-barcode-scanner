package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-06-07.
 * Copyright Universo Online 2019. All rights reserved.
 */
class DetectorService(context: Context, listener: Listener, val uris: Queue<Uri>):
    AsyncFirebaseBarcodeUriDetector.Listener  {

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)
    private val totalBarcodeModels = mutableListOf<BarcodeModel>()

    interface Listener {
        fun onFinishDetectorService(barcodeModels: List<BarcodeModel>)
    }


    fun start() {

        val ctx = weakContext.get() ?: return

        if (uris.isNotEmpty()) {

            val uri = uris.remove()


            AsyncFirebaseBarcodeUriDetector(context = ctx,
                                             listener = this).execute(uri)

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

