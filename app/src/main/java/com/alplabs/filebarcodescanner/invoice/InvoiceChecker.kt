package com.alplabs.filebarcodescanner.invoice

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
class InvoiceChecker(barcode: String) {
    val isValid = barcode.length == 44 && barcode.dropWhile { it.isDigit() }.isEmpty()
    val isCollection = isValid && barcode[0] == '8'
}