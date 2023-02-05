package com.alplabs.filebarcodescanner.scanner.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.media.Image
import android.net.Uri
import android.view.Surface
import androidx.core.graphics.applyCanvas
import com.alplabs.filebarcodescanner.AppBarcode
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseBarcodeDetector {

    private val detectorBarcode = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(
            FORMAT_ITF
        ).build()
    )

    private val detectorTextBarcode by lazy { FirebaseTextBarcodeDetector() }
    private val detectorTextDate by lazy { FirebaseTextDateDetector() }

    fun scannerFromArchive(context: Context, uri: Uri, callback: (BarcodeModel?) -> Unit) {

        try {

            val image = InputImage.fromFilePath(context, uri)
            scanner(context, image, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", th.message, th)

            callback.invoke(null)
        }

    }


    fun scannerFromCamera(
        context: Context,
        image: Image,
        callback: (BarcodeModel?) -> Unit
    ) {

        try {
            val rotatedImage = InputImage.fromMediaImage(
                image,
                Surface.ROTATION_270 * 90
            )

            scanner(context, rotatedImage, callback)

        } catch (th: Throwable) {

            CALog.e("scannerBarcode", "Error", th)

            callback.invoke(null)
        }
    }


    private fun scanner(context: Context, image: InputImage, callback: (BarcodeModel?) -> Unit) {

        detectorBarcode.process(image)
            .addOnSuccessListener { firebaseBarcodes ->

                CALog.d("SCANNER_BARCODE", "$firebaseBarcodes")

                val fbarcode = firebaseBarcodes.firstOrNull()

                if (fbarcode != null) {
                    val barcodeValue = fbarcode.displayValue ?: ""

                    val checker = InvoiceChecker(barcodeValue)

                    when {
                        checker.isBarcodeBank -> {
                            val path = saveImageToFile(context, image, fbarcode, firebaseTextBlock = null)
                            callback.invoke(BarcodeModel(barcodeValue, calendar = null, path = path))
                        }

                        checker.isBarcodeCollection -> {
                            detectorTextDate.scanner(image) { date, ftextBlock ->
                                val path = saveImageToFile(context, image, fbarcode, ftextBlock)
                                callback.invoke(BarcodeModel(barcodeValue, date, path = path))
                            }
                        }

                        else -> callback.invoke(null)
                    }

                } else {
                    detectorTextBarcode.scanner(image) { barcodeValue ->
                        val founded = barcodeValue != null
                        val analytics = (context.applicationContext as? AppBarcode)?.analytics

                        if (barcodeValue != null) {
                            CALog.d("FOUND_CODE_BY_TEXT", "$barcodeValue")
                        }

                        analytics?.eventTestFinished(founded, barcodeValue)
                        callback.invoke(null)
                    }
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


