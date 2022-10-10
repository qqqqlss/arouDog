package com.example.aroundog.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.NaverMapService
import com.example.aroundog.Service.Polyline
import com.example.aroundog.Service.WalkService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.MultipartPathOverlay.ColorPart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.Math.round
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class EndWalkFragment : Fragment(){
    private val TAG = "ENDWALKFRAGMENT"
    lateinit var mapFragment:MapFragment
    lateinit var retrofit:Retrofit
    lateinit var walkRetrofit:WalkService
    val gson:Gson = Gson()
    lateinit var naverMap:NaverMap
    lateinit var exitButton: ImageButton
    var userId:String = "error"
    lateinit var time:String
    lateinit var walkDistance:String
    lateinit var startTime:LocalDateTime
    lateinit var pathPoints:MutableList<Polyline>
    lateinit var tile:String
    lateinit var strSecond:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initMapView()//지도생성, 초기화

        initRetrofit()//retrofit 초기화

        //저장된 id 정보 가져오기
        var user_info_pref = requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id","error").toString()

    }

    /**
     * retrofit초기화
     */
    private fun initRetrofit() {
        var gsonInstance = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
        walkRetrofit = retrofit.create(WalkService::class.java)
    }

    /**
     * 지도생성, 초기화
     */
    private fun initMapView() {
        mapFragment = parentFragmentManager.findFragmentById(R.id.endWalk_container) as MapFragment?
            ?: MapFragment.newInstance().also {
                parentFragmentManager.beginTransaction().add(R.id.endWalk_container, it).commit()
            }

        mapFragment.getMapAsync { map ->
            this.naverMap = map
            var multipartPath = MultipartPathOverlay()
            val colorList = ArrayList<ColorPart>()
            val colorPart = ColorPart(
                Color.RED,   // 지나갈 경로선의 선 색상을 빨간색으로 지정
                Color.WHITE, // 지나갈 경로선의 테두리 색상을 흰색으로 지정
                Color.GRAY,  // 지나온 경로선의 선 색상을 회색으로 지정
                Color.LTGRAY // 지나온 경로선의 테두리 색상을 밝은 회색으로 지정
            )
            for (i in 0 until pathPoints.size step 1) {
                colorList.add(colorPart)
            }
            multipartPath.coordParts = pathPoints
            multipartPath.colorParts = colorList
            multipartPath.map = naverMap

            var bounds: LatLngBounds = multipartPath.bounds//그려진 오버레이의 영역 리턴
            var cameraUpdate: CameraUpdate =
                CameraUpdate.fitBounds(bounds, 100)//오버레이가 다 보일수 있게 카메라 이동시키는 CameraUpdate 생성

            naverMap.moveCamera(cameraUpdate)//카메라 이동
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                takeSnapshotAndSendDB()
            }
        })
    }

    /**
     * 모든 경로를 포함하는 스냅샷 생성, db에 산책 정보 전송
     */
    private fun takeSnapshotAndSendDB() {
        naverMap.takeSnapshot(false) { img ->
            sendToDB(img)
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(this@EndWalkFragment)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        hideBottomNavigation(false)
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()//현재 프래그먼트 제거
        parentFragmentManager.beginTransaction().remove(mapFragment).commit()//현재 프래그먼트의 지도 프래그먼트 제거
        requireActivity().supportFragmentManager.popBackStack()//이전 프래그먼트 호출
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view:ViewGroup = inflater.inflate(R.layout.fragment_end_walk, container,false) as ViewGroup
        exitButton = view.findViewById<ImageButton>(R.id.exitButton)

        exitButton.setOnClickListener {
            takeSnapshotAndSendDB()
        }

        hideBottomNavigation(true)

        var timeTV:TextView = view.findViewById(R.id.timeTV)
        var walkDistance_end:TextView = view.findViewById(R.id.walkDistanceTV_end)
        
        setFragmentResultListener("walkEnd"){ key, bundle ->
            pathPoints = bundle.getSerializable("pathPoints") as MutableList<Polyline>
            time = bundle.getSerializable("time") as String
            startTime = bundle.getSerializable("startTime") as LocalDateTime
            walkDistance = round((bundle.getSerializable("walkDistance") as Double)).toString()
            tile = bundle.getSerializable("tile") as String
            strSecond = (bundle.getSerializable("second") as Long).toString()
            timeTV.text = "산책시간\n" + time
            walkDistance_end.text = "이동거리\n" + walkDistance + "m"

            //pathOverlay는 2개 미만이거나 null인 원소 있으면 예외 발생
            for (polyline in pathPoints) {
                if(polyline.size < 2){
                    Toast.makeText(context, "너무 짧아 저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
                }
            }
        }
        return view
    }

    /**
     * 산책 정보를 db에 전송
     */
    fun sendToDB(bitmap: Bitmap){
        thread(start = true){
            if(userId == "error"){
//                Toast.makeText(requireContext(),"아이디 에러입니다.",Toast.LENGTH_SHORT).show()
                return@thread
            }

            val date = LocalDateTime.now()
            val endTimeFormat = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val startTimeFormat = startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

            var file: File = getImage(endTimeFormat, bitmap) //이미지 생성
            var image: MultipartBody.Part = getParamImage(file) //이미지 설정
            var params = setParams(startTimeFormat, endTimeFormat) //파라미터 설정

            walkRetrofit.addWalk(userId, params, image).enqueue(object:Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        Log.d(TAG, "성공적")
                    }else{
                        Log.d(TAG, "실패 ")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d(TAG, "전송 실패 ${t.toString()}")
                }
            })
        }

    }

    /**
     * 이미지 생성
     */
    private fun getImage(endTimeFormat: String, bitmap: Bitmap): File {
        val fileName = endTimeFormat + "#" + userId + ".jpg"
        Log.d(TAG, "fileName : $fileName")

        var file: File = File(requireContext().cacheDir, fileName)
        file.createNewFile()
        var bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
        var bitmapdata: ByteArray = bos.toByteArray()

        try {
            var fos: FileOutputStream = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()

        } catch (e: Exception) {

        }
        return file
    }

    /**
     * 이미지를 multipart/form-data 파라미터 형식으로 변경
     */
    private fun getParamImage(file: File): MultipartBody.Part {
        var requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var image: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        return image
    }

    /**
     * 파라미터 설정
     */
    private fun setParams(
        startTimeFormat: String?,
        endTimeFormat: String?
    ): HashMap<String, RequestBody> {

        var historyCenter = calCenterLatLng()//좌표들 중심좌표 계산

        var history: String = gson.toJson(pathPoints)
        var course: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), history)
        var courseCenter: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), historyCenter.toString())
        var startTime: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), startTimeFormat)
        var endTime: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), endTimeFormat)
        var tile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), tile)
        var second:RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), strSecond)
        var distance:RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), walkDistance)

        var params = HashMap<String, RequestBody>()
        params.put("course", course)
        params.put("courseCenter", courseCenter)
        params.put("startTime", startTime)
        params.put("endTime", endTime)
        params.put("tile", tile)
        params.put("second", second)
        params.put("distance",distance)
        return params
    }

    /**
     * 좌표들의 중심좌표 계산
     */
    private fun calCenterLatLng(): LatLng {
        var centerList = ArrayList<LatLng>()
        for (pathPoint in pathPoints) {
            var firstLatLng = pathPoint.first()
            var lastLatLng = pathPoint.last()
            var center = LatLng(
                (firstLatLng.latitude + lastLatLng.latitude) / 2,
                (firstLatLng.longitude + lastLatLng.longitude) / 2
            )
            centerList.add(center)
        }

        var centerLatitude = 0.0
        var centerLongitude = 0.0
        for (latLng in centerList) {
            centerLatitude += latLng.latitude
            centerLongitude += latLng.longitude
        }
        var historyCenter =
            LatLng(centerLatitude / centerList.size, centerLongitude / centerList.size)
        return historyCenter
    }

    /**
     * 네비게이션바 VISIBLE/GONE 처리
     */
    fun hideBottomNavigation(set:Boolean){
        var bottomNavigationView:BottomNavigationView = activity?.findViewById(R.id.bottom_nav) as BottomNavigationView
        if(set)
            bottomNavigationView.visibility = View.GONE
        else
            bottomNavigationView.visibility = View.VISIBLE

    }
}