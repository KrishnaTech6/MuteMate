package com.example.mutemate

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UnmuteWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        MuteHelper(context).unmutePhone()
        return Result.success()
    }
}