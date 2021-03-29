package com.alplabs.filebarcodescanner.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.alplabs.filebarcodescanner.database.model.BarcodeData
import com.alplabs.filebarcodescanner.invoice.InvoiceBank
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.alplabs.filebarcodescanner.invoice.InvoiceInterface
import java.io.File
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
open class BarcodeModel(

    val barcode: String,
    protected val calendar: GregorianCalendar?,
    var title: String = "",
    val path: String

) {

    val invoice : InvoiceInterface get() {
        return if (InvoiceChecker(barcode).isCollection)
            InvoiceCollection(barcode, calendar)
        else
            InvoiceBank(barcode)
    }


    open val barcodeData : BarcodeData
        get() {

            return BarcodeData(
                barcode,
                calendar?.timeInMillis,
                readDatetime = GregorianCalendar().timeInMillis,
                title,
                path
            )

        }

    val pathUri : Uri? = if (path.isNotBlank()) Uri.fromFile(File(path)) else null

    fun pathProviderUri(context: Context) : Uri? {
        return if (path.isNotBlank()) {
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                File(path))
        } else {
            null
        }
    }
}