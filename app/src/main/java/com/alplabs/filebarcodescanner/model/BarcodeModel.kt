package com.alplabs.filebarcodescanner.model

import android.os.Parcel
import android.os.Parcelable
import com.alplabs.filebarcodescanner.extension.toBoolean
import com.alplabs.filebarcodescanner.extension.toByte
import com.alplabs.filebarcodescanner.invoice.InvoiceBank
import com.alplabs.filebarcodescanner.invoice.InvoiceCollection
import com.alplabs.filebarcodescanner.invoice.InvoiceInterface
import java.util.*


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeModel(val barcode: String,
                   private val isInvoiceCollection: Boolean,
                   private val date: GregorianCalendar?) : Parcelable {

    val invoice : InvoiceInterface by lazy {
        if (isInvoiceCollection) InvoiceCollection(barcode, date) else InvoiceBank(barcode)
    }

    constructor(parcel: Parcel) : this(
        barcode = parcel.readString() ?: "",
        isInvoiceCollection = parcel.readByte().toBoolean(),
        date = convertToGregorianCalendar(parcel.readLong()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(barcode)
        parcel.writeByte(isInvoiceCollection.toByte())
        parcel.writeLong(date?.timeInMillis ?: NONE_TIME_MILLIS)
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