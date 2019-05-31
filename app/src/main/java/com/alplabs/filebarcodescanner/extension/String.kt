package com.alplabs.filebarcodescanner.extension

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}


fun String.addSubstring(startIndex: Int, substring: String) : String {

    if (startIndex in 0..length) {
        return substring(0, startIndex) + substring + substring(startIndex)
    }

    return this
}

