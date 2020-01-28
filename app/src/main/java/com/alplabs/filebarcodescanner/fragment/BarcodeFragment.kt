package com.alplabs.filebarcodescanner.fragment


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.adapter.BarcodeAdapter
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.metrics.CALog
import kotlinx.android.synthetic.main.fragment_barcode.view.*


private const val LOAD_ALL_BARCODE = "load_all_barcode"

class BarcodeFragment :
    BaseFragment(),
    BarcodeAdapter.Listener {


    private val clipboardManager get() = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val adapter = BarcodeAdapter(mutableListOf(), this)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val loadAllBarcode = arguments?.getBoolean(LOAD_ALL_BARCODE) ?: true
        loadData(loadAllBarcode)

    }

    private fun loadData(isAll: Boolean) {
        val database = appBarcode?.database ?: return

        if (isAll) {
            DatabaseManager.executeInBackground(
                execution = {
                    database.barcodeDataDao().list().reversed().map { it.toBarcodeModel() }
                },

                resultCallback = {

                    it?.let{ barcodeModels ->
                        view?.progressBar?.visibility = View.GONE

                        adapter.barcodeModels.addAll(barcodeModels)
                        adapter.notifyDataSetChanged()
                    }
                }
            )
        } else {
            DatabaseManager.executeInBackground(
                execution = {
                    database.barcodeDataDao().last().toBarcodeModel()
                },

                resultCallback = {

                    it?.let{ barcodeModel ->
                        view?.progressBar?.visibility = View.GONE

                        adapter.barcodeModels.add(barcodeModel)
                        adapter.notifyDataSetChanged()
                    }
                }
            )
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_barcode, container, false)

        view.rcBarcode.layoutManager = LinearLayoutManager(requireContext())
        view.rcBarcode.adapter = adapter

        return view
    }

    override fun onCopy(rawValue: String) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("barcode", rawValue))

        baseActivity?.showToast(getString(R.string.barcode_copy_success))
    }


    companion object {
        @JvmStatic
        fun newInstance(loadAllBarcode: Boolean) =

            BarcodeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(LOAD_ALL_BARCODE, loadAllBarcode)
                }
            }

    }
}
