package com.example.mutemate

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("UnmuteWorker", "Phone unmuted")
        MuteHelper(context).unmutePhone()
        return Result.success()
    }
}