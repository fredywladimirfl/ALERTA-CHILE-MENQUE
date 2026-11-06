package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SeismicApiService {

  @GET("fdsnws/event/1/query")
  suspend fun getChileEarthquakes(
    @Query("format") format: String = "geojson",
    @Query("minmagnitude") minMag: Double = 2.5,
    @Query("minlatitude") minLat: Double = -56.0,
    @Query("maxlatitude") maxLat: Double = -17.5,
    @Query("minlongitude") minLon: Double = -76.0,
    @Query("maxlongitude") maxLon: Double = -66.0,
    @Query("limit") limit: Int = 35,
    @Query("orderby") orderby: String = "time"
  ): UsgsFeatureCollection

  companion object {
    private const val BASE_URL = "https://earthquake.usgs.gov/"

    fun create(): SeismicApiService {
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
        .create(SeismicApiService::class.java)
    }
  }
}
