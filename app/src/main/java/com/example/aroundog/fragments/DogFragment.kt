package com.example.aroundog.fragments

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.aroundog.Model.DogSliderAdapter
import com.example.aroundog.R
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.ImgDto
import java.io.InputStream
import java.util.*


class DogFragment : Fragment() {
    private var dogDto:DogDto? = null


    lateinit var imgViewPager:ViewPager2
    lateinit var profileDogNameTV:TextView
    lateinit var profileDogGenderTV:TextView
    lateinit var profileDogAgeTV:TextView
    lateinit var profileDogHeightTV:TextView
    lateinit var profileDogWeightTV:TextView
    lateinit var profileDogBreedTV:TextView
    lateinit var profileDogInfoLayout:LinearLayout
    var hasDog = false
    private val DEFAULT_GALLERY_REQUEST_CODE =200
    lateinit var listener: DogSliderAdapter.ItemClickListener
    lateinit var clickView:View
    lateinit var dogImgList:MutableList<ImgDto>
    lateinit var adapter: DogSliderAdapter
    var clickPosition: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasDog = it.getBoolean("hasDog")
            if(hasDog){//등록된 강아지가 있는 경우
                dogDto = it.getSerializable("dog") as DogDto
                dogImgList = dogDto!!.dogImgList
                Log.d("DOGFRAGMENT", "$dogDto")
            }
            else{//강아지가 없는 경우 초기화
                dogImgList = mutableListOf()
            }
        }
        
        //강아지 추가 이미지에 사용할 원클릭 리스너
        listener = object:DogSliderAdapter.ItemClickListener{
            override fun onItemClicked(view: View, position:Int) {
                clickView = view
                clickPosition = position
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.setType("image/")
                startActivityForResult(intent, DEFAULT_GALLERY_REQUEST_CODE)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        if(hasDog){//강아지가 있는 경우
            profileDogNameTV.text = dogDto?.dogName
            profileDogGenderTV.text = dogDto?.dogGender.toString()
            profileDogAgeTV.text = dogDto?.dogAge.toString()
            profileDogHeightTV.text = dogDto?.dogHeight.toString()
            profileDogWeightTV.text = dogDto?.dogWeight.toString()
            profileDogBreedTV.text = dogDto?.breed.toString()

            //dogDto.dogId != null && dogDto.dogImgList == null : 개는 있는데 사진이 없음
            if (dogDto!!.dogImgList.isNullOrEmpty()) {
                dogImgList.add(ImgDto(-100, "emptyImg", "emptyImg"))
                adapter = DogSliderAdapter(dogImgList)

            }else{//강아지와 사진 다 있음
                dogImgList.add(ImgDto(-100, "emptyImg", "emptyImg"))//마지막에 사진 추가 이미지
                adapter  = DogSliderAdapter(dogImgList)
            }

        }else{//강아지가 없는 경우
            dogImgList.add(ImgDto(-200,"emptyDog", "emptyDog"))
            adapter = DogSliderAdapter(dogImgList)
            profileDogInfoLayout.visibility = View.INVISIBLE
        }
        adapter.adapterListener = listener
        imgViewPager.adapter = adapter

        return view
    }

    companion object {
        @JvmStatic
        fun newInstanceWithDog(dog:DogDto) =
            DogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("dog", dog)
                    putBoolean("hasDog", true)
                }
            }

        @JvmStatic
        fun newInstanceAddDog() =
            DogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("hasDog", false)
                }
            }
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_dog, container, false) as ViewGroup

        imgViewPager = view.findViewById(R.id.imgViewPager)
        profileDogNameTV = view.findViewById(R.id.profileDogNameTV)
        profileDogGenderTV = view.findViewById(R.id.profileDogGenderTV)
        profileDogAgeTV = view.findViewById(R.id.profileDogAgeTV)
        profileDogHeightTV = view.findViewById(R.id.profileDogHeightTV)
        profileDogWeightTV = view.findViewById(R.id.profileDogWeightTV)
        profileDogBreedTV = view.findViewById(R.id.profileDogBreedTV)
        profileDogInfoLayout = view.findViewById(R.id.profileDogInfoLayout)

        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            DEFAULT_GALLERY_REQUEST_CODE -> {
                data ?: return

                //갤러리에서 고른 사진의 uri
                var photo = data.data as Uri

                //byte[] 로 변경
                try {
                    var photoArr = context!!.contentResolver.openInputStream(photo)?.buffered()
                        ?.use { it.readBytes() }

                    //인코딩
                    var encodingStr = Base64.getEncoder().encodeToString(photoArr)

                    //retrofit 추가 - id 리턴
                    //리턴받은 id, path 설정


                    //클릭한 이미지 삭제(강아지 사진 추가 이미지)
                    dogImgList.removeAt(clickPosition!!)
                    adapter.notifyItemRemoved(clickPosition!!)

                    //이미지 추가
                    var imgDto = ImgDto(1000L, "test", encodingStr)

                    dogImgList.add(clickPosition!!, imgDto)

                    //마지막 위치에 강아지 사진 추가 이미지 추가
                    if (dogImgList.last().id != -100L) {
                        dogImgList.add(ImgDto(-100, "emptyImg", "emptyImg"))
                        adapter.notifyItemInserted(dogImgList.lastIndex)
                    }
                    adapter.notifyItemInserted(clickPosition!!)
                } catch (e:Exception) {
                    e.printStackTrace()
                }

            }
            else -> {
                Toast.makeText(context, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}