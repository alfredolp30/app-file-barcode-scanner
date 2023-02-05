package com.alplabs.filebarcodescanner.metrics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
class CAAnalytics(context: Context) {

    private val analytics = FirebaseAnalytics.getInstance(context)

    fun eventSelectContent(name: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }

        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun eventInvoiceFinished(founded: Boolean) {
        val invoiceFinished = "invoice_finished_" + if(founded) "founded" else "not_founded"

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SUCCESS, founded.toString())
        }

        analytics.logEvent(invoiceFinished, bundle)
    }


    fun eventTestFinished(founded: Boolean, value: String?) {
        val invoiceFinished = "barcode_text_" + if(founded) "founded" else "not_founded"

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, value)
        }

        analytics.logEvent(invoiceFinished, bundle)
    }

}
