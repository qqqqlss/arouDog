package com.example.aroundog.fragments

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import com.example.aroundog.BuildConfig
import com.example.aroundog.MainActivty
import com.example.aroundog.Model.DogBreed
import com.example.aroundog.R
import com.example.aroundog.Service.CoordinateService
import com.example.aroundog.Service.NaverMapService
import com.example.aroundog.Service.Polyline
import com.example.aroundog.Util
import com.example.aroundog.dto.UserCoordinateDogDto
import com.example.aroundog.utils.*
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit


class MainFragment : Fragment(){

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var pathList: ArrayList<LatLng> = ArrayList<LatLng>()
    private var pathOverlay: PathOverlay = PathOverlay()
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
    lateinit var cameraButton: ImageButton
    lateinit var statusLayout: LinearLayout
    lateinit var webView: WebView
    lateinit var timer: Timer
    var time: Long = 0
    lateinit var currentPhotoPath: String

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

    lateinit var mainActivty: MainActivty
    var isTracking = false
    var pathPoints = mutableListOf<Polyline>()
    lateinit var locationOverlay:LocationOverlay
    lateinit var imageView:ImageView
    lateinit var boundCoroutine:Job
    //카메라
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        val firstTile = MutableLiveData<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        overlayImage = OverlayImage.fromAsset("logo.png") //위치 오버레이 이미지 초기화


        pathOverlaySettings() //경로선 오버레이 초기화

        // 초기화 전로딩
        Util.progressOnInFragment(this)

        initMapView() //지도생성, 초기화

        initRetrofit()//retrofit초기화

        initDogImage()//강아지 이미지 초기화



        //환경설정에서 알람을 표시할 영역을 설정하는 변수, 저장 필요, 일단 임의값으로 대체
        //소수점 다섯째 자리가 약 1m정도씩 차이남
        width = 0.001 //100m

