package com.krishna.mutemate.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.krishna.mutemate.room.MuteScheduleDao

class MuteViewModelFactory(
    private val dao: MuteScheduleDao,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(MuteScheduleDao::class.java , Application::class.java)
            .newInstance(dao, application)
    }
}
