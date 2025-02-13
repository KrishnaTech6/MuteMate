package com.example.mutemate

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("MuteWorker", "Phone muted")
        MuteHelper(context).mutePhone()
        return Result.success()
    }
}
