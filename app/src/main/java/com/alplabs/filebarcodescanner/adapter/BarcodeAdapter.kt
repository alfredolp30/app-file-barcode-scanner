package com.alplabs.filebarcodescanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.extension.setTextWithValue
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.alplabs.filebarcodescanner.model.BarcodeModel
import kotlinx.android.synthetic.main.cell_barcode.view.*
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeAdapter(val barcodeModels: MutableList<BarcodeModel>, listener: Listener) : RecyclerView.Adapter<BarcodeViewHolder>() {

    private val weakListener = WeakReference(listener)

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val df = DecimalFormat("#.00")


    interface Listener {
        fun onCopy(rawValue: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_barcode, parent, false)
        return BarcodeViewHolder(view)
    }

    override fun getItemCount(): Int = barcodeModels.count()

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {

        val barcodeModel = barcodeModels[position]

        val barcode = barcodeModel.invoice.barcodeWithDigits
        val date = barcodeModel.invoice.date
        val value = barcodeModel.invoice.value

        holder.itemView.txtBarcode.text = barcode

        if (date != null) {
            holder.itemView.txtDate.text = sdf.format(date.time)
        } else {
            holder.itemView.txtDate.setText(R.string.without_date)
        }


        holder.itemView.txtValue.text = holder.itemView.context.getString(R.string.value, df.format(value))

        holder.itemView.btnCopy.setOnClickListener {
            weakListener.get()?.onCopy(rawValue = barcode)
        }
    }
}