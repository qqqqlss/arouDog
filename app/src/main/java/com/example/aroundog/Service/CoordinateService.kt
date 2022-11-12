package com.example.aroundog.Service

import com.example.aroundog.dto.UserCoordinateDogDto
import retrofit2.Call
import retrofit2.http.*

interface CoordinateService {

    //다른 사용자들 위치 불러오기
//    @GET("/users")

    //현재 위치 올리기 latitude, longitude, tile
    @GET("/coor/update")
    fun update(
        @Query("dogIdList") dogIdList: List<Long>,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("tile") tile: String
    ): Call<Boolean>

    @GET("/coor")
    fun getWalkingList(
        @Query("tile") tile: String
    ): Call<List<UserCoordinateDogDto>>

    @POST("/coor/false")
    fun endWalking(@Query("dogIdList") dogIdList: List<Long>): Call<Boolean>

}