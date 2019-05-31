package com.alplabs.filebarcodescanner.extension

import android.widget.TextView
import androidx.annotation.StringRes

/**
 * Created by Alfredo L. Porfirio on 2019-05-31.
 * Copyright Universo Online 2019. All rights reserved.
 */
fun TextView.setTextWithValue(@StringRes id: Int, vararg args: Any) {
    text = context.getString(id, args)
}