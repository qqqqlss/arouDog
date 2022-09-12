package com.example.aroundog.Service

import com.example.aroundog.dto.UserDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {

    @POST("/user")
    fun join(@Body user:UserDto): Call<Long>
}