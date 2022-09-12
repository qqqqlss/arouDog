package com.example.aroundog.Service

import com.example.aroundog.BuildConfig
import com.example.aroundog.Model.CheckSuccess
import com.example.aroundog.Model.Gender
import com.example.aroundog.Model.User
import com.example.aroundog.dto.UserDto
import retrofit2.Call
import retrofit2.http.*

interface IntroService {

    @GET("/login")
    fun login(
        @Query("userId") userId: String,
        @Query("password") password: String
    ): Call<UserDto>

    @FormUrlEncoded
    @POST("/user/register")
    fun signUp(
        @Field("userId") userId:String,
        @Field("password") password:String,
        @Field("image") image:Int,
        @Field("userName") userName:String,
        @Field("phone") phone:String,
        @Field("email") email:String,
        @Field("gender") gender: String,
        @Field("age") age:String
    ):Call<CheckSuccess>;

    @GET("/login/idValidate")
    fun idValidate(
        @Query("userId") userId: String
    ):Call<Boolean> ;
}