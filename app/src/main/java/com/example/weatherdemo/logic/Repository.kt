package com.example.weatherdemo.logic

import androidx.lifecycle.liveData
import com.example.weatherdemo.logic.dao.PlaceDao
import com.example.weatherdemo.logic.model.Place
import com.example.weatherdemo.logic.model.Weather
import com.example.weatherdemo.logic.network.WeatherDemoNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {


    fun savePlace(place: Place)=PlaceDao.savePlace(place)
    fun getSavedPlace()=PlaceDao.getSavedPlaces()
    fun isPlaceSaved()=PlaceDao.isPlaceSaved()


    fun searchPlaces(query: String) = fire(Dispatchers.IO) {


        val placeResponse = WeatherDemoNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }


    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {

        coroutineScope {
            val deferredRealtime = async {
                WeatherDemoNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                WeatherDemoNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }

    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}