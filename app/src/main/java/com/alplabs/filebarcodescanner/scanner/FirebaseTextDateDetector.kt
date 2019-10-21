package com.alplabs.filebarcodescanner.scanner

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.alplabs.filebarcodescanner.metrics.CALog
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.lang.Exception
import java.lang.ref.WeakReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-10-18.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseTextDateDetector {
    private val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

    private val LOCALE = Locale("pt", "BR")

    private val dateFormat by lazy { SimpleDateFormat("dd/MM/yyyy", LOCALE) }
    private val largeDateFormat by lazy { SimpleDateFormat("dd MMM yyyy", LOCALE) }

    private val dateRegex by lazy {
        Regex(
            pattern = "(([0-9]{2}\\/[0-9]{2}\\/[0-9]{4})|([0-9]{2} [A-Z]{3} [0-9]{4}))",
            options =  setOf(
                RegexOption.MULTILINE,
                RegexOption.DOT_MATCHES_ALL
            ))
    }

    fun scanner(image: FirebaseVisionImage, callback: (GregorianCalendar?) -> Unit) {

        try {
            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->

                    val result = dateRegex.find(firebaseVisionText.text)

                    val dates = result?.groupValues?.map {


                        val calendar = GregorianCalendar()

                        calendar.apply {
                            try {
                                val date = if (it.contains(" ")) {
                                    largeDateFormat.parse(it)
                                } else {
                                    dateFormat.parse(it)
                                }

                                date?.let {
                                    time = date
                                }

                            } catch (ex: ParseException) {
                                CALog.e("FirebaseTextDateDetector::scanner", ex.message, ex)
                                return@map null
                            }
                        }

                        calendar
                    }?.filterNotNull() ?: listOf()

                    if (dates.isNotEmpty()) {
                        val date = dates.max()!!
                        callback.invoke(date)

                    } else {
                        callback.invoke(null)
                    }

                }
                .addOnFailureListener { e ->
                    CALog.e("FirebaseTextDateDetector::scanner", e.message, e)
                    callback.invoke(null)
                }


        } catch (th: Throwable) {
            CALog.e("FirebaseTextDateDetector::scanner", th.message, th)
            callback.invoke(null)
        }

    }


}