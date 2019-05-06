package com.alplabs.filebarcodescanner.metrics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
class CAAnalytics(context: Context) {

    private val fa = FirebaseAnalytics.getInstance(context)


    fun eventSelectContent(name: String) {

        Bundle().apply {
            this.putString(FirebaseAnalytics.Param.ITEM_NAME, name)

            fa.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, this)

        }

    }

}
