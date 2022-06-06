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
import androidx.fragment.app.setFragmentResultListener
import com.example.aroundog.BuildConfig
import com.example.aroundog.Model.UpdateWalkHistory
import com.example.aroundog.R
import com.example.aroundog.SerialLatLng
import com.example.aroundog.Service.RetrofitService
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
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class EndWalkFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "ENDWALKFRAGMENT"
    lateinit var pathList:ArrayList<LatLng>
    lateinit var mapFragment:MapFragment
    lateinit var gsonInstance: Gson
    lateinit var retrofit:Retrofit
    lateinit var retrofitAPI:RetrofitService
    val gson:Gson = Gson()
    lateinit var naverMap:NaverMap
    lateinit var exitButton: ImageButton
    var userId:String = "admin"

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
        retrofitAPI = retrofit.create(RetrofitService::class.java)


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

        setFragmentResultListener("walkEnd"){ key, bundle ->
            var serialLatLngList = bundle.getSerializable("arraylist") as ArrayList<SerialLatLng>
            pathList = SerialoLatLng(serialLatLngList)//ArrayList<SerialLatLng>을 ArrayList<LatLng>으로 변경
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
                Log.d("sex", "실행됨")
                requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            }
        }
    }

    fun sendToDB(bitmap: Bitmap){
        thread(start = true){
            val now = System.currentTimeMillis()
            val date = Date(now)
            val formatDate = SimpleDateFormat("yyyyMMddkkmmss", Locale("ko", "KR"))
            val strDate = formatDate.format(date)
            val fileName = strDate + userId + ".jpg"

            var file:File = File(requireContext().cacheDir, fileName)
            file.createNewFile()
            var bos:ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,90, bos)
            var bitmapdata:ByteArray = bos.toByteArray()

            try {
                var fos:FileOutputStream = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()

            }catch (e:Exception){

            }

            var requestFile:RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            var body:MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file", file.name, requestFile)

            var history:String = gson.toJson(pathList)


            var requestId:RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), userId)
            var requestHistory:RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), history)

            retrofitAPI.postWalkHistory(requestId, requestHistory, body).enqueue(object : Callback<UpdateWalkHistory> {
                override fun onResponse(call: Call<UpdateWalkHistory>, response: Response<UpdateWalkHistory>) {
                    if(response.isSuccessful){
                        Log.d(TAG, "성공적" + response.body()!!.result)
                    }else{
                        Log.d(TAG, "실패 " + call.toString() + "/"+ response.body().toString())
                    }
                }
                override fun onFailure(call: Call<UpdateWalkHistory>, t: Throwable) {
                    Log.d(TAG, t.message.toString())
                }
            })
        }

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