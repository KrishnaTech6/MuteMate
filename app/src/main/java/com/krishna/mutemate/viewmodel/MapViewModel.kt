package com.krishna.mutemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishna.mutemate.model.LocationMute
import com.krishna.mutemate.room.LocationMuteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val locationMuteDao: LocationMuteDao): ViewModel() {
    val allLocationMute: Flow<List<LocationMute>> = locationMuteDao.getAllLocationMutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertLocationMute(locationMute: LocationMute) {
        viewModelScope.launch(Dispatchers.IO){
            try {
                locationMuteDao.insertLocationMute(locationMute)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun deleteLocationMute(locationMute: LocationMute) {
        viewModelScope.launch(Dispatchers.IO){
            try {
                locationMuteDao.deleteLocationMute(locationMute)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

}