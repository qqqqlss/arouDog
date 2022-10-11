package com.example.aroundog.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.WalkWeekSummaryDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ProfileFragment : Fragment() {
    val TAG = "PROFILEFRAGMENT"
    lateinit var userName:String
    lateinit var dogList:List<DogDto>
    lateinit var profileUserNameTV:TextView
    lateinit var profileUserConfig:Button
    lateinit var profileButtonLayout: LinearLayout
    var idList = arrayListOf<Int>()

    lateinit var retrofit: WalkService
    lateinit var userId:String

    lateinit var profileTotalMinuteTV:TextView
    lateinit var profileTotalDistanceTV:TextView
    lateinit var profileTotalCountTV:TextView
    var hasDog:Boolean = false
    var buttonList = arrayListOf<Button>()
    lateinit var style:ContextThemeWrapper

//    https://greensky0026.tistory.com/224
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var makeGson = GsonBuilder().create()
        var type: TypeToken<List<DogDto>> = object: TypeToken<List<DogDto>>(){}

        //저장된 id 정보 가져오기
        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userName = user_info_pref.getString("userName", "").toString()
        userId = user_info_pref.getString("id", "").toString()


        var dog_info_pref =
            requireActivity().getSharedPreferences("dogInfo", AppCompatActivity.MODE_PRIVATE)
        var listStr = dog_info_pref.getString("dogList", "")
        hasDog = dog_info_pref.getBoolean("hasDog", false)

        dogList = makeGson.fromJson<List<DogDto>>(listStr, type.type)

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(WalkService::class.java)

        style = ContextThemeWrapper(context, R.style.borderLessButton)
    }

    override fun onResume() {
        super.onResume()
        retrofit.getWalkWeekSummary(userId).enqueue(object:Callback<WalkWeekSummaryDto>{
            override fun onResponse(
                call: Call<WalkWeekSummaryDto>,
                response: Response<WalkWeekSummaryDto>
            ) {
                if (response.isSuccessful) {
                    var walkWeekSummaryDto = response.body()
                    profileTotalMinuteTV.text = (walkWeekSummaryDto!!.second / 60).toString()
                    profileTotalDistanceTV.text = walkWeekSummaryDto!!.distance.toString() + " M"
                    profileTotalCountTV.text = walkWeekSummaryDto!!.count.toString() + " 회"
                }
            }

            override fun onFailure(call: Call<WalkWeekSummaryDto>, t: Throwable) {
                Log.d(TAG, "retrofit fail", t)
            }

        })
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
        if(hasDog) {//강아지 있을때
            //dogList의 첫번째 원소를 이용한 강아지 프래그먼트 추가
            var initFragment = DogFragment.newInstanceWithDog(dogList[0])
            childFragmentManager.beginTransaction()
                .replace(R.id.dogInfoFragment, initFragment, dogList[0].dogId.toString())
                .commitAllowingStateLoss()


            //dogList 전체 추가
            dogList.forEach { dogDto ->

                var dogFragment = DogFragment.newInstanceWithDog(dogDto)//프래그먼트 생성
                addButton(dogDto.dogName, dogDto.dogId.toInt(), dogFragment)
            }//dogList.forEach
        }

        //강아지 추가 띄우는 프래그먼트
        var addFragment = DogFragment.newInstanceAddDog()
        childFragmentManager.beginTransaction()//프래그먼트 생성(등록된 강아지 없는 경우 버튼을 클릭해야지만 프래그먼트가 생성되기때문에)
            .add(R.id.dogInfoFragment, addFragment,"-1")
            .commit()
        addButton("+", -1, addFragment)

        if (hasDog) {
            childFragmentManager.beginTransaction().hide(addFragment).commit()
        }

        //첫 버튼에 클릭된 효과
        var firstButton = buttonList[0]
        firstButton.textColor = resources.getColor(R.color.brown)
        firstButton.setTypeface(firstButton.typeface, Typeface.BOLD)
        firstButton.textSize = 16F

    }
    
    private fun addButton(
        buttonText: String,
        buttonId:Int,
        fragment: DogFragment
    ) {
        var button = Button(style, null, R.style.borderLessButton).apply {
            buttonList.add(this)
            text = buttonText
            id = buttonId
            textColor = resources.getColor(R.color.lightGray)
            textSize = 14F
            setTypeface(this.typeface, Typeface.NORMAL)
            idList.add(buttonId)//아이디 리스트에 추가(클릭 리스너에서 사용)

            setOnClickListener(ButtonClickListener(fragment))
        }
        profileButtonLayout.addView(button)


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
        profileTotalMinuteTV = view.findViewById(R.id.profileTotalMinuteTV)
        profileTotalDistanceTV = view.findViewById(R.id.profileTotalDistanceTV)
        profileTotalCountTV = view.findViewById(R.id.profileTotalCountTV)


        return view
    }
    inner class ButtonClickListener(var fragment:Fragment):View.OnClickListener{
        override fun onClick(view: View) {
            //button id == 프래그먼트 태그
            if (childFragmentManager.findFragmentByTag(view.id.toString()) != null) {//해당 태그를 가진 프래그먼트가 있을때
                childFragmentManager.beginTransaction()
                    .show(childFragmentManager.findFragmentByTag(view.id.toString())!!)
                    .commit()
            } else {
                childFragmentManager.beginTransaction()
                    .add(R.id.dogInfoFragment, fragment, view.id.toString())
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

            //클릭된 버튼 크기, 색 변경
            for (button in buttonList) {
                if (button == view) {
                    button.textColor = resources.getColor(R.color.brown)
                    button.setTypeface(button.typeface, Typeface.BOLD)
                    button.textSize = 16F
                } else {
                    button.textColor = resources.getColor(R.color.lightGray)
                    button.setTypeface(button.typeface, Typeface.NORMAL)
                    button.textSize = 14F
                }
            }
        }
    }
}