package com.example.aroundog.Service

import com.example.aroundog.dto.UserCoordinateDogDto
import retrofit2.Call
import retrofit2.http.*

interface CoordinateService {

    //다른 사용자들 위치 불러오기
//    @GET("/users")

    //현재위치 올리기 모든 정보 LocalDateTime 제외
    @FormUrlEncoded
    @POST("/coor/insert")
    fun insert(
        @Field("userId") userId: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("tile") tile: String
    ): Call<Boolean>

    //현재 위치 올리기 latitude, longitude, tile
    @GET("/coor/update")
    fun update(
        @Query("userId") userId: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("tile") tile: String
    ): Call<Boolean>

    @GET("/coor")
    fun getWalkingList(
        @Query("tile") tile: String
    ): Call<List<UserCoordinateDogDto>>

    @GET("/coor/false")
    fun endWalking(@Query("userId") userId: String): Call<Boolean>

}