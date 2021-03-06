package com.alplabs.filebarcodescanner

import android.app.Application
import com.alplabs.filebarcodescanner.database.AppDatabase
import com.alplabs.filebarcodescanner.database.DatabaseManager
import com.alplabs.filebarcodescanner.metrics.CAAnalytics
import com.alplabs.filebarcodescanner.metrics.CALog
/**
 * Created by Alfredo L. Porfirio on 2019-05-06.
 * Copyright Universo Online 2019. All rights reserved.
 */
class AppBarcode : Application() {

    override fun onCreate() {
        super.onCreate()

        CALog.d(AppBarcode::onCreate.name, "init application")
    }

    val analytics: CAAnalytics by lazy { CAAnalytics(applicationContext) }
    val database: AppDatabase by lazy { DatabaseManager.createDatabase(applicationContext) }
}
