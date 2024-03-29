package com.alplabs.filebarcodescanner.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alplabs.filebarcodescanner.R
import com.alplabs.filebarcodescanner.model.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.squareup.picasso.Picasso
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeAdapter(val barcodeModels: MutableList<BarcodeModel>, listener: Listener)
    : RecyclerView.Adapter<BarcodeViewHolder>() {

    private val weakListener = WeakReference(listener)

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val sdfRead = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
    private val df = DecimalFormat("#.00")


    interface Listener {
        fun onChangeTitle(barcodeModel: BarcodeModel)
        fun onCopy(barcodeWithDigits: String)
        fun onDelete(barcodeModel: BarcodeHistoryModel)
        fun onOpenPreview(uri: Uri)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_barcode_history, parent, false)
        return BarcodeViewHolder(view)
    }

    override fun getItemCount(): Int = barcodeModels.count()

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {

        val barcodeModel = barcodeModels[position]

        val title = barcodeModel.title
        val barcodeWithDigits = barcodeModel.invoice.barcodeWithDigits
        val calendar = barcodeModel.invoice.calendar
        val value = barcodeModel.invoice.value

        val ctx = holder.itemView.context

        val previewUri = barcodeModel.pathUri
        if (previewUri != null && holder.imgPreview != null) {
            Picasso.get()
                .load(previewUri)
                .resizeDimen(R.dimen.preview_image_width,  R.dimen.preview_image_height)
                .into(holder.imgPreview)

            holder.imgPreview.setOnClickListener {
                barcodeModel.pathProviderUri(it.context)?.let { uriProvider ->
                    weakListener.get()?.onOpenPreview(uriProvider)
                }
            }

        } else {
            holder.imgPreview?.visibility = View.GONE
        }

        holder.constraintEnd?.visibility = if (barcodeModel is BarcodeHistoryModel) {
            View.VISIBLE
        } else {
            View.GONE
        }

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