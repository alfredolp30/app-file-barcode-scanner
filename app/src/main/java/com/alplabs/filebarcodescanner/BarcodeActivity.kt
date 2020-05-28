package com.alplabs.filebarcodescanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alplabs.filebarcodescanner.adapter.Barcode2Adapter
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.eventbus.ChangedBarcodeData
import com.alplabs.filebarcodescanner.viewmodel.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import kotlinx.android.synthetic.main.fragment_barcode.*
import org.greenrobot.eventbus.EventBus

class BarcodeActivity :

    BaseActivity(),
    Barcode2Adapter.Listener {

    private val clipboardManager get() = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val adapter = Barcode2Adapter(mutableListOf(), this)

    private val alertTitleManager = AlertTitleManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rcBarcode.layoutManager = LinearLayoutManager(this)
        rcBarcode.adapter = adapter

        loadData()
    }


    private fun loadData() {
        val database = appBarcode?.database ?: return


        DatabaseManager.executeInBackground(
            execution = {
                database.barcodeDataDao().last().toBarcodeModel()
            },

            resultCallback = {

                progressBar?.visibility = View.GONE

                it?.let{ barcodeModel ->
                    loadData(listOf(barcodeModel))
                }
            }
        )
    }

    private fun loadData(barcodeModels: List<BarcodeModel>) {
        adapter.barcodeModels.addAll(barcodeModels)
        adapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onChangeTitle(barcodeModel: BarcodeModel) {
        if (isDestroyed || isFinishing) return

        alertTitleManager.createAlert(this, barcodeModel.title) { title ->
            changedTitle(barcodeModel, title)
        }
    }

    private fun changedTitle(barcodeModel: BarcodeModel, title: String) {
        val database = appBarcode?.database ?: return

        barcodeModel.title = title

        val index = adapter.barcodeModels.indexOfFirst { it.barcode == barcodeModel.barcode }
        if (index >= 0) {
            adapter.notifyItemChanged(index)
        }

        val barcodeData = barcodeModel.barcodeData

        DatabaseManager.executeInBackground(
            execution = {

                database.barcodeDataDao().replace(barcodeData = barcodeData)
            },

            resultCallback = {

                EventBus.getDefault().post(ChangedBarcodeData(barcodeData))

            }
        )
    }

    override fun onCopy(barcodeWithDigits: String) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("barcode", barcodeWithDigits))

        showToast(getString(R.string.barcode_copy_success))
    }

    override fun onDelete(barcodeModel: BarcodeHistoryModel) {}

}
