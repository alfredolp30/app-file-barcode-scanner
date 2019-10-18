package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.addSubstring
import com.alplabs.filebarcodescanner.extension.digitToInt
import java.util.*
import kotlin.math.abs

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceCollection(private val barcode: String,
                        override val date: GregorianCalendar?) : InvoiceBase() {

    private val effectiveValue: String by lazy { barcode.substring(2, 3) }

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


    override val barcodeWithDigits: String get() {

        val block1 = barcode.substring(0, 11)
        val block2 = barcode.substring(11, 22)
        val block3 = barcode.substring(22, 33)
        val block4 = barcode.substring(33)


        val digit1 = digit(block1)
        val digit2 = digit(block2)
        val digit3 = digit(block3)
        val digit4 = digit(block4)

        return block1 + digit1 + block2 + digit2 + block3 + digit3 + block4 + digit4
    }


    override fun digit11(block: String) : Int {

        val multiplier = createWeights(listOf(2, 3, 4, 5, 6, 7, 8, 9), block.length)

        var sum = 0

        block.reversed().forEachIndexed { index, c ->

            c.digitToInt()?.let { n ->

                sum += n * multiplier[index]

            }

        }

        val digit = when (val rest = sum % 11) {
            0, 1 -> 0

            10 -> 1

            else -> 11 - rest
        }

        return digit
    }

    private fun digit(block: String): Int {

        return when (effectiveValue) {
            "6", "7" -> {
                digit10(block)
            }

            else -> {
                digit11(block)
            }
        }
    }
}