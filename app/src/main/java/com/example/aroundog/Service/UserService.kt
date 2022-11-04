package com.example.aroundog.Service

import com.example.aroundog.Model.Gender
import com.example.aroundog.dto.UserDto
import retrofit2.Call
import retrofit2.http.*

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


    @FormUrlEncoded
    @PATCH("/user/{userId}")
    fun updateUser(
        @Path("userId") userId:String,
        @Field("userName") name:String,
        @Field("age") age:Int,
        @Field("email") email:String,
        @Field("phone") phone:String,
        @Field("gender") gender:Gender,
        @Field("image") image:Int
    ):Call<Boolean>

    @GET("/hate/{userId}")
    fun getHateDog(
        @Path("userId") userId: String
    ):Call<String>

    @FormUrlEncoded
    @POST("/hate/{userId}")
    fun updateHateDog(
        @Path("userId") userId: String,
        @Field("hateDog") hateDog:String
    ):Call<Boolean>
}