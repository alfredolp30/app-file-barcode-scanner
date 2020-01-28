package com.alplabs.filebarcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.fragment.InitialFragment
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.ui.main.SectionsPagerAdapter
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.activity_tab_main.*


open class TabMainActivity :
    BaseActivity(),
    InitialFragment.Listener,
    BarcodeDetectorManager.Listener {

    companion object {

        val REQUEST_CODE_FILE = nextRequestCode()
        val RESULT_CODE_CAMERA_ACTIVITY = nextRequestCode()

    }

    private var bdm = BarcodeDetectorManager(context = this, listener = this)
    private var alert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)

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

                    saveBarcodeAndShow(barcodeModels)

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

        alert?.hide()


        if (barcodeModels.isEmpty()) {

            showToast(getString(R.string.not_found_barcode), Toast.LENGTH_LONG)

        } else {

            saveBarcodeAndShow(barcodeModels)

        }
    }

    override fun onBarcodeScannerError(error: Error?) {

        alert?.hide()

        val errorMsg: String = error?.localizedMessage ?: getString(R.string.unknown_error)

        showToast(errorMsg, Toast.LENGTH_LONG)

    }


    private fun showProgressFragment(uri: Uri) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle(R.string.processing_file)
        alertBuilder.setView(layoutInflater.inflate(R.layout.alert_progress, null))
        alertBuilder.setNegativeButton(R.string.cancel) { _, _ ->
            bdm = BarcodeDetectorManager(this, this)
        }
        alertBuilder.setOnCancelListener {
            bdm = BarcodeDetectorManager(this, this)
        }


        alert = alertBuilder.create()

        if (!isDestroyed && !isFinishing) {
            alert?.show()
            bdm.start(uri)
        }

    }


    private fun saveBarcodeAndShow(barcodeModels: List<BarcodeModel>) {

        DatabaseManager.executeInBackground(
            execution = {
                appBarcode?.let { appBarcode ->
                    barcodeModels.forEach {
                        appBarcode.database.barcodeDataDao().add(it.barcodeData)
                    }
                }
            },

            resultCallback = {

                Intent(this, BarcodeActivity::class.java).also {
                    startActivity(it)
                }

            }
        )

    }
}


class BarcodeSendActivity : TabMainActivity()