package com.alplabs.filebarcodescanner.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import com.alplabs.filebarcodescanner.scanner.AsyncHtml2Bitmap
import com.alplabs.filebarcodescanner.scanner.AsyncPdf2Bitmap
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import com.alplabs.filebarcodescanner.scanner.DetectorService
import java.lang.ref.WeakReference
import android.widget.EditText
import android.app.AlertDialog
import com.alplabs.filebarcodescanner.R
import java.util.*


private const val FILE_URI = "file_uri"

class ProgressFragment : BaseFragment(), AsyncHtml2Bitmap.Listener,
    AsyncPdf2Bitmap.Listener, DetectorService.Listener {

    private var listener: WeakReference<Listener>? = null
    private var uriFile: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            uriFile = it.getParcelable(FILE_URI)
        }

        start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = WeakReference(context)
        } else {
            throw RuntimeException("$context must implement ProgressFragmentListener")
        }
    }

    interface Listener {
        fun onBarcodeScannerSuccess(barcodeModels: List<BarcodeModel>)
        fun onBarcodeScannerError(error: Error? = null)
    }

    companion object {

        @JvmStatic
        fun newInstance(fileUri: Uri) =
            ProgressFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(FILE_URI, fileUri)
                }
            }
    }


    private fun start() {
        val uri = uriFile ?: return

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(requireContext().contentResolver.getType(uri))

        CALog.i("FILE_REQ_SUCCESS", "Uri: $uri extension: $extension")

        when (extension) {
            "pdf" -> {
                pdf2Bitmap(uri)
            }

            "jpeg", "png", "jpg", "bmp" -> {
                scannerBarcode(listOf(uri))
            }

            "htm" -> {
                html2Bitmap(uri)
            }

            else -> {
                CALog.e("FILE_REQ_SUCCESS", "Not supported file extension")
            }
        }
    }


    private fun pdf2Bitmap(pdfUri: Uri, password: String? = null) {
        val ctx = context

        if (ctx != null) {
            AsyncPdf2Bitmap(ctx, this).execute(AsyncPdf2Bitmap.WorkPdf(uri = pdfUri, password = password))
        } else {
            listener?.get()?.onBarcodeScannerError()
        }

    }

    override fun onFinishPdf2Bitmap(uris: List<Uri>) {
        scannerBarcode(uris)
    }

    override fun onRequiredPassword(fileName: String) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(getString(R.string.required_password, fileName))

        val view = LayoutInflater.from(context).inflate(R.layout.alert_password, null, false)

        val edtTextPass = view.findViewById<EditText>(R.id.edtTextPass)

        alert.setView(view)

        alert.setPositiveButton(R.string.confirm) { _, _ ->
            edtTextPass.clearComposingText()

            val password = edtTextPass.text.toString().trim()

            pdf2Bitmap(this.uriFile!!, password)
        }

        alert.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            listener?.get()?.onBarcodeScannerError(Error(getString(R.string.cancel_digit_password)))
        }

        if (activity?.isFinishing == false && activity?.isDestroyed == false) alert.show()
    }


    private fun html2Bitmap(htmlUri: Uri) {
        val ctx = context

        if (ctx != null) {
            AsyncHtml2Bitmap(ctx, this).execute(htmlUri)
        } else {
            listener?.get()?.onBarcodeScannerError()
        }

    }

    override fun onFinishHtml2Bitmap(uri: Uri?) {

        if (uri != null) {
            scannerBarcode(listOf(uri))
        } else {
            listener?.get()?.onBarcodeScannerError()
        }
    }


    private fun scannerBarcode(uris: List<Uri>) {
        val ctx = context

        if (uris.isNotEmpty() && ctx != null) {
            val detector = DetectorService(ctx, this, uris = ArrayDeque(uris))
            detector.start()
        } else {

            listener?.get()?.onBarcodeScannerError()
        }
    }

    override fun onFinishDetectorService(barcodeModels: List<BarcodeModel>) {
        listener?.get()?.onBarcodeScannerSuccess(barcodeModels)
    }
}
