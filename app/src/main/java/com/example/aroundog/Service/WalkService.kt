package com.example.aroundog.Service

import com.example.aroundog.Model.UpdateWalkHistory
import com.example.aroundog.dto.AllWalkInformationDto
import com.example.aroundog.dto.WalkInfoDto
import com.example.aroundog.dto.WalkListDto
import com.example.aroundog.dto.WalkWeekSummaryDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface WalkService {
    @Multipart
    @POST("/walk/{userId}/add")
    @JvmSuppressWildcards
    fun addWalk(
        @Path("userId", encoded = true) userId: String,
        @PartMap params: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part
    ): Call<Void>

    @GET("/walk/good")
    fun getWalkListOrderedByGood(
        @Query("userId") userId: String,
        @Query("tile") tile: String,
        @Query("start") first: Int,
        @Query("size") last: Int
    ): Call<List<WalkListDto>>

    @FormUrlEncoded
    @POST("/walk/button")
    fun clickButton(
        @Field("walkId") walkId: Long,
        @Field("button") button: String
    ): Call<Void>

    @FormUrlEncoded
    @POST("/walk/button")
    fun clickButton(
        @Field("userId") userId: String,
        @Field("walkId") walkId: Long,
        @Field("button") button: String
    ): Call<Void>

    @GET("/walk/week")
    fun getWalkWeekSummary(
        @Query("userId") userId: String
    ): Call<WalkWeekSummaryDto>

    @GET("/walk/{userId}/allInfo")
    fun getAllWalkInfo(
        @Path("userId") userId: String
    ): Call<AllWalkInformationDto>

    @GET("/walk/{walkId}/info")
    fun getWalkInfo(
        @Path("walkId") walkId: Long
    ):Call<WalkInfoDto>
}