package com.alplabs.filebarcodescanner.fragment


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.adapter.BarcodeAdapter
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.fragment_barcodes.view.*


private const val BARCODE = "barcode"

class BarcodeFragment : BaseFragment(), BarcodeAdapter.Listener {


    private val clipboardManager get() = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val adapter = BarcodeAdapter(mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
           it.getParcelableArray(BARCODE)?.filter { item -> item is BarcodeModel }?.forEach { item ->
               adapter.barcodesModel.add(item as BarcodeModel)
           }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_barcodes, container, false)

        view.rcBarcode.layoutManager = LinearLayoutManager(requireContext())
        view.rcBarcode.adapter = adapter

        return view
    }

    override fun onCopy(rawValue: String) {
        clipboardManager?.primaryClip = ClipData.newPlainText("barcode", rawValue)

        baseActivity?.showToast(getString(R.string.barcode_copy_success))
    }


    companion object {
        @JvmStatic
        fun newInstance(barcodeModels: List<BarcodeModel>) =

            BarcodeFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArray(BARCODE, barcodeModels.toTypedArray())
                }
            }

    }
}
