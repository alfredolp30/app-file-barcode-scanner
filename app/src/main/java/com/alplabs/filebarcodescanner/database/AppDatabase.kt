package com.alplabs.filebarcodescanner.database

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alplabs.filebarcodescanner.database.model.BarcodeData
import com.alplabs.filebarcodescanner.database.model.BarcodeDataDao

/**
 * Created by Alfredo L. Porfirio on 2020-01-02.
 * Copyright Universo Online 2020. All rights reserved.
 */
@Database(entities = [BarcodeData::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun barcodeDataDao(): BarcodeDataDao
}