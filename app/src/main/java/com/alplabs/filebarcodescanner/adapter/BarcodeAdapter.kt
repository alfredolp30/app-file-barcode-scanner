package com.alplabs.filebarcodescanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.model.BarcodeModel
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeAdapter(val barcodesModel: MutableList<BarcodeModel>, listener: Listener) : RecyclerView.Adapter<BarcodeViewHolder>() {

    private val weakListener = WeakReference(listener)

    interface Listener {
        fun onShare(rawValue: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_barcode, parent, false)
        return BarcodeViewHolder(view)
    }

    override fun getItemCount(): Int = barcodesModel.count()

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {

        val displayValue = barcodesModel[position].displayValue

        holder.txtRawBarcode.text = displayValue

        holder.imgBtnShare?.setOnClickListener {
            weakListener.get()?.onShare(rawValue = displayValue)
        }
    }
}