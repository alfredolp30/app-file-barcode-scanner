package com.alplabs.filebarcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.alplabs.filebarcodescanner.fragment.BarcodeFragment
import com.alplabs.filebarcodescanner.fragment.InitialFragment
import com.alplabs.filebarcodescanner.fragment.ProgressFragment
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel

import kotlinx.android.synthetic.main.activity_barcode.*
import kotlinx.android.synthetic.main.cell_initial.*

open class BarcodeActivity : BaseActivity(), InitialFragment.Listener, ProgressFragment.Listener {

    companion object {

        val REQUEST_CODE_FILE = nextRequestCode()
        val RESULT_CODE_CAMERA_ACTIVITY = nextRequestCode()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        setSupportActionBar(toolbar)

        showInitialFragment()

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

    override fun onSupportNavigateUp(): Boolean {
        showInitialFragment()
        return true
    }


    override fun onSelectArchive() {
        appBarcode?.analytics?.eventSelectContent("select_archive")

        pickerFile()
    }

    override fun onSelectCamera() {
        appBarcode?.analytics?.eventSelectContent("select_camera")

        cameraCapture()
    }


    private fun pickerFile() {

       Intent(Intent.ACTION_OPEN_DOCUMENT).also {

            it.addCategory(Intent.CATEGORY_OPENABLE)
            it.type = "*/*"
            it.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpg", "image/png", "image/jpeg", "image/bmp", "application/pdf", "text/html"))


            startActivityForResult(it, REQUEST_CODE_FILE)
        }


    }


    private fun cameraCapture() {

        Intent(this, CameraActivity::class.java).also {
            startActivityForResult(it, RESULT_CODE_CAMERA_ACTIVITY)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_FILE -> {
                if (resultCode == Activity.RESULT_OK) {

                    data?.data?.also { uri ->
                        openFile(uri)
                    }

                } else {
                    CALog.i("PICKER_FILE", "cancelled")
                }
            }

            RESULT_CODE_CAMERA_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {

                    val barcodeModels =
                        data?.extras?.getParcelableArrayList<BarcodeModel>(CameraActivity.BARCODE) ?: arrayListOf()

                    showBarcodeFragment(barcodeModels)

                } else {
                    CALog.i("CAMERA_ACTIVITY", "cancelled")
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
       supportActionBar?.setDisplayHomeAsUpEnabled(false)

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, InitialFragment())
            .commitAllowingStateLoss()

    }


    private fun showProgressFragment(uri: Uri) {

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, ProgressFragment.newInstance(uri))
            .commitAllowingStateLoss()

    }


    private fun showBarcodeFragment(barcodeModels: List<BarcodeModel>) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, BarcodeFragment.newInstance(barcodeModels))
            .commitAllowingStateLoss()

    }


    override fun onBarcodeScannerError(error: Error?) {

        val errorMsg: String = error?.localizedMessage ?: getString(R.string.unknown_error)

        showToast(errorMsg, Toast.LENGTH_LONG)

        showInitialFragment()

    }
}


class BarcodeSendActivity : BarcodeActivity()