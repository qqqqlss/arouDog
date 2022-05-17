package com.example.aroundog

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.aroundog.databinding.ActivityMainBinding
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
import kotlin.concurrent.thread

//, MapView.CurrentLocationEventListener
class MainActivity : AppCompatActivity(){
    private var TAG: String = "MAINTAG"
    private lateinit var binding: ActivityMainBinding
    lateinit var userPermission: PermissionSupport

    private lateinit var fusedLocationClient: FusedLocationProviderClient//위치 서비스 클라이언트
    private lateinit var locationRequest:LocationRequest
    private lateinit var mCurrentLocation: Location
    private lateinit var locationCallback: LocationCallback
    var centerPoint: MapPoint? = null
    lateinit var mapPolyline: MapPolyline
    var isStart:Boolean = false
    var textView: TextView? = null
    var mapArrayList:ArrayList<MapPoint> = ArrayList()
    var count =0

    //나침반
    //https://copycoding.tistory.com/116
    //




    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        permissionCheck()
        textView = binding.textView
        textView!!.movementMethod = ScrollingMovementMethod()
        textView!!.post {
            val scrollAmount = textView!!.layout.getLineTop(textView!!.lineCount) - textView!!.height
            if (scrollAmount > 0)
                textView!!.scrollTo(0, scrollAmount)
            else
                textView!!.scrollTo(0,0)
        }
        initGoogleApiClient()


        //버튼
        val startButton = binding.start
        startButton.setOnClickListener {
            if (!isStart) {
                isStart = true
                printLog(TAG, "Button click")
                mapPolyline = MapPolyline()
            }
            else{
                //mapPolyline.
            }
        }

//        //캐시기능
//        if(!MapView.isMapTilePersistentCacheEnabled()) {
//            MapView.setMapTilePersistentCacheEnabled(true)
//        }
        //mapView.setCurrentLocationEventListener(this)
        //mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

        //https://saysimple.tistory.com/36
//        if(centerPoint != null){
//            binding.mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(centerPoint!!.mapPointGeoCoord.latitude, centerPoint!!.mapPointGeoCoord.longitude), false)
//        }
//        thread(start=true){
//            var currentLocation = MapPoint.mapPointWithGeoCoord(37.6, 127.6)
//            while (true){
//                    mapView.removeAllPolylines()
//                    Thread.sleep(100L)
//                    mapPolyline?.addPoint(currentLocation)
//                Log.d(TAG, mapPolyline.toString())
//                    //mapView.addPolyline(mapPolyline)
//                    Log.d(TAG, "thread")
//                }
//        }
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(userPermission.permissionResult(requestCode, permissions, grantResults ))
            userPermission.requestPermission()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

//
//    override fun onCurrentLocationUpdate(mapView: MapView?, currentLocation: MapPoint?, accuracyInMeters: Float) {
//        Log.d(TAG, "update")
//
//        binding.mapView.setShowCurrentLocationMarker(false)
//        if(currentLocation != null) {
//            if (count > 10) {
//                centerPoint = currentLocation
//                if (mapView != null) {
////                    binding.mapView.setMapCenterPointAndZoomLevel(
////                        MapPoint.mapPointWithGeoCoord(
////                            currentLocation.mapPointGeoCoord.latitude,
////                            currentLocation.mapPointGeoCoord.longitude
////                        ), 1, false
////                    )
//                //지도 이미지 위치 이동
//                    if (isStart) {//산책중일때(위치정보 저장)
//                        printLog(
//                            TAG,
//                            "currentLocation : ${
//                                currentLocation.mapPointGeoCoord.latitude.toString().substring(0, 6)
//                            }, ${currentLocation.mapPointGeoCoord.longitude.toString().substring(0, 6)}"
//                        )
//                        mapView.removeAllPolylines()
//                        mapPolyline?.addPoint(currentLocation)
//                        mapView.addPolyline(mapPolyline)
////                    printLog(TAG, "산책 위치")
////                    mapView.removeAllPolylines()
////                    mapPolyline?.addPoint(currentLocation)
//////                    mapView.addPolyline(mapPolyline)
////                    mapView.removeAllPolylines()
////                    mapArrayList.add(currentLocation)
////                    var array:Array<MapPoint> = mapArrayList.toTypedArray()
////                    var mapPolyline2 = MapPolyline()
////                    mapPolyline2.addPoints(array)
////                    binding.mapView.addPolyline(mapPolyline2)
//                    }
//                }
//                count = 0
//            }else{
//                count++
//            }
//        }
//    }
//
//    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
//        Log.d(TAG, "head update")
//        binding.mapView.setMapRotationAngle(0F,false)
//    }
//
//    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
//        TODO("Not yet implemented")
//    }
    fun printLog(TAG:String, msg:String){
        textView?.append(msg + "\n")
        Log.d(TAG, msg)
    }

    fun initGoogleApiClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val firstCenter:MapPoint = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)

                    binding.mapView.setMapCenterPointAndZoomLevel(firstCenter,1,false)
                    Log.d(TAG, "first")
                }
            }
        createLocationRequest()
        initLocationCallback()

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())



    }
    fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    }
    fun initLocationCallback(){
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return//null이면 반환
//                for (location in locationResult.locations) {
//                    // Update UI with location data
//                    // ...
//                }
                if(isStart) {
                    val location: MapPoint = toMapPoint(p0.lastLocation)
                    binding.mapView.removeAllPolylines()
                    mapPolyline.addPoint(location)
                    binding.mapView.addPolyline(mapPolyline)
                    //binding.mapView.addPolyline(mapPolyline)
                }
//                var location:Location = p0.lastLocation
//                var latitude = location.latitude
//                var longitude = location.longitude
            }
        }
    }

    fun toMapPoint(location:Location):MapPoint{
        Log.d(TAG, "lati : ${location.latitude}, long: ${location.longitude}")
        return MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)
    }

}
