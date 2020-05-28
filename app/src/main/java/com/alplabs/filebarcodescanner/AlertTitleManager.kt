package com.alplabs.filebarcodescanner

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 2020-03-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
class AlertTitleManager {

    fun createAlert(context: Context, title: String, callback: (title: String) -> Unit) {

        val alert = AlertDialog.Builder(context)
        alert.setTitle(context.getString(R.string.invoice_alert_title))

        val view = LayoutInflater.from(context).inflate(R.layout.alert_title, null, false)

        val edtTextTitle = view.findViewById<EditText>(R.id.edtTextTitle)
        edtTextTitle.setText(title)

        alert.setView(view)

        alert.setPositiveButton(R.string.confirm) { _, _ ->
            edtTextTitle.clearComposingText()

            val newTitle = edtTextTitle.text.toString().trim()
            callback(newTitle)

        }

        alert.setNegativeButton(context.getString(R.string.cancel), null)

        alert.show()

    }

}