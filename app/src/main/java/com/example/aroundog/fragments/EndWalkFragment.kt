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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.SerialLatLng
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
import com.naver.maps.map.overlay.PathOverlay
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


class EndWalkFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "ENDWALKFRAGMENT"
    lateinit var pathList:ArrayList<LatLng>
    lateinit var mapFragment:MapFragment
    lateinit var gsonInstance: Gson
    lateinit var retrofit:Retrofit
    lateinit var walkRetrofit:WalkService
    val gson:Gson = Gson()
    lateinit var naverMap:NaverMap
    lateinit var exitButton: ImageButton
    lateinit var userId:Long
    lateinit var time:String
    lateinit var walkDistance:String
    lateinit var startTime:LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapFragment = parentFragmentManager.findFragmentById(R.id.endWalk_container) as MapFragment?
            ?: MapFragment.newInstance().also {
                parentFragmentManager.beginTransaction().add(R.id.endWalk_container, it).commit()
            }

        mapFragment.getMapAsync(this)

        gsonInstance = GsonBuilder().setLenient().create()


        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
        walkRetrofit = retrofit.create(WalkService::class.java)


        var user_info_pref = requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getLong("id",-1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

        hideBottomNavigation(true)

        var timeTV:TextView = view.findViewById(R.id.timeTV)
        var walkDistance_end:TextView = view.findViewById(R.id.walkDistanceTV_end)
        
        setFragmentResultListener("walkEnd"){ key, bundle ->
            var serialLatLngList = bundle.getSerializable("arraylist") as ArrayList<SerialLatLng>
            time = bundle.getSerializable("time") as String
            startTime = bundle.getSerializable("startTime") as LocalDateTime
            walkDistance = round((bundle.getSerializable("walkDistance") as Double)).toString()
            pathList = SerialoLatLng(serialLatLngList)//ArrayList<SerialLatLng>을 ArrayList<LatLng>으로 변경
            timeTV.text="산책시간\n"+time
            walkDistance_end.text = "이동거리\n" + walkDistance + "m"
        }
        return view
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        var pathOverlay:PathOverlay = PathOverlay()
        //pathList가 1일 경우 에러
        if(pathList.size < 2){
            pathList.add(pathList[0])
        }
        pathOverlay.coords=pathList
        pathOverlay.outlineWidth=0//테두리 없음
        pathOverlay.width=20//경로선 폭
        pathOverlay.passedColor = Color.RED//지나온 경로선
        pathOverlay.color= Color.GREEN//경로선 색상
        pathOverlay.map=naverMap

        var bounds:LatLngBounds = pathOverlay.bounds//그려진 오버레이의 영역 리턴
        var cameraUpdate:CameraUpdate = CameraUpdate.fitBounds(bounds,100)//오버레이가 다 보일수 있게 카메라 이동시키는 CameraUpdate 생성

        naverMap.moveCamera(cameraUpdate)//카메라 이동

        exitButton.setOnClickListener{

            naverMap.takeSnapshot(false){
                sendToDB(it)
                Log.d(TAG, "실행됨")
                requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            }

        }
    }

    fun sendToDB(bitmap: Bitmap){
        thread(start = true){
            if(userId == -1L){
                Toast.makeText(requireContext(),"아이디 에러입니다.",Toast.LENGTH_SHORT).show()
                return@thread
            }

            val date = LocalDateTime.now()
            val endTimeFormat = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val startTimeFormat = startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

            var file: File = getImage(endTimeFormat, bitmap) //이미지 생성
            var params = getParams(startTimeFormat, endTimeFormat) //파라미터 설정
            var image: MultipartBody.Part = getParamImage(file) //이미지 설정

            walkRetrofit.addWalk(1L, params, image).enqueue(object:Callback<Void>{
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

    private fun getParamImage(file: File): MultipartBody.Part {
        var requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var image: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        return image
    }

    private fun getParams(
        startTimeFormat: String?,
        endTimeFormat: String?
    ): HashMap<String, RequestBody> {

        val firstLatLng: LatLng = pathList.get(0)
        val lastLatLng: LatLng = pathList.get(pathList.size-1)
        val historyCenter:String = "{\"latitude\":" + (firstLatLng.latitude + lastLatLng.latitude)/2 + ",\"longitude\":" + (firstLatLng.longitude + lastLatLng.longitude)/2 + "}"


        var history: String = gson.toJson(pathList)
        var course: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), history)
        var courseCenter: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), historyCenter)
        var startTime: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), startTimeFormat)
        var endTime: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), endTimeFormat)

        var params = HashMap<String, RequestBody>()
        params.put("course", course)
        params.put("courseCenter", courseCenter)
        params.put("startTime", startTime)
        params.put("endTime", endTime)
        return params
    }

    fun SerialoLatLng(list:ArrayList<SerialLatLng>):ArrayList<LatLng>{
        var temp = ArrayList<LatLng>()
        val iterator = list.iterator()
        while(iterator.hasNext()){
            temp.add(iterator.next().latLng)
        }
        return temp
    }
    fun hideBottomNavigation(set:Boolean){
        var bottomNavigationView:BottomNavigationView = activity?.findViewById(R.id.bottom_nav) as BottomNavigationView
        if(set)
            bottomNavigationView.visibility = View.GONE
        else
            bottomNavigationView.visibility = View.VISIBLE

    }
}