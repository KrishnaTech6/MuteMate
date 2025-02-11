package com.example.mutemate

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class MuteWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        MuteHelper(context).mutePhone()
        return Result.success()
    }
}
