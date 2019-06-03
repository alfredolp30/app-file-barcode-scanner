package com.alplabs.filebarcodescanner.extension

/**
 * Created by Alfredo L. Porfirio on 2019-06-03.
 * Copyright Universo Online 2019. All rights reserved.
 */
fun Char.digitToInt() : Int? {

    if (!isDigit()) return null


    return this.toInt() - '0'.toInt()
}