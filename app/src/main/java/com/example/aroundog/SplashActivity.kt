package com.example.aroundog

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.aroundog.Service.IntroService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.UserAndDogDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SplashActivity : Activity(){
    lateinit var user_info_editor:SharedPreferences.Editor
    lateinit var dog_info_editor: SharedPreferences.Editor
    var loginSuccess = false
    var endRetrofit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        //스플래시
        installSplashScreen()
        super.onCreate(savedInstanceState)

        //sharedPreferences
        val user_info_pref = getSharedPreferences("userInfo", MODE_PRIVATE)
        val dog_info_pref =
            getSharedPreferences("dogInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
        user_info_editor = user_info_pref.edit()
        dog_info_editor = dog_info_pref.edit()

        val autoLogin = user_info_pref.getBoolean("autoLogin", false)

        //자동 로그인
        if (autoLogin == false) {
            // 자동 로그인 하지 않았을 경우 로그인으로 이동하며 세션 영역 내 정보 삭제
            user_info_editor.clear()
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            var id = user_info_pref.getString("id", "")!!
            var password = user_info_pref.getString("password", "")!!
            login(id, password)
        }

        //레트로핏 결과값을 기다리기 위한 리스너 등록
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (endRetrofit) {
                        if (loginSuccess) {
                            //자동 로그인 성공
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            //자동 로그인 실패
                            user_info_editor.clear()
                            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            intent.putExtra("autoLoginFail", true)
                            startActivity(intent)
                            finish()
                        }
                        //이게 if문에만 있으면 스플래시가 끝나지 않아서 액티비티가 또 호출됨
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        return true
                    }
                    return false
                }
            }
        )
    }
    private fun login(
        id: String,
        pw: String
    ) {
        //로그를 보기 위한 Interceptor
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            //.connectTimeout(100, TimeUnit.SECONDS)
            //.readTimeout(100,TimeUnit.SECONDS)
            //.writeTimeout(100, TimeUnit.SECONDS)
            .build()
        // 데이터베이스 접속 및 확인
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)     //로그 기능 추가
            .build()

        val introAPI = retrofit.create(IntroService::class.java)

        introAPI.login(id, pw).enqueue(object : Callback<List<UserAndDogDto>> {
            override fun onResponse(
                call: Call<List<UserAndDogDto>>,
                response: Response<List<UserAndDogDto>>
            ) {
                if (response.isSuccessful) { // 성공적으로 받아왔을 때
                    if (response.body()!![0].isSuccess()) { // 아이디, 비밀번호가 일치했을 때
                        val userAndDogList = response.body() // 유저 데이터를 호출한 데이터베이스로부터 받아와 변수에 저장

                        //유저 정보 저장
                        user_info_editor.putString("id", userAndDogList!![0].userId)
                        user_info_editor.putString("password", userAndDogList!![0].password)
                        user_info_editor.putInt("userAge", userAndDogList!![0].userAge)
                        user_info_editor.putInt("image", userAndDogList!![0].image)
                        user_info_editor.putString("userName", userAndDogList!![0].userName)
                        user_info_editor.putString("phone", userAndDogList!![0].phone)
                        user_info_editor.putString("email", userAndDogList!![0].email)
                        user_info_editor.putString(
                            "userGender",
                            userAndDogList!![0].userGender.toString()
                        )
                        user_info_editor.commit() // 세션 영역에 해당 유저의 정보를 넣음

                        if (userAndDogList!![0].dogId != 0L) {//등록된 강아지가 있는지 확인
                            var dogList = mutableListOf<DogDto>()
                            //강아지 정보 리스트로 저장
                            userAndDogList!!.forEach { userAndDogDto ->
                                var dog = DogDto(
                                    userAndDogDto.dogId,
                                    userAndDogDto.dogName,
                                    userAndDogDto.dogAge,
                                    userAndDogDto.dogWeight,
                                    userAndDogDto.dogHeight,
                                    userAndDogDto.dogGender,
                                    userAndDogDto.breed,
                                    userAndDogDto.dogImgList
                                )
                                dogList.add(dog)
                            }
                            //Json 으로 만들기 위한 Gson
                            var makeGson = GsonBuilder().create()
                            var type: TypeToken<MutableList<DogDto>> =
                                object : TypeToken<MutableList<DogDto>>() {}
                            var dogStr = makeGson.toJson(dogList, type.type)

                            dog_info_editor.putBoolean("hasDog", true)
                            dog_info_editor.putString("dogList", dogStr)
                        } else {//강아지 없을때
                            dog_info_editor.putBoolean("hasDog", false)
                        }
                        dog_info_editor.commit()
                        loginSuccess = true
                    }
                }
                endRetrofit = true
            }

            override fun onFailure(call: Call<List<UserAndDogDto>>, t: Throwable) {
                Toast.makeText(applicationContext, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                    .show()
                t.printStackTrace()
                endRetrofit = true
            }
        })
    }
}