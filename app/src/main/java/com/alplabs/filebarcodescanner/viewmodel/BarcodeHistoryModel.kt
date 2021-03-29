package com.alplabs.filebarcodescanner.viewmodel


import com.alplabs.filebarcodescanner.database.model.BarcodeData
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2020-02-04.
 * Copyright Universo Online 2020. All rights reserved.
 */
class BarcodeHistoryModel(

    barcode: String,
    calendar: GregorianCalendar?,
    val readCalendar: GregorianCalendar,
    title: String,
    path: String

) : BarcodeModel(barcode, calendar, title, path) {


    override val barcodeData : BarcodeData
        get() {

            return BarcodeData(
                barcode,
                datetime = calendar?.timeInMillis,
                readDatetime = readCalendar.timeInMillis,
                title,
                path
            )

        }


}