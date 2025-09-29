package com.example.mnotepad.workers

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mnotepad.database.PasswordStorage.getUnlockTime
import java.util.concurrent.TimeUnit

class PasswordWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {

    override fun doWork(): Result {
        Log.e("Worker", "Do work")
        val period = getUnlockTime(applicationContext)

        val request = OneTimeWorkRequestBuilder<PasswordWorker>()
            .setInitialDelay(period?.toLong() ?: 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "PasswordWorker",
            ExistingWorkPolicy.REPLACE,
            request
        )

        return Result.success()
    }
}
