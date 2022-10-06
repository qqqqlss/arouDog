package com.example.aroundog.Service

import com.example.aroundog.dto.UserDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    @POST("/user")
    fun join(@Body user:UserDto): Call<Long>

    @GET("/user/findId")
    fun findId(
        @Query("userName") userName: String,
        @Query("userEmail") userEmail: String
    ): Call<Boolean>

    @GET("/user/findPw")
    fun findPw(
        @Query("userId") userId:String,
        @Query("userName") userName: String,
        @Query("userEmail") userEmail: String
    ): Call<Boolean>


}