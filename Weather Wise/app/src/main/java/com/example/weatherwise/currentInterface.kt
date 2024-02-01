package com.example.weatherwise

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface currentInterface {
    @GET("weather")
    fun getCurrentLocationWeather(
       @Query("lat") latitude:Double,
       @Query("lon") longitude:Double,
        @Query("appid") appid : String
    ): Call<currentweather>
}