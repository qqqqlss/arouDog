package com.example.aroundog.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
    lateinit var profileButtonLayout: LinearLayout
    var idList = arrayListOf<Int>()

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        profileUserNameTV.text = userName

        addDogFragments()

        return view
    }

    private fun addDogFragments() {
        //dogList의 첫번째 원소를 이용한 강아지 프래그먼트 추가
        var initFragment = DogFragment.newInstance(dogList[0])
        childFragmentManager.beginTransaction()
            .replace(R.id.dogInfoFragment, initFragment, dogList[0].dogId.toString())
            .commitAllowingStateLoss()

        //dogList 전체 추가
        dogList.forEach { dogDto ->
            idList.add(dogDto.dogId.toInt())//아이디 리스트에 추가(클릭 리스너에서 사용)
            var dogFragment = DogFragment.newInstance(dogDto)//프래그먼트 생성
            var style = ContextThemeWrapper(context, R.style.borderLessButton)
            var button = Button(style, null, R.style.borderLessButton).apply {
                text = dogDto.dogName
                id = dogDto.dogId.toInt()

                //버튼 id == 프래그먼트 태그
                setOnClickListener { view ->
                    if (childFragmentManager.findFragmentByTag(view.id.toString()) != null) {//해당 태그를 가진 프래그먼트가 있을때
                        childFragmentManager.beginTransaction()
                            .show(childFragmentManager.findFragmentByTag(view.id.toString())!!)
                            .commit()
                    } else {
                        childFragmentManager.beginTransaction()
                            .add(R.id.dogInfoFragment, dogFragment, view.id.toString())
                            .commit()
                    }

                    //다른 프래그먼트 hide
                    for (id in idList) {
                        if (view.id != id) {//자신은 제외
                            if (childFragmentManager.findFragmentByTag(id.toString()) != null) {
                                childFragmentManager.beginTransaction()
                                    .hide(childFragmentManager.findFragmentByTag(id.toString())!!)
                                    .commit()
                            }
                        }
                    }
                }
            }
            profileButtonLayout.addView(button)
        }
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_profile, container, false) as ViewGroup

        profileUserNameTV = view.findViewById(R.id.profileUserNameTV)
        profileUserConfig = view.findViewById(R.id.profileUserConfig)
        profileButtonLayout = view.findViewById(R.id.profileButtonLayout)

        return view
    }
}