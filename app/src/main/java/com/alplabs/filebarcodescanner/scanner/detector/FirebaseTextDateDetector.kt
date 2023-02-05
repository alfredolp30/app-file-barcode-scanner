package com.alplabs.filebarcodescanner.scanner.detector


import com.alplabs.filebarcodescanner.metrics.CALog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alfredo L. Porfirio on 2019-10-18.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseTextDateDetector {
    private val detector = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    companion object {
        private val LOCALE = Locale("pt", "BR")
    }

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

                    val dates = mutableMapOf<Text.TextBlock, GregorianCalendar?>()

                    firebaseText.textBlocks.forEach {
                        dates[it] = findDate(it.text)
                    }

                    val date = dates.maxByOrNull { it.value?.timeInMillis ?: 0 }

                    if (date?.value != null) {
                        callback.invoke(date.value, date.key)
                    } else {
                        callback.invoke(null, null)
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


    private fun findDate(text: String) : GregorianCalendar? {

        val result = dateRegex.find(text)

       result?.groupValues?.forEach {
           var date: Date? = null

           try { date = largeDateFormat.parse(it) } catch (e: Exception) {}

           if (date == null) {
               try { date = dateFormat.parse(it) } catch (e: Exception) {}
           }

           date?.let {
               val calendar = GregorianCalendar().apply {
                   time = date
               }

               return calendar
           }
        }

        return null
    }


}