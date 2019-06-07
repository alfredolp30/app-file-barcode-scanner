package com.alplabs.filebarcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.alplabs.filebarcodescanner.fragment.BarcodeFragment
import com.alplabs.filebarcodescanner.fragment.InitialFragment
import com.alplabs.filebarcodescanner.fragment.ProgressFragment
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel

import kotlinx.android.synthetic.main.activity_barcode.*

open class BarcodeActivity : BaseActivity(), ProgressFragment.Listener {

    companion object {

        val FILE_REQUEST_CODE = nextRequestCode()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        setSupportActionBar(toolbar)

        showInitialFragment()

        fab.setOnClickListener {

            appBarcode?.analytics?.eventSelectContent("new_file")

            pickerFile()
        }


        when (intent?.action) {
            Intent.ACTION_VIEW -> {

                intent?.data?.let {
                    openFile(it)
                }

            }

            Intent.ACTION_SEND -> {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    openFile(it)
                }
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
                        openFile(uri)
                    }

                } else {
                    CALog.i("PICKER_FILE", "cancelled")
                }
            }

            else -> {
                CALog.e("PICKER_FILE", "not found request code")
            }
        }
    }

    private fun openFile(uri: Uri) {
        showProgressFragment(uri)
    }


    override fun onBarcodeScannerSuccess(barcodeModels: List<BarcodeModel>) {


        if (barcodeModels.isEmpty()) {

            showToast(getString(R.string.not_found_barcode), Toast.LENGTH_LONG)
            showInitialFragment()

        } else {

            showBarcodeFragment(barcodeModels)

        }
    }

   private fun showInitialFragment() {

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, InitialFragment())
            .commitNowAllowingStateLoss()

    }


    private fun showProgressFragment(uri: Uri) {

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, ProgressFragment.newInstance(uri))
            .commitNowAllowingStateLoss()

    }


    private fun showBarcodeFragment(barcodeModels: List<BarcodeModel>) {

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, BarcodeFragment.newInstance(barcodeModels))
            .commitNowAllowingStateLoss()

    }


    override fun onBarcodeScannerError() {

        showToast(getString(R.string.unknown_error), Toast.LENGTH_LONG)

        showInitialFragment()

    }
}


class BarcodeSendActivity : BarcodeActivity()