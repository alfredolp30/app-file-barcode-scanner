package com.alplabs.filebarcodescanner.invoice

import java.util.*


/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
interface InvoiceInterface {
    val value: Double
    val calendar: GregorianCalendar?
    val barcodeWithDigits: String

    fun digit11(block: String) : Int
    fun digit10(block: String) : Int
}