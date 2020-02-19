package com.alplabs.filebarcodescanner.database.model

import androidx.room.Entity
import androidx.room.Ignore
import com.alplabs.filebarcodescanner.viewmodel.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.viewmodel.BarcodeModel
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
@Entity(primaryKeys = ["barcode"])
class BarcodeData(

    val barcode: String,
    val datetime: Long?,
    val readDatetime: Long

) {

    fun toBarcodeModel() = BarcodeModel(
        barcode = barcode,
        calendar = datetimeToCalendar(datetime)
    )


    fun toBarcodeHistoryModel() = BarcodeHistoryModel(
        barcode = barcode,
        calendar = datetimeToCalendar(datetime),
        readCalendar = datetimeToCalendar(readDatetime)
    )


    companion object {

        fun datetimeToCalendar(datetime: Long): GregorianCalendar {
            return GregorianCalendar().apply { timeInMillis = datetime }
        }

        fun datetimeToCalendar(datetime: Long?) : GregorianCalendar? {
            return if (datetime != null) {
                datetimeToCalendar(datetime)
            } else {
                null
            }
        }

    }

}