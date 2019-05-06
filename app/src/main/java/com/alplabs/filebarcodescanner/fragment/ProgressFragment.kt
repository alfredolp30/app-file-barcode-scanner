package com.alplabs.filebarcodescanner.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import com.alplabs.filebarcodescanner.scanner.AsyncHtml2Bitmap
import com.alplabs.filebarcodescanner.scanner.AsyncPdf2Bitmap
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.metrics.CALog
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.alplabs.filebarcodescanner.scanner.FirebaseBarcodeDetector
import java.lang.ref.WeakReference

private const val FILE_URI = "file_uri"

class ProgressFragment : BaseFragment(), AsyncHtml2Bitmap.Listener,
    AsyncPdf2Bitmap.Listener, FirebaseBarcodeDetector.Listener {

    private var listener: WeakReference<Listener>? = null
    private var uriFile: Uri? = null


    private val detector = FirebaseBarcodeDetector(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            uriFile = it.getParcelable(FILE_URI)
        }

        if (uriFile != null) {
            start()
        }
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

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface Listener {
        fun onBarcodeScannerSuccess(barcodeModels: List<BarcodeModel>)
        fun onBarcodeScannerError()
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


    private fun pdf2Bitmap(pdfUri: Uri) {
        AsyncPdf2Bitmap(requireContext(), this).execute(pdfUri)
    }

    override fun onFinishPdf2Bitmap(uris: List<Uri>) {
        scannerBarcode(uris)
    }


    private fun html2Bitmap(htmlUri: Uri) {
        AsyncHtml2Bitmap(requireContext(), this).execute(htmlUri)
    }

    override fun onFinishHtml2Bitmap(uri: Uri) {
        scannerBarcode(listOf(uri))
    }


    private fun scannerBarcode(uris: List<Uri>) {
        detector.scanner(requireContext(), uris)
    }

    override fun onDetectorSuccess(barcodeModels: List<BarcodeModel>) {
        listener?.get()?.onBarcodeScannerSuccess(barcodeModels)
    }

    override fun onDetectorFailure() {
        listener?.get()?.onBarcodeScannerError()
    }
}
