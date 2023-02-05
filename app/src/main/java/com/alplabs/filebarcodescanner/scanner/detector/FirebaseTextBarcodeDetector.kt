package com.alplabs.filebarcodescanner.scanner.detector


import com.alplabs.filebarcodescanner.metrics.CALog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Created by Alfredo L. Porfirio on 2019-10-18.
 * Copyright Universo Online 2019. All rights reserved.
 */
class FirebaseTextBarcodeDetector {
    private val detector = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    private val patterns = arrayOf(
        "\\d{12}(\\r\\n|\\r|\\n| )\\d{12}(\\r\\n|\\r|\\n| )\\d{12}(\\r\\n|\\r|\\n| )\\d{12}", // collection
        "\\d{5}\\.\\d{5} \\d{5}\\.\\d{6} \\d{5}\\.\\d{6} \\d \\d{14}" // bank
    )


    private val regexes by lazy {
        patterns.map {
            Regex(
                pattern = it,
                options =  setOf(
                    RegexOption.MULTILINE,
                    RegexOption.DOT_MATCHES_ALL
                )
            )
        }
    }

    fun scanner(image: InputImage, callback: (possibleBarcode: String?, ) -> Unit) {

        try {
            detector.process(image)
                .addOnSuccessListener { firebaseText ->
                    val possibleBarcode = findBarcode(firebaseText.text)
                    callback.invoke(possibleBarcode)
                }
                .addOnFailureListener { e ->
                    CALog.e("FirebaseTextBarcodeDetector::scanner", e.message, e)
                    callback.invoke(null)
                }


        } catch (th: Throwable) {
            CALog.e("FirebaseTextBarcodeDetector::scanner", th.message, th)
            callback.invoke(null)
        }

    }


    private fun findBarcode(text: String) : String? {
        var result: String? = null

        for (regex in regexes) {
            result = regex.find(text)?.groupValues?.first()

            if (result != null) {
                break
            }
        }

        return result
    }


}