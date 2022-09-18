package com.example.aroundog.fragments

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.aroundog.BuildConfig
import com.example.aroundog.Model.DogBreed
import com.example.aroundog.R
import com.example.aroundog.SerialLatLng
import com.example.aroundog.Service.CoordinateService
import com.example.aroundog.dto.UserCoordinateDogDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainFragment : Fragment(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var pathList: ArrayList<LatLng> = ArrayList<LatLng>()
    private var pathOverlay: PathOverlay = PathOverlay()
    private var isStart: Boolean = false
    private var isFirst: Boolean = true
    lateinit var overlayImage: OverlayImage
    lateinit var compassImage: OverlayImage
    lateinit var lastLocation: Location
    lateinit var startTime: LocalDateTime
    var walkDistance: Double = 0.0
    val TAG = "MainFragmentTAG"

    lateinit var frame: FrameLayout
    lateinit var startWalkButton: Button
    lateinit var walkDistanceTV: TextView
    lateinit var walkTimeTV: TextView
    lateinit var pauseButton: ImageButton
    lateinit var statusLayout: LinearLayout
    lateinit var webView: WebView
    lateinit var timer: Timer
    var time: Long = 0

    lateinit var strTime: String
    var tile = ""
    lateinit var userId: String
    lateinit var retrofit: CoordinateService
    lateinit var databaseCoroutine: Job

    lateinit var userCoordinateDogDtoList:List<UserCoordinateDogDto>
    var updateCoordinateMap = HashMap<Long, Marker>()//<개id, 마커>
    var visibleOnMapMap = HashMap<Long, Marker>()

    lateinit var dog1:OverlayImage
    lateinit var dog2:OverlayImage
    lateinit var dog3:OverlayImage
    
    var aroundUserMap = HashMap<Long, String>() //개id, 유저id
    var duplicateUserDog = HashSet<Long>()//주인이 중복되는 개 id 저장

    lateinit var bounds:LatLngBounds
    var width:Double = 0.0

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

        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id", "error").toString()

        //환경설정에서 알람을 표시할 영역을 설정하는 변수, 저장 필요, 일단 임의값으로 대체
        //소수점 다섯째 자리가 약 1m정도씩 차이남
        width = 0.001 //100m

        //retrofit
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(CoordinateService::class.java)

        dog1 = OverlayImage.fromResource(R.drawable.dog1)
        dog2 = OverlayImage.fromResource(R.drawable.dog2)
        dog3 = OverlayImage.fromResource(R.drawable.dog3)
    }

    override fun onCreateView(//인터페이스를 그리기위해 호출
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: ViewGroup = setView(inflater, container)

        //산책시작 버튼 클릭 리스너
        startWalkButton.setOnClickListener {
            Log.d(TAG, "산책시작 버튼 클릭")

            //최신 위치가 저장되었는지 확인
            if (this::lastLocation.isInitialized) {
                startWalk()
                dbProcess()
                createWebView()//웹뷰 생성, tile 값 지정
            } else{
                Toast.makeText(context, "로딩중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show()
            }


        }

        //산책종료 버튼클릭 리스너
        pauseButton.setOnClickListener {
            isStart = false

            //Bundle설정
            setBundle()
            parentFragmentManager.beginTransaction()
                .add(R.id.main_container, EndWalkFragment(), "endWalk").addToBackStack(null)
                .commit()

            //산책 종료 메서드
            endWalk()
            
            //insert/update 코루틴 종료
            databaseCoroutine.cancel()

            //지도에서 마커 삭제
            CoroutineScope(Dispatchers.Main).launch {
                updateCoordinateMap.forEach{ (id, marker)->
                    marker.map = null
                }
                updateCoordinateMap.clear() //서버에서 불러오는 정보 저장하는 맵 초기화
                visibleOnMapMap.clear() //지도에 표시되는 마커들이 저장된 맵 초기화
                aroundUserMap.clear() //개id, 유저id 해시맵 초기화
                duplicateUserDog.clear() //중복되는 유저 초기화
            }

            //서버에 false 전송
            retrofit.endWalking(userId).enqueue(object:Callback<Boolean>{
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "endWalking is success");
                    }
                }
                override fun onFailure(
                    call: Call<Boolean>,
                    t: Throwable
                ) {
                    Log.d(TAG, "endWalking fail", t)
                }
            })
        }//listener

        return view
    }

    private fun dbProcess() {
        //첫 실행 여부 확인
        var coorUpdate = false
        databaseCoroutine = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (tile.isEmpty()) {
                    //타일 값 받을때까지
                    delay(10L)
                    continue
                }//if

                if (naverMap != null) {
                    if (!coorUpdate) {//첫 실행일때는 모든 정보 넣어서 테이블에 추가
                        retrofit.insert(
                            userId,
                            lastLocation.latitude,
                            lastLocation.longitude,
                            tile
                        ).enqueue(object : Callback<Boolean> {
                            override fun onResponse(
                                call: Call<Boolean>,
                                response: Response<Boolean>
                            ) {
                                if (response.isSuccessful) {
                                    if (response.body() == true) {
                                        Log.d(TAG, "업데이트 성공")
                                    } else
                                        Log.d(TAG, "업데이트 실패")
                                }
                            }

                            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                Log.d(TAG, "전송실패 ", t)
                            }
                        })
                        Log.d(TAG, "tile ${tile}")
                        coorUpdate = true
                    } //if

                    else {//첫실행 아닐때
                        retrofit.update(
                            userId,
                            lastLocation.latitude,
                            lastLocation.longitude,
                            tile
                        ).enqueue(object : Callback<Boolean> {
                            override fun onResponse(
                                call: Call<Boolean>,
                                response: Response<Boolean>
                            ) {
                                if (response.isSuccessful) {
                                    if (response.body() == true) {
                                        Log.d(TAG, "업데이트 성공")
                                    } else
                                        Log.d(TAG, "업데이트 실패")
                                }
                            }

                            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                Log.d(TAG, "전송실패 ", t)
                            }
                        })
                    }//else

                    //다른 사용자들의 위치정보 불러오기
                    //tile 올려서 해당 사용자들만 받아오게
                    retrofit.getWalkingList(tile)
                        .enqueue(object : Callback<List<UserCoordinateDogDto>> {
                            override fun onResponse(
                                call: Call<List<UserCoordinateDogDto>>,
                                response: Response<List<UserCoordinateDogDto>>
                            ) {
                                if (response.isSuccessful) {
                                    userCoordinateDogDtoList = response.body()!!
                                    updateCoordinateMap.clear()
                                    userCoordinateDogDtoList.forEach { dto ->
                                        if (!userId.equals(dto.userId)) {//자신의 정보를 제외하고 실행
                                            if (!visibleOnMapMap.containsKey(dto.dogId)) {//해당 아이디가 지도에 없으면(visibleOnMapMap) 마커 추가
                                                var latLng: LatLng
                                                if (!aroundUserMap.containsValue(dto.userId)) {//주인이 중복되지 않는경우
                                                    latLng = LatLng(
                                                        dto.latitude,
                                                        dto.longitude
                                                    )
                                                } else {
                                                    latLng = LatLng(
                                                        dto.latitude - 0.0001,
                                                        dto.longitude - 0.0001
                                                    )
                                                    duplicateUserDog.add(dto.dogId)//주인이 중복될경우 개id저장
                                                }

                                                var marker = Marker().apply {
                                                    position = latLng
                                                    captionText = dto.dogName
                                                    icon = setDogImage(dto.dogBreed)//이미지 설정
                                                    setOnClickListener { o ->
                                                        Toast.makeText(
                                                            context,
                                                            dto.dogName + " / " + dto.dogAge + "살",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        true
                                                    }
                                                }
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    marker.map = naverMap
                                                }
                                                visibleOnMapMap.put(
                                                    dto.dogId,
                                                    marker
                                                ) //지도에 표시되는 마커를 관리하는 맵에 추가
                                                updateCoordinateMap.put(
                                                    dto.dogId,
                                                    marker
                                                ) //서버에서 불러온 정보를 저장한 맵에도 추가
                                                aroundUserMap.put(dto.dogId, dto.userId)//개, 유저 매핑

                                            } else {//지도에 있으면 position 변경
                                                var latLng: LatLng
                                                if (!duplicateUserDog.contains(dto.dogId)) {//주인이 중복되지 않는경우
                                                    latLng = LatLng(
                                                        dto.latitude,
                                                        dto.longitude
                                                    )
                                                } else {//주인이 중복되는 경우
                                                    latLng = LatLng(
                                                        dto.latitude - 0.0001,
                                                        dto.longitude - 0.0001
                                                    )
                                                }
                                                val marker = visibleOnMapMap.get(dto.dogId)
                                                marker!!.position = latLng
                                                updateCoordinateMap.put(
                                                    dto.dogId,
                                                    marker
                                                ) //서버에서 불러온 정보이므로 지도에 표시된 여부와 상관없이 추가해야함(불러온 정보가 모두 추가됨)
                                            }
                                        }
                                    }

                                    //산책 종료한사람 삭제
                                    visibleOnMapMap.forEach { (key, value) ->
                                        if (!updateCoordinateMap.containsKey(key)) {//불러온 updateCoordinateMap에 visibleOnMapMap의 값이 없을때 -> 산책 종료
                                            CoroutineScope(Dispatchers.Main).launch {
                                                value.map = null //지도에서 표시안함
                                                visibleOnMapMap.remove(key) //지도에 표시되는 마커를 관리하는 맵에서 제거
                                                duplicateUserDog.remove(key) //중복되는 유저 삭제
                                                aroundUserMap.remove(key) //개, 유저 매핑에서 삭제
                                            }
                                        }
                                    }

                                    //지도에 표시된 개의 마커의 위치를 기준으로 bounds안에 들어와있는지 확인
                                    if (isStart && bounds != null) {
                                        visibleOnMapMap.forEach { markerId ->
                                            val markerPosition = markerId.value.position
                                            if (bounds.contains(markerPosition)) {//좌표가 영역 안에 포함될경우
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    Toast.makeText(
                                                        context,
                                                        "개 접근중",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                Log.d(TAG, "피해요!!!!!")
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<List<UserCoordinateDogDto>>,
                                t: Throwable
                            ) {
                                Log.d(TAG, "getWalkingList fail", t)
                            }
                        })
                    //1초간 대기
                    delay(1000L)
                }//if
            }//while
        }//launch
    }

    private fun setDogImage(dogBreed: DogBreed): OverlayImage {
        if (dogBreed.equals(DogBreed.BIG)) {
            return dog1
        } else if (dogBreed.equals(DogBreed.MEDIUM)) {
            return dog2
        } else{
            return dog3
        }
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup
        startWalkButton = view.findViewById(R.id.startWalkButton)
        walkTimeTV = view.findViewById(R.id.walkTimeTV)
        walkDistanceTV = view.findViewById(R.id.walkDistanceTV)
        pauseButton = view.findViewById(R.id.pauseButton)
        statusLayout = view.findViewById(R.id.statusLayout)
        frame = view.findViewById(R.id.map)
        webView = view.findViewById(R.id.webView)
        return view
    }

    private fun setBundle() {
        var bundle: Bundle = Bundle()
        bundle.putSerializable("arraylist", LatLngToSerial())
        bundle.putSerializable("walkDistance", walkDistance)
        bundle.putSerializable("time", strTime)
        bundle.putSerializable("startTime", startTime)
        setFragmentResult("walkEnd", bundle)
    }


    private fun createWebView() {
        var url =
            BuildConfig.SERVER + "map?latitude=${lastLocation.latitude}&longitude=${lastLocation.longitude}"
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript("javascript:getLocation()") {
                        Log.d(TAG, it)
                        tile = it.replace("\"","")
                    }
                }
            }
            settings.javaScriptEnabled = true
        }
        webView.clearCache(true)
        webView.clearHistory()
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.loadUrl(url)
    }

    fun LatLngToSerial(): ArrayList<SerialLatLng> {
        var tempList = ArrayList<SerialLatLng>()
        var iterator = pathList.iterator()
        while (iterator.hasNext()) {
            var temp = SerialLatLng(iterator.next())
            tempList.add(temp)
        }
        return tempList
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    fun startWalk() {
        isStart = true
        pathList.add(LatLng(lastLocation))//시작위치 지정
        startTime = LocalDateTime.now()//시작시간 지정
        statusLayout.visibility = View.VISIBLE
        startWalkButton.visibility = View.GONE
        frame.layoutParams.height = 0
        startTimer()
    }

    fun startTimer() {
        timer = kotlin.concurrent.timer(period = 1000) {
            time++
            CoroutineScope(Dispatchers.Main).launch {
                setTimer()
            }
        }
    }

    fun stopTimer() {
        timer.cancel()
    }

    fun resetTimer() {
        timer.cancel()
        time = 0
        setTimer()
    }

    fun setTimer() {
        var hour = TimeUnit.SECONDS.toHours(time)
        var minute = TimeUnit.SECONDS.toMinutes(time) - hour * 60
        var second = TimeUnit.SECONDS.toSeconds(time) - hour * 3600 - minute * 60
        strTime = String.format("%02d", hour) + " : " + String.format(
            "%02d",
            minute
        ) + " : " + String.format("%02d", second)
        walkTimeTV.text = strTime
    }

    fun pathOverlaySettings() {
        pathOverlay.outlineWidth = 0//테두리 없음
        pathOverlay.width = 20//경로선 폭
        pathOverlay.passedColor = Color.RED//지나온 경로선
        pathOverlay.color = Color.GREEN//경로선 색상
    }

    fun endWalk() {
        pathList.clear()
        pathOverlay.map = null
        walkDistance = 0.0
        //서버에는 time 전달
        statusLayout.visibility = View.GONE
        startWalkButton.visibility = View.VISIBLE

        val layout: ViewGroup.LayoutParams = frame.layoutParams
        layout.width = ViewGroup.LayoutParams.MATCH_PARENT
        layout.height = ViewGroup.LayoutParams.MATCH_PARENT
        frame.layoutParams = layout
        resetTimer()
        walkDistance = 0.0
        walkDistanceTV.text="0M"
    }



    fun uiSettings() {
        //naverMap.uiSettings.isCompassEnabled=true
        naverMap.uiSettings.isLocationButtonEnabled = true//현재위치 버튼 여부
        naverMap.uiSettings.isZoomControlEnabled = false//줌 버튼 여부
    }

    fun setlocationOverlay(): LocationOverlay {
        var locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.icon = overlayImage
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
        //위치 업데이트될때의 리스너
        //bearing업데이트일때도 여기로 들어옴
        naverMap.addOnLocationChangeListener { location ->
            if (naverMap.locationTrackingMode == LocationTrackingMode.NoFollow) {
                locationOverlay.bearing = 0f
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

            if (location == lastLocation) {//각도업데이트일때

            } else {//위치업데이트일때
                if (isStart) {//산책을 시작했다면
                    //pathOverlay.map=null
                    var updateLocation: LatLng = LatLng(location)
                    walkDistance += updateLocation.distanceTo(pathList.last())//마지막 위치와 현재 위치의 거리차이 저장
                    walkDistanceTV.text = walkDistance.toInt().toString() + " M"
                    pathList.add(updateLocation)
                    pathOverlay.coords = pathList
                    pathOverlay.map = naverMap

                } else {
                    //textView.text = "이동거리 0M"
                }
                Log.d(TAG, "위치업데이트")
                lastLocation = location
            }
            //현재 내 위치 기준 영역 저장
            bounds = setBounds(location, width)
        }
    }
    fun setBounds(location: Location, width: Double) :LatLngBounds {
        var southWest = LatLng(location.latitude - width, location.longitude - width)
        var northEast = LatLng(location.latitude + width, location.longitude + width)
        return LatLngBounds(southWest, northEast)
    }
}
