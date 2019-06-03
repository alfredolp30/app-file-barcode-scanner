package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.addSubstring
import com.alplabs.filebarcodescanner.extension.digitToInt
import java.util.*
import kotlin.math.abs

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

    override val date : GregorianCalendar? get() {
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

        return block1 + digit10(block1, false) + block2  + digit10(block2) + block3 + digit10(block3) + digit11(block4) + block5
    }


    private fun digit10(block: String, isStartOne: Boolean = true) : Int {

        val multiplier = createWeights(if (isStartOne) listOf(1, 2)  else listOf(2, 1), block.length)

        var sum = 0

        block.forEachIndexed { index, c ->

            c.digitToInt()?.let { n ->

                var multi = (n * multiplier[index])

                while (multi > 10) {
                    var sumInternal = 0

                    multi.toString().forEach { nInternal ->
                        sumInternal += nInternal.digitToInt()!!
                    }

                    multi = sumInternal
                }

                sum += multi

            }
        }

        val digit = (10 - (sum % 10)) % 10



        return digit
    }
}