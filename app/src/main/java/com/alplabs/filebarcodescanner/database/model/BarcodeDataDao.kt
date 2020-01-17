package com.alplabs.filebarcodescanner.database.model

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
@Dao
interface BarcodeDataDao {
    @Query("SELECT * FROM BarcodeData")
    fun list() : List<BarcodeData>

    @Insert(onConflict = REPLACE)
    fun add(barcodeData: BarcodeData): Long?

    @Delete
    fun remove(barcodeData: BarcodeData)

    @Update
    fun replace(barcodeData: BarcodeData)
}