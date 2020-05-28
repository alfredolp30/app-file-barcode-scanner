package com.alplabs.filebarcodescanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.viewmodel.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import com.google.android.gms.vision.barcode.Barcode
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class Barcode2Adapter(val barcodeModels: MutableList<BarcodeModel>, listener: Listener)
    : RecyclerView.Adapter<BarcodeViewHolder>() {

    private val weakListener = WeakReference(listener)

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val sdfRead = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
    private val df = DecimalFormat("#.00")


    interface Listener {
        fun onChangeTitle(barcodeModel: BarcodeModel)
        fun onCopy(barcodeWithDigits: String)
        fun onDelete(barcodeModel: BarcodeHistoryModel)
    }

    private enum class ModelType(val type: Int) {
        NORMAL(1),
        HISTORY(2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {

        val layout = when (viewType) {

            ModelType.HISTORY.type -> R.layout.cell_barcode_history

            else -> R.layout.cell_barcode


        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return BarcodeViewHolder(view)
    }

    override fun getItemCount(): Int = barcodeModels.count()

    override fun getItemViewType(position: Int): Int {
        return if (barcodeModels[position] is BarcodeHistoryModel) {
            ModelType.HISTORY.type
        } else {
            ModelType.NORMAL.type
        }
    }

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {

        val barcodeModel = barcodeModels[position]

        val title = barcodeModel.title
        val barcodeWithDigits = barcodeModel.invoice.barcodeWithDigits
        val calendar = barcodeModel.invoice.calendar
        val value = barcodeModel.invoice.value

        val ctx = holder.itemView.context

        holder.txtTitle?.text = if (title.isBlank()) ctx.getString(R.string.invoice_title_default) else title
        holder.txtTitle?.setOnClickListener {
            weakListener.get()?.onChangeTitle(barcodeModel)
        }

        holder.txtBarcode?.text = barcodeWithDigits

        if (calendar != null) {
            holder.txtDate?.text = sdf.format(calendar.time)
        } else {
            holder.txtDate?.setText(R.string.without_date)
        }


        holder.txtValue?.text = ctx.getString(R.string.value, df.format(value))

        holder.btnCopy?.setOnClickListener {
            weakListener.get()?.onCopy(barcodeWithDigits = barcodeWithDigits)
        }

        if (barcodeModel is BarcodeHistoryModel) {
            val readCalendar = barcodeModel.readCalendar

            val txtRead = sdfRead.format(readCalendar.time)
            holder.txtRead?.text = txtRead

            holder.btnDelete?.setOnClickListener {

                weakListener.get()?.onDelete(barcodeModel = barcodeModel)

            }
        }

    }
}