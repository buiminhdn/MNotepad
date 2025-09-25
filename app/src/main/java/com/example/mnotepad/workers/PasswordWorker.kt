package com.example.mnotepad.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mnotepad.activities.LockActivity
import com.example.mnotepad.database.PasswordStorage.setIsLocked

class PasswordWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {

    override fun doWork(): Result {
        Log.e("heehe", "runb di ma")
        setIsLocked(applicationContext, false)
        return Result.success();
    }
}