package com.example.aroundog.fragments

import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.setFragmentResultListener
import com.example.aroundog.R
import com.example.aroundog.SerialLatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay


class EndWalkFragment : Fragment(), OnMapReadyCallback {
    lateinit var pathList:ArrayList<LatLng>
    lateinit var mapFragment:MapFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapFragment = parentFragmentManager.findFragmentById(R.id.endWalk_container) as MapFragment?
            ?: MapFragment.newInstance().also {
                parentFragmentManager.beginTransaction().add(R.id.endWalk_container, it).commit()
            }

        mapFragment.getMapAsync(this)
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
        var exitButton: ImageButton = view.findViewById<ImageButton>(R.id.exitButton)

        exitButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            //requireActivity().supportFragmentManager.popBackStack()
        }

        hideBottomNavigation(true)

        setFragmentResultListener("walkEnd"){ key, bundle ->
            var serialLatLngList = bundle.getSerializable("arraylist") as ArrayList<SerialLatLng>
            pathList = SerialoLatLng(serialLatLngList)//ArrayList<SerialLatLng>을 ArrayList<LatLng>으로 변경
        }
        return view
    }

    override fun onMapReady(naverMap: NaverMap) {
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
        var cameraUpdate:CameraUpdate = CameraUpdate.fitBounds(bounds,10)//오버레이가 다 보일수 있게 카메라 이동시키는 CameraUpdate 생성

        naverMap.moveCamera(cameraUpdate)//카메라 이동

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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