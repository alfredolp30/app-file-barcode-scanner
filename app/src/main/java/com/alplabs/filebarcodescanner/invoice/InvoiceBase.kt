package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.digitToInt

/**
 * Created by Alfredo L. Porfirio on 2019-06-03.
 * Copyright Universo Online 2019. All rights reserved.
 */
abstract class InvoiceBase : InvoiceInterface {


    protected fun createWeights(weights: List<Int>, sizeOutput: Int) : List<Int> {

        val output = mutableListOf<Int>()
        val sizeWeights = weights.size

        for(i in 0 until sizeOutput) {

            output.add(weights[i % sizeWeights])

        }

        return output
    }

    abstract fun digit11(block: String) : Int

}