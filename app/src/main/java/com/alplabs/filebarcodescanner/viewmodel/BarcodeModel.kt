package com.alplabs.filebarcodescanner.viewmodel

import android.os.Parcel
import android.os.Parcelable
import com.alplabs.filebarcodescanner.database.model.BarcodeData
import com.alplabs.filebarcodescanner.extension.toBoolean
import com.alplabs.filebarcodescanner.invoice.InvoiceBank
import com.alplabs.filebarcodescanner.invoice.InvoiceChecker
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.alplabs.filebarcodescanner.invoice.InvoiceInterface
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeModel(val barcode: String,
                   private val calendar: GregorianCalendar?) : Parcelable {

    val invoice : InvoiceInterface by lazy {
        if (InvoiceChecker(barcode).isCollection) InvoiceCollection(barcode, calendar) else InvoiceBank(barcode)
    }

    val barcodeData : BarcodeData by lazy {

        BarcodeData(
            barcode = barcode,
            datetime = calendar?.timeInMillis,
            readDatetime = GregorianCalendar().timeInMillis
        )

    }

    constructor(parcel: Parcel) : this(
        barcode = parcel.readString() ?: "",
        calendar = convertToGregorianCalendar(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(barcode)
        parcel.writeLong(calendar?.timeInMillis ?: NONE_TIME_MILLIS)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BarcodeModel> {
        override fun createFromParcel(parcel: Parcel): BarcodeModel {
            return BarcodeModel(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeModel?> {
            return arrayOfNulls(size)
        }


        const val NONE_TIME_MILLIS = -1L

        fun convertToGregorianCalendar(value: Long) : GregorianCalendar? {

            return if (value == NONE_TIME_MILLIS) {
                null
            } else {
                GregorianCalendar().apply {
                    timeInMillis = value
                }
            }
        }
    }


}