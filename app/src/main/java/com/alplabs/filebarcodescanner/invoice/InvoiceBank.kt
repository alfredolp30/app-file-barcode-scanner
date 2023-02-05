package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.addSubstring
import com.alplabs.filebarcodescanner.extension.digitToInt
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceBank(private val barcode: String) : InvoiceBase() {

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

    override val calendar : GregorianCalendar get() {
        val calendar = GregorianCalendar()

        calendar.set(1997, 9, 7)

        calendar.add(GregorianCalendar.DAY_OF_YEAR, factorExpiration)

        return calendar
    }


    override val barcodeWithDigits: String get() {
        val block1 = barcode.substring(0, 4) + barcode.substring(19, 24)
        val block2 = barcode.substring(24, 34)
        val block3 = barcode.substring(34, 44)
        val block4 = barcode.substring(0, 4) + barcode.substring(5)
        val block5 = barcode.substring(5, 9) + barcode.substring(9, 19)

        return block1 + digit10(block1) + block2  + digit10(block2) + block3 + digit10(block3) + digit11(block4) + block5
    }

    override fun digit11(block: String): Int {

        val multiplier = createWeights(listOf(2, 3, 4, 5, 6, 7, 8, 9), block.length)

        var sum = 0

        block.reversed().forEachIndexed { index, c ->

            c.digitToInt()?.let { n ->

                sum += n * multiplier[index]

            }

        }


        val digit = 11 - (sum % 11)

        return when (digit) {
            0, 10, 11 -> 1

            else -> digit
        }
    }
}