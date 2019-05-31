package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.addSubstring
import java.util.*
import kotlin.math.abs

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceBank(private val barcode: String) : InvoiceInterface {

    override val value: Double get() {
        var str = barcode.substring(9, 19)
        str = str.trimStart { it == '0' }

        val diff = 3 - str.length

        if (diff > 0) {
            str = str.addSubstring(0, "0".repeat(diff))
        }

        if (str.length >= 3) {
            str = str.addSubstring(str.length-2, ".")
        }

        return str.toDoubleOrNull() ?: 0.0
    }

    private val factorExpiration: Int get() = barcode.substring(5, 9).trimStart { it == '0' }.toIntOrNull() ?: 0

    override val date : GregorianCalendar? get() {
        val calendar = GregorianCalendar()

        calendar.set(1997, 9, 7)

        calendar.add(GregorianCalendar.DAY_OF_YEAR, factorExpiration)

        return calendar
    }
}