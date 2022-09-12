@file:Suppress("IfThenToSafeAccess")

package com.example.aroundog

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.aroundog.Model.User
import com.example.aroundog.Service.IntroService
import com.example.aroundog.dto.UserDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity2 : AppCompatActivity(){
    private var backPressedTime : Long = 0
    private var loadingDialog: LoadingDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val login_register: Button? = findViewById(R.id.login_register)
        val login_find: Button? = findViewById(R.id.login_find)
        val login_button: Button? = findViewById(R.id.login_button)
        val login_id: TextView? = findViewById(R.id.login_id)
        val login_pw: TextView? = findViewById(R.id.login_pw)
        val login_id_check: TextView? = findViewById(R.id.login_id_check)
        val login_pw_check: TextView? = findViewById(R.id.login_pw_check)
        val login_check: TextView? = findViewById(R.id.login_check)
        val login_stay_cb: CheckBox? = findViewById(R.id.login_stay_cb)
        lateinit var user_info_pref: SharedPreferences
        lateinit var user_info_editor: SharedPreferences.Editor


        // 회원가입 버튼
        if (login_register != null) {
            login_register.setOnClickListener {
                val intent = Intent(this@MainActivity2, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        // 아이디, 비밀번호 찾기 버튼
        if (login_find != null) {
            login_find.setOnClickListener {
                val intent = Intent(this@MainActivity2,FindActivity::class.java)
                startActivity(intent)
            }
        }

        // 로그인 버튼

        // 로그인 버튼
        if (login_button != null) {
            login_button.setOnClickListener {
                user_info_pref = getSharedPreferences("userInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
                user_info_editor = user_info_pref.edit()
                val id = login_id?.text.toString()
                val pw = login_pw?.text.toString()
                if (login_id_check != null) {
                    login_id_check.visibility = View.GONE
                }
                if (login_pw_check != null) {
                    login_pw_check.visibility = View.GONE
                }
                if (login_check != null) {
                    login_check.visibility = View.GONE
                }
                if (id == "" && pw == "") { // 아이디, 비밀번호 둘 다 빈칸일 때
                    if (login_id_check != null) {
                        login_id_check.visibility = View.VISIBLE
                    }
                } else if (id == "") { // 아이디만 빈칸일 때
                    if (login_id_check != null) {
                        login_id_check.visibility = View.VISIBLE
                    }
                } else if (pw == "") { // 비밀번호만 빈칸일 때
                    if (login_pw_check != null) {
                        login_pw_check.visibility = View.VISIBLE
                    }
                } else {
                    loadingDialog?.show()

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

                    introAPI.login(id, pw).enqueue(object : Callback<UserDto> {
                        override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
                            loadingDialog?.dismiss()
                            if (response.isSuccessful) { // 성공적으로 받아왔을 때
                                if (response.body()!!.isSuccess) { // 아이디, 비밀번호가 일치했을 때
                                    val userdata = response.body() // 유저 데이터를 호출한 데이터베이스로부터 받아와 변수에 저장
                                    if (userdata != null) {
                                        user_info_editor.putString("id", userdata.userId)
                                        user_info_editor.putString("password", userdata.password)
                                        user_info_editor.putString("name", userdata.userName)
                                        user_info_editor.putString("phone", userdata.phone)
                                        user_info_editor.putString("email", userdata.email)
                                        user_info_editor.putString("email", userdata.email)
                                        user_info_editor.putInt("age", userdata.age)
                                        user_info_editor.putString("gender",
                                            userdata.gender.toString()
                                        )
                                    }
                                    user_info_editor.commit() // 세션 영역에 해당 유저의 정보를 넣음
                                    if (login_stay_cb != null) {
                                        if (login_stay_cb.isChecked) { // 로그인 상태 유지가 체크되어 있다면
                                            user_info_editor.putBoolean(
                                                "autoLogin",
                                                true
                                            ) // 자동 로그인 여부를 true
                                            user_info_editor.commit()
                                            val intent =
                                                Intent(this@MainActivity2, MainActivty::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else { // 로그인 상태 유지 체크가 안되어 있다면
                                            user_info_editor.putBoolean(
                                                "autoLogin",
                                                false
                                            ) // 자동 로그인 여부를 false
                                            user_info_editor.commit()
                                            val intent =
                                                Intent(this@MainActivity2, MainActivty::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                } else { // 아이디, 비밀번호가 일치하지 않을 때
                                    if (login_check != null) {
                                        login_check.visibility = View.VISIBLE
                                    } // 다시 확인하라는 텍스트뷰 출력
                                }
                            }
                        }

                        override fun onFailure(call: Call<UserDto>, t: Throwable) {
                            loadingDialog?.dismiss()
                            Toast.makeText(applicationContext, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                                .show()
                            t.printStackTrace()
                        }
                    })
                }
            }
        }
    }

    override fun onBackPressed() {

        // 2초내 다시 클릭하면 앱 종료
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
            return
        }
        // 처음 클릭 메시지
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }
}
