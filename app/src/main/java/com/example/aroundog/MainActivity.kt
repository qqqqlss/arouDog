package com.example.aroundog

import android.graphics.Color
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.aroundog.databinding.ActivityMainAfterLoginBinding
import com.example.aroundog.databinding.ActivityMainBinding
import com.example.aroundog.fragments.AroundWalkFragment
import com.example.aroundog.fragments.ProfileFragment
import com.example.aroundog.fragments.MainFragment
import com.google.android.material.navigation.NavigationBarView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource

class MainActivity : AppCompatActivity(){
    private var TAG: String = "MAINTAG"
    private lateinit var binding: ActivityMainAfterLoginBinding
    lateinit var userPermission: PermissionSupport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAfterLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        permissionCheck()
        setFragment()

    }
    fun setFragment(){
        val mainFragment:MainFragment = MainFragment()
        val aroundWalkFragment:AroundWalkFragment = AroundWalkFragment()
        val profileFragment:ProfileFragment = ProfileFragment()


        supportFragmentManager.beginTransaction().replace(R.id.main_container, mainFragment,"walk").commitAllowingStateLoss()
        binding.bottomNav.menu.findItem(R.id.statusLayout).setChecked(true)//시작은 산책하기로

        binding.bottomNav.setOnItemSelectedListener(object: NavigationBarView.OnItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.statusLayout->{//산책하기
                        if(supportFragmentManager.findFragmentByTag("walk") != null)
                            supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("walk")!!).commit()
                        else
                            supportFragmentManager.beginTransaction().add(R.id.main_container,mainFragment,"walk").commit()

                        //다른프래그먼트는 가리기
                        if(supportFragmentManager.findFragmentByTag("aroundWalk") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("aroundWalk")!!).commit()
                        if(supportFragmentManager.findFragmentByTag("profile") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("profile")!!).commit()
                        return true
                    }
                    R.id.aroundWalk->{//주변 경로
                        if(supportFragmentManager.findFragmentByTag("aroundWalk") != null)
                            supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("aroundWalk")!!).commit()
                        else
                            supportFragmentManager.beginTransaction().add(R.id.main_container,aroundWalkFragment,"aroundWalk").commit()

                        //다른프래그먼트는 가리기
                        if(supportFragmentManager.findFragmentByTag("walk") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("walk")!!).commit()
                        if(supportFragmentManager.findFragmentByTag("profile") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("profile")!!).commit()
                        return true
                    }
                    R.id.profile -> {//프로필
                        if(supportFragmentManager.findFragmentByTag("profile") != null)
                            supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("profile")!!).commit()
                        else
                            supportFragmentManager.beginTransaction().add(R.id.main_container,profileFragment,"profile").commit()

                        //다른프래그먼트는 가리기
                        if(supportFragmentManager.findFragmentByTag("walk") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("walk")!!).commit()
                        if(supportFragmentManager.findFragmentByTag("aroundWalk") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("aroundWalk")!!).commit()
                        return true
                    }
                }
                return false
            }
        })

    }

    // 권한 체크
    private fun permissionCheck() {
        // SDK 23버전 이하 버전에서는 Permission이 필요하지 않다.
        if (Build.VERSION.SDK_INT >= 23) {
            // 방금 전 만들었던 클래스 객체 생성
            userPermission = PermissionSupport(this, this)
            // 권한 체크한 후에 리턴이 false로 들어온다면
            if (!userPermission.check()) {
                // 권한 요청을 해줍니다.
                userPermission.requestPermission()
            }
        }
    }

}