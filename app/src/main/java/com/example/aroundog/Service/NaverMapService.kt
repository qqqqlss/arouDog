package com.example.aroundog.Service
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.aroundog.MainActivity
import com.example.aroundog.PermissionSupport
import com.example.aroundog.R
import com.example.aroundog.utils.*
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationSource

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>


class NaverMapService():LifecycleService(){
    //https://github.com/HanYeop/RunWithMe
    //https://hanyeop.tistory.com/

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // NotificationCompat.Builder 주입
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    // NotificationCompat.Builder 수정하기 위함
    lateinit var currentNotificationBuilder : NotificationCompat.Builder

    var serviceKilled = false

    val TAG = "NaverMapService"

    companion object{
        val isTracking = MutableLiveData<Boolean>() // 위치 추적 상태 여부
        val pathPoints = MutableLiveData<Polylines>() // LatLng = 위도,경도
//        val timeRunInMillis = MutableLiveData<Long>() // 뷰에 표시될 시간
        var isFirstRun = false // 처음 실행 여부 (false = 실행되지않음)
//        val sumDistance = MutableLiveData<Float>(0f)
        val defaultLatLng = MutableLiveData<LatLng>()
        val locationService = LocationService()
    }

    // 초기화
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
//        timeRunInSeconds.postValue(0L)
//        timeRunInMillis.postValue(0L)
//        sumDistance.postValue(0f)
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this,MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_IMMUTABLE)
        baseNotificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.dog1)
            .setContentTitle("달리기 기록을 측정중입니다.")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
        currentNotificationBuilder = baseNotificationBuilder

        postInitialValues()

        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // 위치 추적 상태가 되면 업데이트 호출
        isTracking.observe(this){
            updateLocation(it) //위치 업데이트
            updateNotificationTrackingState(it) //알림창
        }
    }
    // 서비스가 종료 되었을 때
    private fun killService(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        serviceKilled = true
        isFirstRun = false
        pauseService()
//        startTime = 0L
//        pauseLastLatLng = LatLng(0.0,0.0)
//        stopLastLatLng = LatLng(0.0,0.0)
//        pauseLast = false
//        count = 1

        postInitialValues()
        stopForeground(true)
        stopSelf()

    }
    // 서비스 정지
    private fun pauseService(){
        isTracking.postValue(false)
//        isTimerEnabled = false
    }


    // 위치정보 수신하여 addPathPoint 로 추가
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if(isTracking.value!!) {
                p0?.locations?.let { locations ->
                    for(location in locations) {
                        Log.d(TAG, "location : $location")
                        locationService.listener?.onLocationChanged(location)
                        addPathPoint(location)
                        Log.d(TAG, "저장됨 : ${location.latitude} , ${location.longitude}")
                    }
                }
            }
//            else{
//                p0?.locations?.let { locations ->
//                    for(location in locations) {
//                        // 처음 시작 때 위치 초기화
//                        if(!isFirstRun) {
//                            defaultLatLng.postValue(LatLng(location.latitude, location.longitude))
//                            pauseLastLatLng = LatLng(location.latitude, location.longitude)
//                        }
//                        stopLastLatLng = LatLng(location.latitude, location.longitude)
//                        resumeRunning()
//                    }
//                }
//            }
        }
    }

    // 위치 정보 요청하기
    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {
        Log.d(TAG, "updateLocation")
        if (isTracking) {
            Log.d(TAG, "updateLocation is true")
            if (PermissionSupport.hasLocationPermissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL // 위치 업데이트 주기
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL // 가장 빠른 위치 업데이트 주기
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 배터리소모를 고려하지 않으며 정확도를 최우선으로 고려
                    maxWaitTime = LOCATION_UPDATE_INTERVAL // 최대 대기시간

                }
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        } else {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    // 위치정보 추가
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
                distancePolyline()
            }
        }

    }
    // 거리 표시 (마지막 전, 마지막 경로 차이 비교)
    private fun distancePolyline(){
        if(pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().size > 1){
            val preLastLatLng = pathPoints.value!!.last()[pathPoints.value!!.last().size - 2] // 마지막 전 경로
            val lastLatLng = pathPoints.value!!.last().last() // 마지막 경로

            // 이동거리 계산
            val result = FloatArray(1)
            Location.distanceBetween(
                preLastLatLng.latitude,
                preLastLatLng.longitude,
                lastLatLng.latitude,
                lastLatLng.longitude,
                result
            )

            // 비동기
//            sumDistance.postValue(sumDistance.value!!.plus(result[0]))

            Log.d(TAG, "distancePolyline: ${result[0]}")

        }
    }
    // 알림창 버튼 생성, 액션 추가
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        // 서비스 종료상태가 아닐 때
        if(!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    // 서비스가 호출 되었을 때
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                // 시작, 재개 되었을 때
                ACTION_START_OR_RESUME_SERVICE ->{
//                    if(!isFirstRun){
                    Log.d(TAG, "서비스 시작")
                        startForegroundService()
                        isFirstRun = true
                    }
//                else{
//                        startTimer()
//                        ttsSpeak("러닝을 다시 시작합니다.")
//                    }
//                    startTime = System.currentTimeMillis()
//                }
                // 중지 되었을 때
                ACTION_PAUSE_SERVICE ->{
//                    ttsSpeak("러닝이 일시 중지되었습니다.")
//                    pauseService()
                }
                // 종료 되었을 때
                ACTION_STOP_SERVICE ->{
//                    ttsSpeak("러닝이 종료되었습니다.")
                    killService()
                }
                // 처음 화면 켰을 때
                ACTION_SHOW_RUNNING_ACTIVITY ->{
                    updateLocation(true)
                }
                else -> null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    // Notification 등록, 서비스 시작
    private fun startForegroundService(){
        startTimer()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())
    }

    // 채널 만들기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW // 알림음 없음
        )
        notificationManager.createNotificationChannel(channel)
    }
    // 타이머 시작
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
//        timeStarted = System.currentTimeMillis()
//        isTimerEnabled = true
//
//        CoroutineScope(Dispatchers.Main).launch {
//            // 위치 추적 상태일 때
//            while (isTracking.value!!){
//                // 현재 시간 - 시작 시간 => 경과한 시간
//                lapTime = System.currentTimeMillis() - timeStarted
//                // 총시간 (일시정지시 저장된 시간) + 경과시간 전달
//                timeRunInMillis.postValue(totalTime + lapTime)
//                // 알림창에 표시될 시간 초 단위로 계산함
//                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
//                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
//                    lastSecondTimestamp += 1000L
//                }
//                delay(TIMER_UPDATE_INTERVAL)
//            }
//            // 위치 추적이 종료(정지) 되었을 때 총시간 저장
//            totalTime += lapTime
//        }
    }
    // 빈 polyline 추가
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // null 이라면 초기화

    class LocationService():LocationSource {
        var listener: LocationSource.OnLocationChangedListener? = null
        override fun activate(listener: LocationSource.OnLocationChangedListener) {
            this.listener = listener
        }

        override fun deactivate() {
        }
    }
}

