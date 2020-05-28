package com.alplabs.filebarcodescanner.fragment


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alplabs.filebarcodescanner.AlertTitleManager
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.adapter.Barcode2Adapter
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.eventbus.*
import com.alplabs.filebarcodescanner.viewmodel.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import kotlinx.android.synthetic.main.fragment_barcode.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class BarcodeFragment :
    BaseFragment(),
    Barcode2Adapter.Listener {

    private val clipboardManager get() = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val adapter = Barcode2Adapter(mutableListOf(), this)

    private val alertTitleManager = AlertTitleManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_barcode, container, false)

        view.rcBarcode.layoutManager = LinearLayoutManager(requireContext())
        view.rcBarcode.adapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()

        EventBus.getDefault().unregister(this)
    }

    private fun loadData() {
        val database = appBarcode?.database ?: return

        DatabaseManager.executeInBackground(
            execution = {
                database.barcodeDataDao().list().reversed().map { it.toBarcodeHistoryModel() }
            },

            resultCallback = {

                view?.progressBar?.visibility = View.GONE

                it?.let{ barcodeModels ->
                    loadData(barcodeModels)
                }
            }
        )


    }

    private fun loadData(barcodeModels: List<BarcodeHistoryModel>) {
        adapter.barcodeModels.addAll(barcodeModels)
        adapter.notifyDataSetChanged()
    }

    override fun onChangeTitle(barcodeModel: BarcodeModel) {
        val isDestroyed = activity?.isDestroyed ?: true
        val isFinishing = activity?.isFinishing ?: true

        if (isDestroyed || isFinishing) return

        context?.let { ctx ->
            alertTitleManager.createAlert(ctx, barcodeModel.title) { title ->
                changedTitle(barcodeModel, title)
            }
        }
    }

    private fun changedTitle(barcodeModel: BarcodeModel, title: String) {

        val database = appBarcode?.database ?: return

        barcodeModel.title = title

        val index = adapter.barcodeModels.indexOfFirst { it.barcode == barcodeModel.barcode }
        if (index >= 0) {
            adapter.notifyItemChanged(index)
        }

        DatabaseManager.executeInBackground(
            execution = {
                database.barcodeDataDao().replace(barcodeData = barcodeModel.barcodeData)
            },

            resultCallback = {

            }
        )
    }

    override fun onCopy(barcodeWithDigits: String) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("barcode", barcodeWithDigits))

        baseActivity?.showToast(getString(R.string.barcode_copy_success))
    }

    override fun onDelete(barcodeModel: BarcodeHistoryModel) {
        val database = appBarcode?.database ?: return

        DatabaseManager.executeInBackground(
            execution = {
                database.barcodeDataDao().remove(barcodeModel.barcodeData)
            },

            resultCallback = {

                val index = adapter.barcodeModels.indexOfFirst { model -> model.barcode == barcodeModel.barcode }

                if (index >= 0) {
                    adapter.barcodeModels.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }

            }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(newBarcodeData: NewBarcodeData) {

        val barcodeData = newBarcodeData.barcodeData

        val index = adapter.barcodeModels.indexOfFirst { b -> b.barcode == barcodeData.barcode }
        if (index >= 0) {
            adapter.barcodeModels.removeAt(index)
            adapter.notifyItemRemoved(index)
        }

        adapter.barcodeModels.add(0, barcodeData.toBarcodeHistoryModel())
        adapter.notifyItemInserted(0)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(changeBarcodeData: ChangedBarcodeData) {

        val barcodeData = changeBarcodeData.barcodeData

        val index = adapter.barcodeModels.indexOfFirst { b -> b.barcode == barcodeData.barcode }
        if (index >= 0) {
            adapter.barcodeModels[index] = barcodeData.toBarcodeHistoryModel()
            adapter.notifyItemChanged(index)
        }

    }
}
