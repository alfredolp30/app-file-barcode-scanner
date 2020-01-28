package com.alplabs.filebarcodescanner.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import java.lang.ref.WeakReference

/**
 * Created by Alfredo L. Porfirio on 2020-01-13.
 * Copyright Universo Online 2020. All rights reserved.
 */
object DatabaseManager {

    private const val DATABASE_NAME = "database.db"

    fun createDatabase(context: Context) : AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
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