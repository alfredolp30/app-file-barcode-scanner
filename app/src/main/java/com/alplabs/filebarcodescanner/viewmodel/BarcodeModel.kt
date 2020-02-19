package com.alplabs.filebarcodescanner.viewmodel

import com.alplabs.filebarcodescanner.database.model.BarcodeData
import com.alplabs.filebarcodescanner.invoice.InvoiceBank
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.alplabs.filebarcodescanner.invoice.InvoiceInterface
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
open class BarcodeModel(val barcode: String,
                   protected val calendar: GregorianCalendar?) {

    val invoice : InvoiceInterface get() {
        return if (InvoiceChecker(barcode).isCollection) InvoiceCollection(barcode, calendar) else InvoiceBank(barcode)
    }


    open val barcodeData : BarcodeData
        get() {

            return BarcodeData(
                barcode = barcode,
                datetime = calendar?.timeInMillis,
                readDatetime = GregorianCalendar().timeInMillis
            )

        }


}