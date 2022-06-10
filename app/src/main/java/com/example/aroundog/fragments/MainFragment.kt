package com.example.aroundog.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.setFragmentResult
import com.example.aroundog.R
import com.example.aroundog.SerialLatLng
import com.example.aroundog.RealtimeLocation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.timer


class MainFragment : Fragment(), OnMapReadyCallback{

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var pathList:ArrayList<LatLng> = ArrayList<LatLng>()
    var serialPathList:ArrayList<SerialLatLng> = ArrayList<SerialLatLng>()
    private var pathOverlay: PathOverlay = PathOverlay()
    private var isStart:Boolean = false
    private var isFirst:Boolean = true
    lateinit var overlayImage: OverlayImage
    lateinit var compassImage: OverlayImage
    lateinit var lastLocation: Location
    var walkDistance:Double = 0.0
    val TAG = "MainFragmentTAG"

    lateinit var frame:FrameLayout
    lateinit var startWalkButton:Button
    lateinit var walkDistanceTV:TextView
    lateinit var walkTimeTV:TextView
    lateinit var pauseButton:ImageButton
    lateinit var statusLayout:LinearLayout

    lateinit var timer:Timer
    var time:Long = 0

    val realdb:RealtimeLocation = RealtimeLocation()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mapView = parentFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                parentFragmentManager.beginTransaction().add(R.id.map, it, "map").commit()
            }
        mapView.getMapAsync(this)

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        locationSource.isCompassEnabled = true // 나침반 여부 지정

        overlayImage = OverlayImage.fromAsset("logo.png")

        pathOverlaySettings()

        realdb.initializeDbRef()

    }

    override fun onCreateView(//인터페이스를 그리기위해 호출
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        if(parentFragmentManager.findFragmentByTag("map") != null)
//            parentFragmentManager.beginTransaction().show(parentFragmentManager.findFragmentByTag("map")!!).commit()

        val view:ViewGroup = inflater.inflate(R.layout.fragment_main,container,false) as ViewGroup
        startWalkButton = view.findViewById(R.id.startWalkButton)
        walkTimeTV = view.findViewById(R.id.walkTimeTV)
        walkDistanceTV = view.findViewById(R.id.walkDistanceTV)
        pauseButton = view.findViewById(R.id.pauseButton)
        statusLayout = view.findViewById(R.id.statusLayout)
        frame = view.findViewById(R.id.map)

        //산책시작 버튼 클릭 리스너
        startWalkButton.setOnClickListener {
            Log.d(TAG, "산책시작 버튼 클릭")
            isStart = true
            pathList.add(LatLng(lastLocation))//시작위치 지정
            serialPathList.add(SerialLatLng(LatLng(lastLocation)))
            startWalk()

        }

        //산책종료 버튼클릭 리스너
        pauseButton.setOnClickListener {
            //산책결과 프래그먼트 추가해야함
            Toast.makeText(activity,"산책종료", Toast.LENGTH_SHORT).show()
            isStart = false



            var bundle:Bundle = Bundle()
            bundle.putSerializable("arraylist", LatLngToSerial())
            //bundle.putSerializable("arraylist", serialPathList)
            setFragmentResult("walkEnd",bundle)
            parentFragmentManager.beginTransaction().add(R.id.main_container, EndWalkFragment(), "endWalk").addToBackStack(null).commit()


            endWalk()
        }

        return view
    }
    fun LatLngToSerial(): ArrayList<SerialLatLng> {
        var tempList = ArrayList<SerialLatLng>()
        var iterator = pathList.iterator()
        while(iterator.hasNext()){
            var temp = SerialLatLng(iterator.next())
            tempList.add(temp)
        }
        return tempList
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    fun startWalk(){
        statusLayout.visibility=View.VISIBLE
        startWalkButton.visibility=View.GONE
        frame.layoutParams.height=0
        startTimer()
    }

    fun startTimer(){
        timer = kotlin.concurrent.timer(period = 1000){
            time++
            setTimer()
        }
    }
    fun stopTimer(){
        timer.cancel()
    }
    fun resetTimer(){
        timer.cancel()
        time=0
        setTimer()
    }
    fun setTimer(){
        var hour = TimeUnit.SECONDS.toHours(time)
        var minute = TimeUnit.SECONDS.toMinutes(time) - hour*60
        var second = TimeUnit.SECONDS.toSeconds(time) - hour*3600 - minute*60
        walkTimeTV.text = String.format("%02d",hour) + " : " + String.format("%02d",minute) + " : "  + String.format("%02d",second)
    }

    fun pathOverlaySettings(){
        pathOverlay.outlineWidth=0//테두리 없음
        pathOverlay.width=20//경로선 폭
        pathOverlay.passedColor = Color.RED//지나온 경로선
        pathOverlay.color= Color.GREEN//경로선 색상
    }

    fun endWalk(){
        pathList.clear()
        pathOverlay.map=null
        walkDistance = 0.0
        //서버에는 time 전달
        statusLayout.visibility=View.GONE
        startWalkButton.visibility=View.VISIBLE

        val layout:ViewGroup.LayoutParams = frame.layoutParams
        layout.width=ViewGroup.LayoutParams.MATCH_PARENT
        layout.height=ViewGroup.LayoutParams.MATCH_PARENT
        frame.layoutParams = layout
        resetTimer()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
                Log.d(TAG, "권한거부")

                return
            }
            else{

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun uiSettings(){
        //naverMap.uiSettings.isCompassEnabled=true
        naverMap.uiSettings.isLocationButtonEnabled=true//현재위치 버튼 여부
        naverMap.uiSettings.isZoomControlEnabled=false//줌 버튼 여부
    }

    fun setlocationOverlay(): LocationOverlay {
        var locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.icon=overlayImage
        locationOverlay.iconHeight = 100
        locationOverlay.iconWidth = 100

        return locationOverlay
    }

    override fun onMapReady(p0: NaverMap) {
        this.naverMap = p0
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        uiSettings()//지도 ui세팅

        var locationOverlay = setlocationOverlay()

        var ymarker = Marker()
        ymarker.position = LatLng(37.514,126.838)
        ymarker.map = naverMap
        var zmarker = Marker()
        zmarker.position = LatLng(37.5133,126.83)
        zmarker.map = naverMap

        //옵션 변경될때의 리스너
        naverMap.addOnOptionChangeListener {
            val mode=naverMap.locationTrackingMode
            if(mode== LocationTrackingMode.None){
                naverMap.locationTrackingMode= LocationTrackingMode.NoFollow
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
            }
            else{//위치업데이트일때
                if(isStart){//산책을 시작했다면
                    //pathOverlay.map=null
                    var updateLocation:LatLng = LatLng(location)
                    walkDistance += updateLocation.distanceTo(pathList.last())//마지막 위치와 현재 위치의 거리차이 저장
                    walkDistanceTV.text = walkDistance.toInt().toString() + " M"
                    pathList.add(updateLocation)
                    serialPathList.add(SerialLatLng(updateLocation))
                    pathOverlay.coords = pathList
                    pathOverlay.map = naverMap

                    realdb.writeNewUser("x", location.latitude, location.longitude) //현재 위치 db전송
                    ymarker.position = realdb.getValue("y")
                    ymarker.position = realdb.getValue("z")

                    Log.d("firebase", "ymarker position "+ymarker.position.toString())

                }else {
                    //textView.text = "이동거리 0M"
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
}