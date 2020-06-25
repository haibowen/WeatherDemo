package com.example.weatherdemo.logic

import androidx.lifecycle.liveData
import com.example.weatherdemo.logic.model.Place
import com.example.weatherdemo.logic.model.Weather
import com.example.weatherdemo.logic.network.WeatherDemoNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import java.lang.Exception
import java.lang.RuntimeException

object Repository {
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {

        val result = try {

            val placeResponse = WeatherDemoNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }

        emit(result as Result<List<Place>>)


    }

    fun refreshWeather(lng:String,lat:String)= liveData(Dispatchers.IO) {
        val result=try {
            coroutineScope {
                val deferredRealtime=async {
                    WeatherDemoNetwork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily=async {
                    WeatherDemoNetwork.getDailyWeather(lng,lat)
                }
                val realtimeResponse=deferredRealtime.await()
                val dailyResponse=deferredDaily.await()
                if (realtimeResponse.status=="ok"&&dailyResponse.status=="ok"){
                    val  weather=Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}"+"daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        }catch (e:Exception){
            Result.failure<Weather>(e)
        }
        emit(result as Result<Weather>)
    }


}