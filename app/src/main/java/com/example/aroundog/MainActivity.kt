package com.example.aroundog

import android.graphics.Color
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback{
    private var TAG: String = "MAINTAG"
    private lateinit var binding: ActivityMainBinding
    lateinit var userPermission: PermissionSupport
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private var pathList:ArrayList<LatLng> = ArrayList<LatLng>()
    private var pathOverlay:PathOverlay= PathOverlay()
    private var isStart:Boolean = false
    private var isFirst:Boolean = true
    lateinit var overlayImage:OverlayImage
    lateinit var compassImage:OverlayImage

    lateinit var lastLocation:Location
    var walkDistance:Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//프래그먼트
//        https://huiung.tistory.com/160
//        https://velog.io/@woonyumnyum/Android-Studio-%ED%94%84%EB%9E%98%EA%B7%B8%EB%A8%BC%ED%8A%B8%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%84%A4%EB%B9%84%EA%B2%8C%EC%9D%B4%EC%85%98-%EB%A7%8C%EB%93%A4%EA%B8%B0
//        https://code-algo.tistory.com/2
        permissionCheck()

//        binding.mapView.onCreate(savedInstanceState)
//        binding.mapView.getMapAsync(this)
//        binding.button.setOnClickListener {
//
//            if (isStart) {
//                isStart = false
//                endWalk()
//                Toast.makeText(this, "산책끝", Toast.LENGTH_SHORT).show()
//            } else {
//                isStart = true
//                //시작위치 지정
//                pathList.add(LatLng(lastLocation))
//
//                Toast.makeText(this, "산책시작", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        locationSource =
//            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
//
//        //locationSource.isCompassEnabled = true // 나침반 여부
//
//        overlayImage = OverlayImage.fromAsset("logo.png")
//        //compassImage = OverlayImage.fromResource(R.drawable.compass)
//
//        pathOverlaySettings()


//-------------------------------여기서부터 프래그먼트 관련------------------------
        setFragment()

    }
    fun setFragment(){
        val mainFragment:MainFragment = MainFragment()
        val aroundWalkFragment:AroundWalkFragment = AroundWalkFragment()
        val profileFragment:ProfileFragment = ProfileFragment()


        supportFragmentManager.beginTransaction().replace(R.id.main_container, mainFragment,"walk").commitAllowingStateLoss()
        binding.bottomNav.menu.findItem(R.id.walk).setChecked(true)//시작은 산책하기로

        binding.bottomNav.setOnItemSelectedListener(object: NavigationBarView.OnItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.walk->{//산책하기
                        if(supportFragmentManager.findFragmentByTag("walkStart")!=null){
                            supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("walkStart")!!).commit()

                            if(supportFragmentManager.findFragmentByTag("aroundWalk") != null)
                                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("aroundWalk")!!).commit()
                            if(supportFragmentManager.findFragmentByTag("profile") != null)
                                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("profile")!!).commit()
                            if(supportFragmentManager.findFragmentByTag("walk") != null)
                                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("walk")!!).commit()
                            return true
                        }
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

                        if(supportFragmentManager.findFragmentByTag("walkStart") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("walkStart")!!).commit()
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

                        if(supportFragmentManager.findFragmentByTag("walkStart") != null)
                            supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("walkStart")!!).commit()
                        return true
                    }
                }
                return false
            }
        })

    }

    fun pathOverlaySettings(){
        pathOverlay.outlineWidth=0//테두리 없음
        pathOverlay.width=20//경로선 폭
        pathOverlay.passedColor = Color.RED//지나온 경로선
        pathOverlay.color=Color.GREEN//경로선 색상
    }

    fun endWalk(){
        //Log.d(TAG,"end walk ${pathList.toString()}")
        pathList.clear()
        pathOverlay.map=null
        Log.d(TAG,"walk distance ${walkDistance}")
        walkDistance = 0.0

    }


//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
//                grantResults)) {
//            if (!locationSource.isActivated) { // 권한 거부됨
//                naverMap.locationTrackingMode = LocationTrackingMode.None
//                Log.d(TAG, "권한거부")
//
//                return
//            }
//            else{
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
    fun uiSettings(){
        //naverMap.uiSettings.isCompassEnabled=true
        naverMap.uiSettings.isLocationButtonEnabled=true
        naverMap.uiSettings.isZoomControlEnabled=false
    }
    fun setlocationOverlay():LocationOverlay{
        var locationOverlay:LocationOverlay = naverMap.locationOverlay
        locationOverlay.icon=overlayImage
//        locationOverlay.subIcon = compassImage
//        locationOverlay.subIcon=LocationOverlay.DEFAULT_SUB_ICON_ARROW
        locationOverlay.iconHeight = 50
        locationOverlay.iconWidth = 100

        return locationOverlay

    }
    override fun onMapReady(p0: NaverMap) {
        this.naverMap = p0

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
        locationSource.isCompassEnabled =true//있어야 실시간으로 각도변경 가능

        uiSettings()
        var locationOverlay = setlocationOverlay()

        //옵션 변경될때의 리스너
        naverMap.addOnOptionChangeListener {
            val mode=naverMap.locationTrackingMode
            if(mode==LocationTrackingMode.None){
                naverMap.locationTrackingMode=LocationTrackingMode.NoFollow
            }
            if(mode == LocationTrackingMode.NoFollow) {
                Log.d(TAG, "mode NoFollow")
                naverMap.cameraPosition = CameraPosition(LatLng(lastLocation),16.0, 0.0,0.0)
            }
        }
        //위치 업데이트될때의 리스너
        //bearing업데이트일때도 여기로 들어옴
        naverMap.addOnLocationChangeListener { location ->

            if(naverMap.locationTrackingMode == LocationTrackingMode.NoFollow){
                locationOverlay.bearing=0f
            }

            //지도 첫 로딩시
            if (isFirst) {
                naverMap.moveCamera(
                    CameraUpdate.scrollAndZoomTo(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 16.0
                    )
                )
                isFirst = false
                Log.d(TAG, "첫번째 위치 업데이트")
                lastLocation = location
            }

            if (location == lastLocation){//각도업데이트일때
                Log.d(TAG, "bearing : ${location.bearing}")
                //locationOverlay.bearing=0f
            }
            else{//위치업데이트일때
                if(isStart){//산책을 시작했다면
                    //pathOverlay.map=null
                    var latLocation:LatLng = LatLng(location)
                    var distance:Double = latLocation.distanceTo(pathList.last())
                    Log.d(TAG, "마지막 " + pathList.last().toString()+" 현재 "+ latLocation.toString())
                    Log.d(TAG,"이전과 거리차이"+distance.toString())
                    walkDistance += latLocation.distanceTo(pathList.last())//마지막 위치와 현재 위치의 거리차이 저장


                    pathList.add(latLocation)
                    pathOverlay.coords = pathList
                    pathOverlay.map = naverMap
                    //binding.walkDistance.text= walkDistance.toLong().toString() + "M"
                }
                Log.d(TAG, "위치업데이트")
            }

            lastLocation = location

        }
//        //지도 첫번째 로딩될때 리스너 등록
//        naverMap.addOnLoadListener(object:NaverMap.OnLoadListener{
//            override fun onLoad() {
//                var coor:Location? = locationSource.lastLocation
//                if (coor != null) {
//                    Log.d(TAG , "first location ${ coor.latitude }, ${coor.longitude}")
//                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(coor.latitude,coor.longitude), 16.0))
//                }
//                Log.d(TAG, "첫번째 로딩")
//            }
//        })
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}