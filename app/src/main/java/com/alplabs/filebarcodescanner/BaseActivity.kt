package com.alplabs.filebarcodescanner

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.eventbus.NewBarcodeData
import com.alplabs.filebarcodescanner.model.BarcodeModel
import org.greenrobot.eventbus.EventBus

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


    protected fun saveBarcodeAndShow(barcodeModels: List<BarcodeModel>) {
        val database = appBarcode?.database ?: return

        val barcodeData = barcodeModels.first().barcodeData

        DatabaseManager.executeInBackground(
            execution = {
                database.barcodeDataDao().add(barcodeData)
            },

            resultCallback = {

                EventBus.getDefault().post(NewBarcodeData(barcodeData))

                Intent(this, BarcodeActivity::class.java).also {
                    startActivity(it)
                }

            }
        )

    }

}