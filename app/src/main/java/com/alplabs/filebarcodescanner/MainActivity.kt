package com.alplabs.filebarcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.Bitmap
import android.media.Image
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import kotlin.Exception


class MainActivity : AppCompatActivity(), AsyncHtml2Bitmap.Listener, AsyncPdf2Bitmap.Listener {

    companion object {

        val FILE_REQUEST_CODE = ActivityRequestCode.nextRequestCode()

    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        button.setOnClickListener {
            pickerFile()
        }
    }


    private fun pickerFile() {

        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpg", "image/png", "image/jpeg", "image/bmp", "application/pdf", "text/html"))

        }

        startActivityForResult(i, FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FILE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {

                    data?.data?.also { uri ->
                        fileRequestSuccess(uri)
                    }

                } else {
                    Log.w("PICKER_FILE", "cancelled")
                }
            }

            else -> {
                Log.e("PICKER_FILE", "not found request code")
            }
        }
    }

    private fun fileRequestSuccess(uri: Uri) {

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))

        Log.i("FILE_REQ_SUCCESS", "Uri: $uri extension: $extension")

        when (extension) {
            "pdf" -> {
                pdf2Bitmap(uri)
            }

            "jpeg", "png", "jpg", "bmp" -> {
                scannerBarcode(uri)
            }

            "htm" -> {
                html2Bitmap(uri)
            }

            else -> {
                Log.e("FILE_REQ_SUCCESS", "Not supported file extension")
            }
        }

    }


    private fun pdf2Bitmap(pdfUri: Uri) {
        AsyncPdf2Bitmap(this, this).execute(pdfUri)
    }

    override fun onFinishPdf2Bitmap(bitmaps: List<Bitmap>) {

        bitmaps.forEach {
            scannerBarcode(it)
        }

    }


    private fun html2Bitmap(htmlUri: Uri) {
        AsyncHtml2Bitmap(this, this).execute(htmlUri)
    }

    override fun onFinishHtml2Bitmap(bitmap: Bitmap?) {

        if (bitmap != null) {

            scannerBarcode(bitmap)

        }

    }

    private fun scannerBarcode(uri: Uri) {

        try {

            val image = FirebaseVisionImage.fromFilePath(this, uri)

            scannerBarcode(image)

        } catch (e: Exception) {

            Log.e("scannerBarcode", "Error create FirebaseVisionImage", e)

        }

    }


    private fun scannerBarcode(bitmap: Bitmap) {

        try {

            val image = FirebaseVisionImage.fromBitmap(bitmap)

            scannerBarcode(image)

        } catch (e: Exception) {

            Log.e("scannerBarcode", "Error create FirebaseVisionImage", e)

        }

    }


    private fun scannerBarcode(image: FirebaseVisionImage) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        val firebaseVision = FirebaseVision.getInstance()

        firebaseVision.isStatsCollectionEnabled = true

        val detector = firebaseVision.getVisionBarcodeDetector(options)

        detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->

                Log.d("SCANNER_BARCODE", "" + barcodes)

                detected(barcodes)

            }

            .addOnFailureListener {

                Log.e( "SCANNER_BARCODE", it.message)

            }

    }

    private fun detected(barcodes: List<FirebaseVisionBarcode>) {

        barcodes.forEach {

            Log.d("DETECTED", it.rawValue)

        }

    }

}
