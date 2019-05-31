package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.addSubstring
import java.util.*
import kotlin.math.abs

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceCollection(private val barcode: String) : InvoiceInterface {

    private val mapSegmentation = mapOf(

        '0' to "Desconhecido",
        '1' to "Prefeitura",
        '2' to "Saneamento",
        '3' to "Energia Elétrica e Gás",
        '4' to "Telecomunicações",
        '5' to "Órgãos Governamentais",
        '6' to "Carnes e Assemelhados",
        '7' to "Multas de trânsito",
        '9' to "Uso exclusivo do banco"

    )

    val segmentation : String? = mapSegmentation[barcode[1]]

    override val value: Double get() {
        var str = barcode.substring(4, 15)
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


    override val date : GregorianCalendar? get() {

        val year = barcode.substring(19, 23).trimStart { it == '0' }.toIntOrNull() ?: 0
        val month = barcode.substring(23, 25).trimStart { it == '0' }.toIntOrNull() ?: 0
        val day = barcode.substring(25, 27).trimStart { it == '0' }.toIntOrNull() ?: 0


        if (year >= 2000 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
            return null
        }


        val calendar = GregorianCalendar()
        calendar.set(year, month-1, day)

        val currentCalendar = GregorianCalendar()

        val timeMillis = abs(currentCalendar.timeInMillis - calendar.timeInMillis)

        val days = timeMillis / (1000 * 60 * 60 * 24)

        if (days <= 365 * 4) {
            return calendar
        }

        return null
    }
}