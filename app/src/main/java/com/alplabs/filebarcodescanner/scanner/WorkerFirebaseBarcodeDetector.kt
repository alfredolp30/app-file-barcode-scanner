package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.media.Image
import android.net.Uri
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.alplabs.filebarcodescanner.scanner.detector.FirebaseBarcodeDetector
import java.lang.ref.WeakReference

class WorkerFirebaseBarcodeDetector(context: Context, listener: Listener) {
    interface Listener {
        fun onDetectorFinish(barcodeModel: BarcodeModel?)
    }

    private val weakContext = WeakReference(context)
    private val weakListener = WeakReference(listener)
    private val barcodeDetector = FirebaseBarcodeDetector()

    fun start(uri: Uri) {
        val ctx = weakContext.get()

        if (ctx == null) {
            weakListener.get()?.onDetectorFinish(barcodeModel = null)
            return
        }

        barcodeDetector.scannerFromArchive(ctx, uri) { barcodeModel ->
            weakListener.get()?.onDetectorFinish(barcodeModel)
        }
    }

    fun start(image: Image) {
        val ctx = weakContext.get()

        if (ctx == null) {
            weakListener.get()?.onDetectorFinish(barcodeModel = null)
            return
        }

        barcodeDetector.scannerFromCamera(ctx, image) { barcodeModel ->
            weakListener.get()?.onDetectorFinish(barcodeModel)
        }
    }
}


