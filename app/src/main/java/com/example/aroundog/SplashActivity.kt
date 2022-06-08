package com.example.aroundog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val pref = getSharedPreferences("userInfo", MODE_PRIVATE)
        val editor = pref.edit()
        val autoLogin = pref.getBoolean("autoLogin", false)
        Handler(Looper.getMainLooper()).postDelayed({
            if (autoLogin == false) {
                // 자동 로그인 하지 않았을 경우 로그인으로 이동하며 세션 영역 내 정보 삭제
                editor.clear()
                val intent = Intent(this@SplashActivity, MainActivity2::class.java)
                startActivity(intent)
                finish()
            } else {
                // 자동 로그인 체크하였을 경우 바로 메인 액티비티로 이동
                val intent = Intent(this@SplashActivity, MainActivty::class.java)
                startActivity(intent)
                finish()
            }

            //                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            //                startActivity(intent);
            //                finish();
        }, 2500)
    }
}