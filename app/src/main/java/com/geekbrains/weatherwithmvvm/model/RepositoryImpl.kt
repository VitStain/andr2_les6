package com.geekbrains.weatherwithmvvm.model

import com.geekbrains.weatherwithmvvm.model.entities.Weather
import com.geekbrains.weatherwithmvvm.model.entities.getRussianCities
import com.geekbrains.weatherwithmvvm.model.entities.getWorldCities

class RepositoryImpl : Repository {
    override fun getWeatherFromServer(lat: Double, lng: Double): Weather {
        val dto = WeatherLoader.loadWeather(lat, lng)
        return Weather(
            temperature = dto?.fact?.temp,
            feelsLike = dto?.fact?.feels_like,
            condition = dto?.fact?.condition
        )
    }

    override fun getWeatherFromLocalStorageRus() = getRussianCities()
    override fun getWeatherFromLocalStorageWorld() = getWorldCities()
}