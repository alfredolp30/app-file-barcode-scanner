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


    override fun digit10(block: String): Int {
        val multiplier = createWeights(listOf(2, 1), block.length)

        var sum = 0

        block.reversed().forEachIndexed { index, c ->

            c.digitToInt()?.let { n ->

                var multi = (n * multiplier[index])

                while (multi > 9) {
                    var sumInternal = 0

                    multi.toString().forEach { nInternal ->
                        sumInternal += nInternal.digitToInt()!!
                    }

                    multi = sumInternal
                }

                sum += multi

            }
        }

        val digit = (10 - (sum % 10)) % 10

        return digit
    }

}