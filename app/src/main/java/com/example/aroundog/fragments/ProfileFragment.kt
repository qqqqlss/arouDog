package com.example.aroundog.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.aroundog.R
import com.example.aroundog.dto.DogDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken


class ProfileFragment : Fragment() {
    val TAG = "PROFILEFRAGMENT"
    lateinit var userName:String
    lateinit var dogList:List<DogDto>
    lateinit var profileUserNameTV:TextView
    lateinit var profileUserConfig:Button
    lateinit var imgViewPager:ViewPager2
    lateinit var profileDogNameTV:TextView
    lateinit var profileDogGenderTV:TextView
    lateinit var profileDogAgeTV:TextView
    lateinit var profileDogHeightTV:TextView
    lateinit var profileDogWeightTV:TextView
    lateinit var profileDogBreedTV:TextView




//    https://greensky0026.tistory.com/224
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    var makeGson = GsonBuilder().create()
    var type: TypeToken<List<DogDto>> = object: TypeToken<List<DogDto>>(){}

    //저장된 id 정보 가져오기
    var user_info_pref =
        requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
    userName = user_info_pref.getString("userName", "").toString()


    var dog_info_pref =
        requireActivity().getSharedPreferences("dogInfo", AppCompatActivity.MODE_PRIVATE)
    var listStr = dog_info_pref.getString("dogList", "")
    dogList = makeGson.fromJson<List<DogDto>>(listStr, type.type)

    var dogFragment = DogFragment.newInstance(dogList[0])
    childFragmentManager.beginTransaction().replace(R.id.dogInfoFragment, dogFragment,"dog").commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)


        profileUserNameTV.text = userName
        return view
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_profile, container, false) as ViewGroup

        profileUserNameTV = view.findViewById(R.id.profileUserNameTV)
        profileUserConfig = view.findViewById(R.id.profileUserConfig)

        return view
    }
}