        //저장된 id 정보 가져오기
        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id", "error").toString()
    }

    init {
        // 위치 추적 여부 관찰하여 updateTracking 호출
        //레이아웃 변경
        NaverMapService.isTracking.observe(this){
            isTracking = it
            //updateTracking(it)
        }
        // 경로 변경 관찰
        NaverMapService.pathPoints.observe(this) {
            pathPoints = it
            addLatestPolyline()
        }
    }

    /**
     * 지도 생성, 초기화
     */
    private fun initMapView() {
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        locationSource.isCompassEnabled = true // 나침반 여부 지정
        
        var mapView = parentFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                parentFragmentManager.beginTransaction().add(R.id.map, it, "map").commit()
            }

        mapView.getMapAsync {
            naverMap = it
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

            uiSettings()//지도 ui세팅
            locationOverlay = setlocationOverlay()//위치 오버레이 설정

            //지도 옵션 변경 리스너
            naverMap.addOnOptionChangeListener {

                val mode = naverMap.locationTrackingMode
                Log.d(TAG, "option change : $mode")

                //None이면 지도에서 마커 없어짐
                if (mode == LocationTrackingMode.None) {
                    naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                }

                //face -> NoFollow면 카메라 현재 위치에서 정북방향으로 회전되게
                if (mode == LocationTrackingMode.NoFollow) {
                    Log.d(TAG, "mode NoFollow")
                    if (this::lastLocation.isInitialized) {
                        if(isTracking){
                            naverMap.cameraPosition =
                                CameraPosition(pathPoints.last().last(), 16.0, 0.0, 0.0)
                        }else{
                            naverMap.cameraPosition =
                                CameraPosition(LatLng(lastLocation), 16.0, 0.0, 0.0)
                        }

                    }
                }
            }

            //위치 업데이트 리스너
            naverMap.addOnLocationChangeListener { location ->
                Log.d(TAG, "location change in MainFragment : $location")

                lastLocation = location
                //NoFollow모드에서는 지도 회전 안되게 변경
                if (naverMap.locationTrackingMode == LocationTrackingMode.NoFollow) {
                    locationOverlay.bearing = 0f
                }

                //지도 첫 로딩시
                if (isFirst) {
                    naverMap.moveCamera(
                        CameraUpdate.scrollAndZoomTo(
                            LatLng(
                                location
                            ), 16.0
                        )
                    )
                    isFirst = false
                    createWebView()
                    Log.d(TAG, "첫번째 위치 업데이트")
                    //초기화 후 로딩 화면 끔
                    Util.progressOffInFragment()
                }

                //산책중일땐 pathPoints의 마지막 위치로 위치 오버레이를 지정함
                if(isTracking){
                    if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
                        locationOverlay.position = pathPoints.last().last()
                    }
                }
            }
        }//getMapAsync
    }//initMapView

    /**
     * Retrofit초기화
     */
    private fun initRetrofit() {
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(CoordinateService::class.java)
    }

    /**
     * 강아지 이미지 초기화
     */
    private fun initDogImage() {
        dog1 = OverlayImage.fromResource(R.drawable.dog1)
        dog2 = OverlayImage.fromResource(R.drawable.dog2)
        dog3 = OverlayImage.fromResource(R.drawable.dog3)
    }

    // 경로 표시 (마지막 전, 마지막 경로 연결)
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 2){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2] // 마지막 전 경로
            val lastLatLng = pathPoints.last().last() // 마지막 경로

            walkDistance += preLastLatLng.distanceTo(lastLatLng)//마지막 위치와 현재 위치의 거리차이 저장
            walkDistanceTV.text = walkDistance.toInt().toString() + " M"
            Log.d(TAG, "walkDistance : $walkDistance")

            //------------------------일시정지 이전거는 출력 안되는 문제 발생할지도?--
            pathOverlay.coords = pathPoints.last()
            pathOverlay.map=naverMap
            locationOverlay.position = lastLatLng//위치 오버레이 위치 지정

            bounds = setBounds(lastLatLng, width)//현재 내 위치 기준 영역 저장
        }
        else{
            if(pathPoints.isNotEmpty()){
                if(pathPoints.last().isEmpty()){//pathPoints.last()가 비어있으면(초기화 되면)
                    //현재 위치 두번 저장(경로 오버레이는 2개 미만이면 오류나기때문
                    pathPoints.last().add(LatLng(lastLocation))
                    pathPoints.last().add(LatLng(lastLocation))
                    bounds = setBounds(LatLng(lastLocation), width)//현재 내 위치 기준 영역 저장
                }
            }
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            statusLayout.visibility = View.VISIBLE
            startWalkButton.visibility = View.GONE
//            imgPause.visibility = View.INVISIBLE
//            imgStart.visibility = View.VISIBLE
//            imgStop.visibility = View.VISIBLE
        }
        else if (isTracking) {
            statusLayout.visibility = View.GONE
            startWalkButton.visibility = View.VISIBLE

//            imgPause.visibility = View.VISIBLE
//            imgStart.visibility = View.GONE
//            imgStop.visibility = View.GONE
        }
    }

    //사진 기능
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString() + "/Pictures",
            "Aroundog"
        )
        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString())
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            val packageManager = activity!!.packageManager
            takePictureIntent.resolveActivity(packageManager)!!.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.android.provider",
                        it!!
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
        galleryAddPic()
    }
    //갤러리 갱신
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            context!!.sendBroadcast(mediaScanIntent)
        }
    }

    override fun onCreateView(//인터페이스를 그리기위해 호출
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: ViewGroup = setView(inflater, container)

        inflater.inflate(R.layout.warning, view, true)
        imageView = view.findViewById<ImageView>(R.id.warning)
        imageView.visibility = View.INVISIBLE

        val fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f)
        fadeIn.repeatCount = -1
        fadeIn.duration = 1500
        fadeIn.repeatMode = ObjectAnimator.REVERSE

        //산책시작 버튼 클릭 리스너
        startWalkButton.setOnClickListener {
            //최신 위치가 저장되었는지 확인
            if (this::lastLocation.isInitialized) {
                startWalk()
                dbProcess()
                createWebView()//웹뷰 생성, tile 값 지정
                sendCommandToService(ACTION_SHOW_RUNNING_ACTIVITY)
                sendCommandToService(ACTION_START_OR_RESUME_SERVICE)

                boundCoroutine = CoroutineScope(Dispatchers.Main).launch {
                    var isWarning = false
                    //안드로이드 버전에 따라 다르게 저장
                    var vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager =
                            context!!.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator;
                    } else {
                        context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
//                    val notification: Uri =
//                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                    val ringtone = RingtoneManager.getRingtone(context,notification)

                    while(true){
                        //위험 개 있을때
                        if(checkBound()){
                            if(!isWarning){//경고중이 아닐때
                                //애니메이션 실행
                                isWarning = true
                                animationStatus(fadeIn, true)
                            }
                            //이미 경고중일때
                            else{
                                //할거없음
                            }

                            //소리, 진동 으로알림(공통)
                            //안드로이드 버전에 따라 다르게
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500,255))
                            } else {
                                vibrator.vibrate(500L)
                            }
//                            ringtone.play()
                        }
                        //위험 개 없을때
                        else{
                            isWarning = false
                            animationStatus(fadeIn, false)
                        }
                        delay(2000)
                    }
                }

            } else {
                Toast.makeText(context, "로딩중입니다. 잠시만 기다려주세요", Toast.LENGTH_SHORT).show()
            }
        }
    
        //카메라 버튼클릭 리스너
        cameraButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

        //산책종료 버튼클릭 리스너
        pauseButton.setOnClickListener {
            stopRun()
            setBundle()//Bundle설정
            endWalk()
            Log.d(TAG, "end walk : $pathPoints")
            parentFragmentManager.beginTransaction()
                .add(R.id.main_container, EndWalkFragment(), "endWalk").addToBackStack(null)
                .commit()

            //insert/update 코루틴 종료
            databaseCoroutine.cancel()

            //영역 확인 코루틴 종료
            boundCoroutine.cancel()
            animationStatus(fadeIn, false)

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

            //-------------------카메라 중심을 마지막 위치로 바꾸는 코드 추가할 것-------------

        }//listener
        return view
    }

    private fun checkBound():Boolean{
        //bound가 초기화 되었다면
        if(this::bounds.isInitialized) {
 //        지도에 표시된 개의 마커의 위치를 기준으로 bounds안에 들어와있는지 확인
            if (isTracking && bounds != null) {
                visibleOnMapMap.forEach { markerId ->
                    val markerPosition = markerId.value.position
                    if (bounds.contains(markerPosition)) {//좌표가 영역 안에 포함될경우
                        return true
                    }
                }
            }
        }
        else{

        }
        return false
    }

    fun animationStatus(fadeIn:ObjectAnimator, boolean:Boolean){
        if(boolean){
            imageView.visibility = View.VISIBLE
            fadeIn.start()
        }else{
            fadeIn.cancel()
            imageView.visibility = View.INVISIBLE
        }
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivty = context as MainActivty
    }

    private fun sendCommandToService(action : String) {
        var intent = Intent(context, NaverMapService::class.java)
        intent.action = action
        if (Build.VERSION.SDK_INT >= 26) {
            context!!.startForegroundService(intent);
        }
        else {
            context!!.startService(intent);
        }
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
        cameraButton = view.findViewById(R.id.cameraButton)
        statusLayout = view.findViewById(R.id.statusLayout)
        frame = view.findViewById(R.id.map)
        webView = view.findViewById(R.id.webView)
        return view
    }

    private fun setBundle() {
        var bundle: Bundle = Bundle()
        bundle.putSerializable("pathPoints", pathPoints as java.io.Serializable)
        bundle.putSerializable("walkDistance", walkDistance)
        bundle.putSerializable("time", strTime)
        bundle.putSerializable("startTime", startTime)
        bundle.putSerializable("tile", tile)
        bundle.putSerializable("second", time)
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
                        firstTile.postValue(tile)
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


    fun startWalk() {
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

    /**
     * 경로선 오버레이 초기화
     */
    fun pathOverlaySettings() {
        pathOverlay.outlineWidth = 5//테두리 없음
        pathOverlay.width = 30//경로선 폭
        pathOverlay.passedColor = Color.rgb(235, 218, 179)//지나온 경로선
        pathOverlay.color = Color.rgb(235, 218, 179)//경로선 색상
        pathOverlay.patternImage = OverlayImage.fromResource(R.drawable.path_pattern) //경로 패턴이미지
        pathOverlay.patternInterval = 40 //경로 패턴 간격
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

    /**
     * 위치 오버레이 설정
     */
    fun setlocationOverlay(): LocationOverlay {
        var locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.icon = overlayImage
        locationOverlay.iconHeight = 100
        locationOverlay.iconWidth = 100

        return locationOverlay
    }

    fun setBounds(latLng: LatLng, width: Double) :LatLngBounds {
        var southWest = LatLng(latLng.latitude - width, latLng.longitude - width)
        var northEast = LatLng(latLng.latitude + width, latLng.longitude + width)
        return LatLngBounds(southWest, northEast)
    }
}
