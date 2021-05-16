package com.geekbrains.weatherwithmvvm.framework.ui.details

import androidx.lifecycle.*
import com.geekbrains.weatherwithmvvm.model.AppState
import com.geekbrains.weatherwithmvvm.model.Repository
import com.geekbrains.weatherwithmvvm.model.RepositoryImpl

class DetailsViewModel : ViewModel(), LifecycleObserver {
    private val repository: Repository = RepositoryImpl()
    val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()

    fun loadData(lat: Double, lng: Double) {
        liveDataToObserve.value = AppState.Loading
        Thread {
            val data = repository.getWeatherFromServer(lat, lng)
            liveDataToObserve.postValue(AppState.Success(listOf(data)))
        }.start()
    }
}