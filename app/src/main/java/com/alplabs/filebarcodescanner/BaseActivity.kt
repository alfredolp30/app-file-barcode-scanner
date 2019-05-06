package com.alplabs.filebarcodescanner

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Alfredo L. Porfirio on 26/02/19.
 * Copyright Universo Online 2019. All rights reserved.
 */
@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    companion object {
        private var requestCode: Int = 1

        fun nextRequestCode() = requestCode++
    }


    fun showToast(msg: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, msg, duration).show()
    }


    val appBarcode get() = application as? AppBarcode

}