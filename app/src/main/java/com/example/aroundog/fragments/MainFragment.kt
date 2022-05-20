package com.example.aroundog.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.example.aroundog.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource

class MainFragment : Fragment(), OnMapReadyCallback{

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private var pathList:ArrayList<LatLng> = ArrayList<LatLng>()
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mapView = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        mapView.getMapAsync(this)

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        //locationSource.isCompassEnabled = true // 나침반 여부

        overlayImage = OverlayImage.fromAsset("logo.png")
        //compassImage = OverlayImage.fromResource(R.drawable.compass)

        pathOverlaySettings()
    }

    override fun onCreateView(//인터페이스를 그리기위해 호출
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view:ViewGroup = inflater.inflate(R.layout.fragment_main,container,false) as ViewGroup
        startWalkButton = view.findViewById(R.id.startWalkButton)

        walkTimeTV = view.findViewById(R.id.walkTimeTV)
        walkDistanceTV = view.findViewById(R.id.walkDistanceTV)
        pauseButton = view.findViewById(R.id.pauseButton)
        statusLayout = view.findViewById(R.id.statusLayout)
        frame = view.findViewById(R.id.map)

        startWalkButton.setOnClickListener {
            Log.d(TAG, "button click")
            if (isStart) {
                isStart = false
                endWalk()
                Toast.makeText(activity, "산책끝", Toast.LENGTH_SHORT).show()
            } else {
                isStart = true
                //시작위치 지정
                pathList.add(LatLng(lastLocation))
                startWalk()
                Toast.makeText(activity, "산책시작", Toast.LENGTH_SHORT).show()
            }
        }

        //return inflater.inflate(R.layout.fragment_walk, container, false)
        return view
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    fun startWalk(){
        statusLayout.visibility=View.VISIBLE
        startWalkButton.visibility=View.GONE
        frame.layoutParams.height=0

    }
    fun pathOverlaySettings(){
        pathOverlay.outlineWidth=0//테두리 없음
        pathOverlay.width=20//경로선 폭
        pathOverlay.passedColor = Color.RED//지나온 경로선
        pathOverlay.color= Color.GREEN//경로선 색상
    }

    fun endWalk(){
        //Log.d(TAG,"end walk ${pathList.toString()}")
        pathList.clear()
        pathOverlay.map=null
        Log.d(TAG,"walk distance ${walkDistance}")
        walkDistance = 0.0

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
        naverMap.uiSettings.isLocationButtonEnabled=true
        naverMap.uiSettings.isZoomControlEnabled=false
    }
    fun setlocationOverlay(): LocationOverlay {
        var locationOverlay: LocationOverlay = naverMap.locationOverlay
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

                    walkDistanceTV.text = walkDistance.toInt().toString() + " M"
                    //Toast.makeText(activity,walkDistance.toString(), Toast.LENGTH_SHORT).show()
                    pathList.add(latLocation)
                    pathOverlay.coords = pathList
                    pathOverlay.map = naverMap
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