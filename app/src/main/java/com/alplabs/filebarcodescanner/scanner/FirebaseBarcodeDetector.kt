package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.core.graphics.applyCanvas
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.InputImage.IMAGE_FORMAT_YV12
import com.google.mlkit.vision.text.Text
import java.io.File
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
private class FirebaseBarcodeDetector {

    private val detector : BarcodeScanner by lazy {

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ITF)
            .build()

        BarcodeScanning.getClient(options)
    }

    private val detectorDate by lazy { FirebaseTextDateDetector() }

    fun scannerForArchive(context: Context, uri: Uri, callback: (BarcodeModel?) -> Unit) {

        try {

            val image = InputImage.fromFilePath(context, uri)
            scanner(context, image, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", th.message, th)

            callback.invoke(null)
        }

    }


    fun scannerForCamera(
        context: Context,
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        callback: (BarcodeModel?) -> Unit
    ) {

        try {
            val image = InputImage.fromByteBuffer(
                buffer,
                width,
                height,
                Surface.ROTATION_270,
                IMAGE_FORMAT_YV12
            )

            scanner(context, image, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", "Error", th)

            callback.invoke(null)
        }
    }


    private fun scanner(context: Context, image: InputImage, callback: (BarcodeModel?) -> Unit) {

        detector.process(image)
            .addOnSuccessListener { firebaseBarcodes ->

                CALog.d("SCANNER_BARCODE", "" + firebaseBarcodes)

                val fbarcode = firebaseBarcodes.firstOrNull()

                if (fbarcode != null) {
                    val barcodeValue = fbarcode.displayValue?.toString() ?: ""
                    val checker = InvoiceChecker(barcodeValue)

                    if (checker.isValid) {

                        if (checker.isCollection) {

                            detectorDate.scanner(image) { date, ftextBlock ->
                                val path = saveImageToFile(context, image, fbarcode, ftextBlock)
                                callback.invoke(BarcodeModel(barcodeValue, date, path = path))
                            }

                        } else {
                            val path = saveImageToFile(context, image, fbarcode, firebaseTextBlock = null)
                            callback.invoke(BarcodeModel(barcodeValue, calendar = null, path = path))
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



    private fun saveImageToFile(
        context: Context,
        image: InputImage,
        firebaseBarcode: Barcode,
        firebaseTextBlock: Text.TextBlock?
    ) : String {

        val rects = listOf(firebaseBarcode.boundingBox, firebaseTextBlock?.boundingBox)
        val bitmap = image.bitmapInternal ?: return ""

        val mutableBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        mutableBitmap.applyCanvas {
            val paint = Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 10f
            }

            rects.filterNotNull().forEach {
                drawRect(
                    it.left.toFloat(),
                    it.top.toFloat(),
                    it.right.toFloat(),
                    it.bottom.toFloat(),
                    paint
                )
            }

        }

        val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S", Locale("pt", "BR"))
        val dateStr = sdf.format(GregorianCalendar().time)

        val directory = File(context.filesDir, "barcode")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, "$dateStr.png")

        mutableBitmap.compress(Bitmap.CompressFormat.PNG, 80, file.outputStream())

        return file.absolutePath
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

            FirebaseBarcodeDetector().scannerForArchive(ctx, uri) { barcodeModel ->
                handler.post {
                    weakListener.get()?.onDetectorFinish(barcodeModel)
                }
            }

        }.run()
    }

}


class AsyncFirebaseBarcodeBufferDetector(
    context: Context,
    val listener: Listener,
    private val width: Int,
    private val height: Int
):
    AsyncTask<ByteBuffer, Unit, Unit?>() {

    private val detector = FirebaseBarcodeDetector()
    private val weakContext = WeakReference(context)

    override fun doInBackground(vararg params: ByteBuffer?): Unit? {

        val buffer = params[0]
        val ctx = weakContext.get()

        if (buffer != null && ctx != null) {
            detector.scannerForCamera(ctx, buffer, width, height) { barcodeModel ->
                listener.onDetectorFinish(barcodeModel)
            }
        }

        return null

    }

    interface Listener {
        fun onDetectorFinish(barcodeModel: BarcodeModel?)
    }
}