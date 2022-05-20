package com.example.aroundog.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.aroundog.LastLocation
import com.example.aroundog.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "WALKFRAGMENTTAG"
/**
 * A simple [Fragment] subclass.
 * Use the [WalkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WalkFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var pathOverlay: PathOverlay = PathOverlay()
    lateinit var overlayImage: OverlayImage
    private var pathList:ArrayList<LatLng> = ArrayList<LatLng>()
    lateinit var firstLocation: Location
    lateinit var lastLocation: Location
    lateinit var textView: TextView
    var walkDistance:Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mapView = childFragmentManager.findFragmentById(R.id.walking) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.walking, it).commit()
            }
        mapView.getMapAsync(this)

        locationSource =
            FusedLocationSource(this, 1000)

        //locationSource.isCompassEnabled = true // 나침반 여부

        overlayImage = OverlayImage.fromAsset("logo.png")

        pathOverlaySettings()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:ViewGroup = inflater.inflate(R.layout.fragment_walk,container,false) as ViewGroup
        textView = view.findViewById(R.id.textView)

        setFragmentResultListener("walkStart"){ requestKey, bundle ->
            firstLocation = (bundle.getSerializable("lastLocation") as LastLocation).location

        }
        return view

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WalkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WalkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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
//        Log.d(TAG,"walk distance ${walkDistance}")
//        walkDistance = 0.0

    }


    override fun onMapReady(p0:NaverMap) {
        this.naverMap = p0

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
        locationSource.isCompassEnabled = true//있어야 실시간으로 각도변경 가능

        uiSettings()
        var locationOverlay = setlocationOverlay()

        //옵션 변경될때의 리스너
        naverMap.addOnOptionChangeListener {
            val mode = naverMap.locationTrackingMode
            if (mode == LocationTrackingMode.None) {
                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            }
            if (mode == LocationTrackingMode.NoFollow) {
                Log.d(TAG, "mode NoFollow")
                naverMap.cameraPosition = CameraPosition(LatLng(lastLocation), 16.0, 0.0, 0.0)
            }
        }

        //지도 첫번째 로딩될때 리스너 등록
        naverMap.addOnLoadListener(object:NaverMap.OnLoadListener{
            override fun onLoad() {
                var location:Location = Location("")
                location.latitude = firstLocation.latitude
                location.longitude = firstLocation.longitude
                naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(location), 16.0))
                pathList.add(LatLng(location))
                lastLocation= location
                Log.d(TAG, "첫번째 로딩")
            }
        })

        //위치 업데이트될때의 리스너
        //bearing업데이트일때도 여기로 들어옴
        naverMap.addOnLocationChangeListener { location ->

            if (naverMap.locationTrackingMode == LocationTrackingMode.NoFollow) {
                locationOverlay.bearing = 0f
            }

            if (location == lastLocation) {//각도업데이트일때
                Log.d(TAG, "bearing : ${location.bearing}")
                //locationOverlay.bearing=0f
            } else {//위치업데이트일때
//                if (isStart) {//산책을 시작했다면
//                    //pathOverlay.map=null
                    var latLocation: LatLng = LatLng(location)
                    var distance: Double = latLocation.distanceTo(pathList.last())
                    Log.d(
                        TAG,
                        "마지막 " + pathList.last().toString() + " 현재 " + latLocation.toString()
                    )
                    Log.d(TAG, "이전과 거리차이" + distance.toString())
                    walkDistance += latLocation.distanceTo(pathList.last())//마지막 위치와 현재 위치의 거리차이 저장

                    textView.text = "이동거리 : " + walkDistance.toInt().toString() + "M"
                    //Toast.makeText(activity,walkDistance.toString(), Toast.LENGTH_SHORT).show()
                    pathList.add(latLocation)
                    pathOverlay.coords = pathList
                    pathOverlay.map = naverMap
                }
                Log.d(TAG, "위치업데이트")

            lastLocation = location

        }

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
}