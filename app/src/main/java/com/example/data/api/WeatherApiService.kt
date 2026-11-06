package com.example.data.api

import com.example.data.model.OpenMeteoResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {

  @GET("v1/forecast")
  suspend fun getWeather(
    @Query("latitude") lat: Double,
    @Query("longitude") lon: Double,
    @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,uv_index",
    @Query("hourly") hourly: String = "temperature_2m,uv_index",
    @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,uv_index_max",
    @Query("timezone") timezone: String = "America/Santiago",
    @Query("forecast_days") forecastDays: Int = 3
  ): OpenMeteoResponse

  companion object {
    private const val BASE_URL = "https://api.open-meteo.com/"

    fun create(): WeatherApiService {
      val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

      val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(WeatherApiService::class.java)
    }
  }
}
