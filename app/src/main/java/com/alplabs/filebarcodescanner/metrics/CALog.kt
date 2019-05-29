package com.alplabs.filebarcodescanner.metrics

import android.util.Log
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
object CALog {


    private fun logger(level: Int, tag: String, msg: String?, throwable: Throwable?) {
        val message = msg ?: ""

        if (throwable != null) {

            Log.d(tag, message, throwable)

            Fabric.getLogger().log(level, tag, message)
            Fabric.getLogger().e("EXCEPTION", "", throwable)

        } else {

            Log.d(tag, message)

            Fabric.getLogger().d(tag, message)
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
