package com.alplabs.filebarcodescanner.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alplabs.filebarcodescanner.BaseActivity
import com.alplabs.filebarcodescanner.metrics.CALog

/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
open class BaseFragment : Fragment() {

    protected val baseActivity get() = activity as? BaseActivity
    protected val appBarcode get() =  baseActivity?.appBarcode

}