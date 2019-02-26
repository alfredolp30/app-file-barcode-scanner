package com.alplabs.filebarcodescanner

/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
object ActivityRequestCode {

    private var requestCode: Int = 1


    fun nextRequestCode() = requestCode++

}