package com.alplabs.filebarcodescanner.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alplabs.filebarcodescanner.R

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val txtTitle : TextView? = view.findViewById(R.id.txtTitle)
    val txtBarcode : TextView? = view.findViewById(R.id.txtBarcode)
    val txtDate : TextView? = view.findViewById(R.id.txtDate)
    val txtValue : TextView? = view.findViewById(R.id.txtValue)
    val btnCopy : Button? = view.findViewById(R.id.btnCopy)
    val txtRead : TextView? = view.findViewById(R.id.txtRead)
    val btnDelete : Button? = view.findViewById(R.id.btnDelete)
}