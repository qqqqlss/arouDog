package com.example.aroundog.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.aroundog.Model.DogSliderAdapter
import com.example.aroundog.R
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.ImgDto

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasDog = it.getBoolean("hasDog")
            if(hasDog){//등록된 강아지가 있는 경우
                dogDto = it.getSerializable("dog") as DogDto
                Log.d("DOGFRAGMENT", "$dogDto")
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

            //dogDto.dogId == null 등록된 개가 없는 경우
            //dogDto.dogId != null && dogDto.dogImgList == null : 개는 있는데 사진이 없음
            if (dogDto!!.dogImgList.isNullOrEmpty()) {
                var emptyImg = arrayListOf<ImgDto>()
                emptyImg.add(ImgDto(-100, "emptyImg", "emptyImg"))
                imgViewPager.adapter = DogSliderAdapter(emptyImg)

            }else{
                imgViewPager.adapter = DogSliderAdapter(dogDto!!.dogImgList)
            }

        }else{//강아지가 없는 경우
            var emptyDog = arrayListOf<ImgDto>()
            emptyDog.add(ImgDto(-200,"emptyDog", "emptyDog"))
            imgViewPager.adapter = DogSliderAdapter(emptyDog)
            profileDogInfoLayout.visibility = View.INVISIBLE
        }


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
}