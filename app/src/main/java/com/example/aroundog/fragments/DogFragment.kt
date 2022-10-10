package com.example.aroundog.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dogDto = it.getSerializable("dog") as DogDto
            Log.d("DOGFRAGMENT", "$dogDto")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        profileDogNameTV.text = dogDto?.dogName
        profileDogGenderTV.text = dogDto?.dogGender.toString()
        profileDogAgeTV.text = dogDto?.dogAge.toString()
        profileDogHeightTV.text = dogDto?.dogHeight.toString()
        profileDogWeightTV.text = dogDto?.dogWeight.toString()
        profileDogBreedTV.text = dogDto?.breed.toString()

        if (dogDto!!.dogImgList.isNullOrEmpty()) {
            var error = arrayListOf<ImgDto>()
            error.add(ImgDto("error", "error"))
            imgViewPager.adapter = DogSliderAdapter(error)
        }else{
            imgViewPager.adapter = DogSliderAdapter(dogDto!!.dogImgList)
        }


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(dog:DogDto) =
            DogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("dog", dog)
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

        return view
    }
}