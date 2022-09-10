package com.example.aroundog.Service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapService {

    @GET("/map")
    fun getCoordinate(@Query("latitude") latitude:Long, @Query("longitude") longitude:Long): Call<Void>

}