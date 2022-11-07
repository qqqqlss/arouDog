package com.example.aroundog

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.aroundog.Service.Polyline
import com.example.aroundog.dto.DogDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.OverlayImage

class WalkInfoMapActivity : AppCompatActivity() {

    lateinit var walkInfoMapExit:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_info_map)

        setView()

        //x버튼 클릭시 액티비티 닫음
        walkInfoMapExit.setOnClickListener {
            finish()
        }

        //인텐트에서 데이터 받아옴
        val  pathData = intent.getStringExtra("pathData")!!.toString()

        //파싱
        var gson = GsonBuilder().create()
        var type: TypeToken<MutableList<Polyline>> = object: TypeToken<MutableList<Polyline>>(){}
        var pathPoints = gson.fromJson<MutableList<Polyline>>(pathData, type.type)

        //프래그먼트 생성
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.infoMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.infoMap, it).commit()
            }

        mapFragment.getMapAsync {
            var naverMap = it

            //줌 버튼 삭제
            var uiSettings = naverMap.uiSettings
            uiSettings.isZoomControlEnabled = false

            var multipartPath = MultipartPathOverlay()
            val colorList = ArrayList<MultipartPathOverlay.ColorPart>()
            val colorPart = MultipartPathOverlay.ColorPart(
                Color.rgb(235, 218, 179),   // 지나갈 경로선의 선 색상을 빨간색으로 지정
                Color.WHITE, // 지나갈 경로선의 테두리 색상을 흰색으로 지정
                Color.GRAY,  // 지나온 경로선의 선 색상을 회색으로 지정
                Color.LTGRAY // 지나온 경로선의 테두리 색상을 밝은 회색으로 지정
            )
            for (i in 0 until pathPoints.size step 1) {
                colorList.add(colorPart)
            }
            multipartPath.coordParts = pathPoints
            multipartPath.colorParts = colorList
            multipartPath.outlineWidth = 5//테두리 없음
            multipartPath.width = 30//경로선 폭
            multipartPath.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
            multipartPath.patternInterval = 40
            multipartPath.map = naverMap

            var bounds: LatLngBounds = multipartPath.bounds//그려진 오버레이의 영역 리턴
            var cameraUpdate: CameraUpdate =
                CameraUpdate.fitBounds(bounds, 400)//오버레이가 다 보일수 있게 카메라 이동시키는 CameraUpdate 생성

            naverMap.moveCamera(cameraUpdate)//카메라 이동
        }
    }

    fun setView() {
        walkInfoMapExit = findViewById(R.id.walkInfoMapExit)
    }
}