package com.alplabs.filebarcodescanner.database.model

import androidx.room.Entity
import com.alplabs.filebarcodescanner.model.BarcodeHistoryModel
import com.alplabs.filebarcodescanner.model.BarcodeModel
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
@Entity(primaryKeys = ["barcode"])
class BarcodeData(

    val barcode: String,
    val datetime: Long?,
    val readDatetime: Long,
    val title: String,
    val path: String

) {

    fun toBarcodeModel() = BarcodeModel(
        barcode,
        calendar = datetimeToCalendar(datetime),
        title,
        path
    )


    fun toBarcodeHistoryModel() = BarcodeHistoryModel(
        barcode,
        calendar = datetimeToCalendar(datetime),
        readCalendar = datetimeToCalendar(readDatetime),
        title,
        path
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