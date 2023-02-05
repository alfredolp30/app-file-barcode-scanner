package com.alplabs.filebarcodescanner.invoice

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceChecker(barcode: String) {
    private val containsOnlyDigits = barcode.all { it.isDigit() }
    private val isBarcode = barcode.length == 44 && containsOnlyDigits

    val isBarcodeBank = isBarcode
    val isBarcodeCollection = isBarcode && barcode[0] == '8'

}