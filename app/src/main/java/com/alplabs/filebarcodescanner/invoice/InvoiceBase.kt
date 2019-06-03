package com.alplabs.filebarcodescanner.invoice

import com.alplabs.filebarcodescanner.extension.digitToInt

/**
 * Created by Alfredo L. Porfirio on 2019-06-03.
 * Copyright Universo Online 2019. All rights reserved.
 */
abstract class InvoiceBase : InvoiceInterface {


    private fun createWeights(weights: List<Int>, sizeOutput: Int) : List<Int> {

        val output = mutableListOf<Int>()
        val sizeWeights = weights.size

        for(i in 0 until sizeOutput) {

            output.add(weights[i % sizeWeights])

        }

        return output
    }

    protected fun digit11(block: String) : Int {

        val multiplier = createWeights(listOf(2, 3, 4, 5, 6, 7, 8, 9), block.length)

        var sum = 0

        block.reversed().forEachIndexed { index, c ->

            c.digitToInt()?.let { n ->

                sum += n * multiplier[index]

            }

        }

        val digit = when (val rest = sum % 11) {
            0, 1 -> 0

            10 -> 1

            else -> 11 - rest
        }

        return digit
    }


}