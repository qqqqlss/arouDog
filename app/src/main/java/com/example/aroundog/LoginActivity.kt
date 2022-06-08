package com.example.aroundog
/*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aroundog.Model.User
import com.example.aroundog.Service.IntroService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginActivity : AppCompatActivity() {
    private var login_register: Button = findViewById(R.id.login_register)
    private var login_find: Button = findViewById(R.id.login_find)
    private var login_button: Button = findViewById(R.id.login_button)
    private var login_id: TextView = findViewById(R.id.login_id)
    private var login_pw: TextView = findViewById(R.id.login_pw)
    private var login_id_check: TextView = findViewById(R.id.login_id_check)
    private var login_pw_check: TextView = findViewById(R.id.login_pw_check)
    private var login_check: TextView = findViewById(R.id.login_check)
    private var login_stay_cb: CheckBox = findViewById(R.id.login_stay_cb)
    private lateinit var user_info_pref: SharedPreferences
    private lateinit var user_info_editor: SharedPreferences.Editor
    //private lateinit var loadingDialog: LoadingDialog
    private var backKeyPressedTime = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 회원가입 버튼
        login_register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 아이디, 비밀번호 찾기 버튼
        login_find.setOnClickListener {
            val intent = Intent(this@LoginActivity, FindActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼

        // 로그인 버튼
        login_button.setOnClickListener {
            //                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
            user_info_pref =
                getSharedPreferences("userInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
            user_info_editor = user_info_pref.edit()
            val id = login_id.text.toString()
            val password = login_pw.text.toString()
            login_id_check.visibility = View.GONE
            login_pw_check.visibility = View.GONE
            login_check.visibility = View.GONE
            if (id == "" && password == "") { // 아이디, 비밀번호 둘 다 빈칸일 때
                login_id_check.visibility = View.VISIBLE
            } else if (id == "") { // 아이디만 빈칸일 때
                login_id_check.visibility = View.VISIBLE
            } else if (password == "") { // 비밀번호만 빈칸일 때
                login_pw_check.visibility = View.VISIBLE
            } else {
                //loadingDialog.show()

                //로그를 보기 위한 Interceptor
                val interceptor = HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client: OkHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
                // 데이터베이스 접속 및 확인
                val retrofit = Retrofit.Builder()
                    .baseUrl(IntroService.INTRO_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)     //로그 기능 추가
                    .build()

                val introAPI = retrofit.create(IntroService::class.java)

                    introAPI.login(id, password).enqueue(object : Callback <User> {
                    override fun onResponse(call: Call<User?>?, response: Response<User>) {
                        //loadingDialog.dismiss()
                        if (response.isSuccessful) { // 성공적으로 받아왔을 때
                            if (response.body()!!.isSuccess) { // 아이디, 비밀번호가 일치했을 때
                                val userdata = response.body() // 유저 데이터를 호출한 데이터베이스로부터 받아와 변수에 저장
                                if (userdata != null) {
                                    user_info_editor.putString("id", userdata.id)
                                }
                                if (userdata != null) {
                                    user_info_editor.putString("password", userdata.password)
                                }
                                if (userdata != null) {
                                    user_info_editor.putInt("image", userdata.image)
                                }
                                if (userdata != null) {
                                    user_info_editor.putString("name", userdata.name)
                                }
                                if (userdata != null) {
                                    user_info_editor.putString("phone", userdata.phone)
                                }
                                if (userdata != null) {
                                    user_info_editor.putString("email", userdata.email)
                                }
                                user_info_editor.commit() // 세션 영역에 해당 유저의 정보를 넣음
                                if (login_stay_cb.isChecked) { // 로그인 상태 유지가 체크되어 있다면
                                    user_info_editor.putBoolean(
                                        "autoLogin",
                                        true
                                    ) // 자동 로그인 여부를 true
                                    user_info_editor.commit()
                                    val intent =
                                        Intent(this@LoginActivity, MainActivty2::class.java)
                                    startActivity(intent)
                                    finish()
                                } else { // 로그인 상태 유지 체크가 안되어 있다면
                                    user_info_editor.putBoolean(
                                        "autoLogin",
                                        false
                                    ) // 자동 로그인 여부를 false
                                    user_info_editor.commit()
                                    val intent =
                                        Intent(this@LoginActivity, MainActivty2::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else { // 아이디, 비밀번호가 일치하지 않을 때
                                login_check.visibility = View.VISIBLE // 다시 확인하라는 텍스트뷰 출력
                            }
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        //loadingDialog.dismiss()
                        Toast.makeText(applicationContext, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                            .show()
                        t.printStackTrace()
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현 시간과 비교
        // 마지막으로 뒤로가기 버튼을 누른 시간이 2초가 지났으면 안내메시지 출력
        if (System.currentTimeMillis() > backKeyPressedTime + 1000) {
            backKeyPressedTime = System.currentTimeMillis().toInt()
            Toast.makeText(this, "\'뒤로가기\' 버튼을 한번 더 누르면 종료돼요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 마지막으로 뒤로가기 버튼 누른 시간이 2초가 지나지 않았으면 바로 종료
        // 즉, 뒤로가기 버튼 두 번을 눌러야 종료되는 메커니즘임
        if (System.currentTimeMillis() <= backKeyPressedTime + 1000) {
            finish()
        }
    }

}*/