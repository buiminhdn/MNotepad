package com.example.mnotepad.workers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log



class LogBackgroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                Log.e("Service", "Service is running...")
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}