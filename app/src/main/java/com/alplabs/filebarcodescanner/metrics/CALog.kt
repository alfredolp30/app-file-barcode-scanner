package com.alplabs.filebarcodescanner.metrics

import android.util.Log
import com.crashlytics.android.Crashlytics

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
object CALog {

    fun d(tag: String, msg: String?, throwable: Throwable? = null) {

        Crashlytics.log(Log.DEBUG, tag, msg)

        if (throwable != null) {

            Log.d(tag, msg, throwable)

            Crashlytics.logException(throwable)

        } else {

            Log.d(tag, msg)

        }

    }


    fun i(tag: String, msg: String?, throwable: Throwable? = null) {

        Crashlytics.log(Log.INFO, tag, msg)

        if (throwable != null) {

            Log.i(tag, msg, throwable)

            Crashlytics.logException(throwable)

        } else {

            Log.i(tag, msg)

        }

    }


    fun e(tag: String, msg: String?, throwable: Throwable? = null) {

        Crashlytics.log(Log.ERROR, tag, msg)

        if (throwable != null) {

            Log.e(tag, msg, throwable)

            Crashlytics.logException(throwable)

        } else {

            Log.e(tag, msg)

        }


    }

}
