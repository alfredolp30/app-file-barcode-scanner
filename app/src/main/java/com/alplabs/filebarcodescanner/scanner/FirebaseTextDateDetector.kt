package com.alplabs.filebarcodescanner.scanner


import com.alplabs.filebarcodescanner.metrics.CALog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-10-18.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseTextDateDetector {
    private val detector = TextRecognition.getClient()

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

    fun scanner(image: InputImage, callback: (GregorianCalendar?, textBlock: Text.TextBlock?) -> Unit) {

        try {
            detector.process(image)
                .addOnSuccessListener { firebaseText ->

                    firebaseText.textBlocks.forEach {

                        val dates = findDates(it.text)

                        if (dates.isNotEmpty()) {
                            val date = dates.maxOrNull()
                            callback.invoke(date, it)

                        } else {
                            callback.invoke(null, null)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    CALog.e("FirebaseTextDateDetector::scanner", e.message, e)
                    callback.invoke(null, null)
                }


        } catch (th: Throwable) {
            CALog.e("FirebaseTextDateDetector::scanner", th.message, th)
            callback.invoke(null, null)
        }

    }


    private fun findDates(text: String) : List<GregorianCalendar> {

        val result = dateRegex.find(text)

        return result?.groupValues?.map {


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
    }


}