package com.alplabs.filebarcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alplabs.filebarcodescanner.fragment.BarcodesFragment
import com.alplabs.filebarcodescanner.fragment.ErrorFragment
import com.alplabs.filebarcodescanner.fragment.ErrorType
import com.alplabs.filebarcodescanner.fragment.ProgressFragment
import com.alplabs.filebarcodescanner.model.BarcodeModel

import kotlinx.android.synthetic.main.activity_barcode.*

class BarcodeActivity : AppCompatActivity(), ProgressFragment.Listener {

    companion object {

        val FILE_REQUEST_CODE = ActivityRequestCode.nextRequestCode()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        setSupportActionBar(toolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame, ErrorFragment.newInstance(ErrorType.INIT))
            .commit()

        fab.setOnClickListener {
            pickerFile()
        }


        if (intent?.action == Intent.ACTION_SEND) {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                fileSuccess(it)
            }
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
                        fileSuccess(uri)
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

    private fun fileSuccess(uri: Uri) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame, ProgressFragment.newInstance(uri))
            .commit()
    }


    override fun onBarcodeScannerSuccess(barcodesModel: List<BarcodeModel>) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame, BarcodesFragment.newInstance(barcodesModel))
            .commit()

        barcodesModel.forEach {
            Log.i("RAW_VALUE", it.rawValue)
        }
    }

    override fun onBarcodeScannerError() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame, ErrorFragment.newInstance(ErrorType.DURANT_LOADING))
            .commit()
    }
}
