package com.alplabs.filebarcodescanner.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
object DatabaseManager {

    private const val DATABASE_NAME = "database.db"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE BarcodeData ADD COLUMN title TEXT NOT NULL DEFAULT('')")
        }
    }

    private val MIGRATION_2_3 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE BarcodeData ADD COLUMN path TEXT NOT NULL DEFAULT('')")
        }
    }

    fun createDatabase(context: Context) : AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
        ).addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3
        ).build()
    }


    fun <T>executeInBackground(execution: () -> T, resultCallback: ((T?) -> Unit)? = null) {
        AsyncTaskLoad(
            execution,
            resultCallback
        ).execute()
    }

}

class AsyncTaskLoad<T>(

    private val execution: () -> T,
    private val resultCallback: ((T) -> Unit)?

): AsyncTask<Unit, Unit, T>() {

    override fun doInBackground(vararg p0: Unit?): T {
        return execution.invoke()
    }

    override fun onPostExecute(result: T) {
        super.onPostExecute(result)
        resultCallback?.invoke(result)
    }
}