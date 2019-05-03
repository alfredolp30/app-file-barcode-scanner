package com.alplabs.filebarcodescanner.model

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by Alfredo L. Porfirio on 01/03/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
class BarcodeModel(val displayValue: String) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayValue)
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
    }
}