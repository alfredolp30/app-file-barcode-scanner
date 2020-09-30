package com.alplabs.filebarcodescanner.metrics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
object CALog {


    private fun logger(level: Int, tag: String, msg: String?, throwable: Throwable?) {
        val message = msg ?: ""


        Log.println(level, tag, message)
        FirebaseCrashlytics.getInstance().log("$tag : $msg")

        if (throwable != null) {
            Log.d("", throwable.message ?: "")
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    fun d(tag: String, msg: String?, throwable: Throwable? = null) {
        logger(Log.DEBUG, tag, msg, throwable)
    }


    fun i(tag: String, msg: String?, throwable: Throwable? = null) {
        logger(Log.INFO, tag, msg, throwable)
    }

    fun w(tag: String, msg: String?, throwable: Throwable? = null) {
        logger(Log.WARN, tag, msg, throwable)
    }

    fun e(tag: String, msg: String?, throwable: Throwable? = null) {
        logger(Log.ERROR, tag, msg, throwable)
    }

}
