package com.example.aroundog.Service;

import com.example.aroundog.BuildConfig;
import com.example.aroundog.Model.CheckSuccess;
import com.example.aroundog.Model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IntroService {

    String INTRO_URL = BuildConfig.SERVER; // 라즈베리파이 서버

    // 라즈베리파이 서버
    @FormUrlEncoded
    @POST("/intro/register.php")
    Call<CheckSuccess> signUp(
            @Field("id") String id,
            @Field("password") String password,
            @Field("image") int image,
            @Field("name") String name,
            @Field("phone") String phone,
            @Field("email") String email
    );

    @GET("/intro/login.php")
    Call<User> login(
            @Query("id") String id,
            @Query("password") String password
    );

    @GET("/intro/idValidate.php")
    Call<CheckSuccess> idValidate(
            @Query("id") String id
    );
}
