package com.alplabs.filebarcodescanner.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.cell_barcode.view.*

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val txtRawBarcode = view.txtRawBarcode
    val imgBtnShare = view.imgBtnShare
}