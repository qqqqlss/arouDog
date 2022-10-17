package com.example.aroundog.fragments

import android.content.DialogInterface
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aroundog.AddDogActivity
import com.example.aroundog.BuildConfig
import com.example.aroundog.MainActivity2
import com.example.aroundog.R
import com.example.aroundog.Service.DogService
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.WalkWeekSummaryDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.textColor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class ProfileFragment : Fragment() {
    val TAG = "PROFILEFRAGMENT"
    lateinit var userName:String
    lateinit var dogList:MutableList<DogDto>
    lateinit var profileUserNameTV:TextView
    lateinit var profileUserConfig:Button
    lateinit var profileButtonLayout: LinearLayout
    var idList = mutableListOf<Int>()

    lateinit var retrofit:Retrofit
    lateinit var walkService:WalkService
    lateinit var dogService:DogService
    lateinit var userId:String

    lateinit var profileTotalMinuteTV:TextView
    lateinit var profileTotalDistanceTV:TextView
    lateinit var profileTotalCountTV:TextView
    var hasDog:Boolean = false
    var buttonList = mutableListOf<Button>()
    lateinit var style:ContextThemeWrapper

//    https://greensky0026.tistory.com/224
    init{

        //AddDogActivity의 newDogData값이 변경될 때 실행
        AddDogActivity.newDogData.observe(this){
            dogList.add(it)
            val dogFragment = addNewDog(it)

            //리스트 마지막, 마지막-1 변경(+버튼과 추가한 강아지 인덱스 변경)
            changeIndex(buttonList as MutableList<Any>)
            changeIndex(idList as MutableList<Any>)

            //버튼 전체 삭제
            profileButtonLayout.removeAllViews()

            //버튼 리스트의 순서대로 버튼 추가
            for (button in buttonList) {
                profileButtonLayout.addView(button)
            }

            //추가된 강아지 프래그먼트 추가
            childFragmentManager.beginTransaction()
                .add(R.id.dogInfoFragment, dogFragment, buttonList[buttonList.lastIndex-1].id.toString())
                .commit()
            //강아지 추가 프래그먼트 숨기기
            childFragmentManager.beginTransaction().hide(childFragmentManager.findFragmentByTag("-1")!!).commit()
            
            //추가한 강아지 버튼 텍스트 스타일 변경
            clickButton(buttonList.lastIndex - 1)

            //강아지 추가 버튼 스타일 변경
            var button = buttonList.last()
            button.textColor = resources.getColor(R.color.lightGray)
            button.setTypeface(Typeface.create(button.typeface, Typeface.NORMAL))//BOLD할때처럼 하면 적용 안딤
            button.textSize = 14F
        }
    }

    /**
     * @param MutableList 인덱스를 변경할 리스트
     */
    private fun changeIndex(list:MutableList<Any>){
        var lastIndex = list.lastIndex
        var lastObject = list.last()
        var secondFromLast = list[lastIndex-1]
        list[lastIndex] = secondFromLast
        list[lastIndex-1] = lastObject
    }

    private fun addNewDog(newDogData:DogDto):DogFragment {
        var dogFragment = DogFragment.newInstanceWithDog(newDogData)//프래그먼트 생성
        addButton(newDogData.dogName, newDogData.dogId.toInt(), dogFragment)
        return dogFragment
    }

    //    https://greensky0026.tistory.com/224
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var makeGson = GsonBuilder().create()
        var type: TypeToken<MutableList<DogDto>> = object: TypeToken<MutableList<DogDto>>(){}

        //저장된 id 정보 가져오기
        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userName = user_info_pref.getString("userName", "").toString()
        userId = user_info_pref.getString("id", "").toString()


        var dog_info_pref =
            requireActivity().getSharedPreferences("dogInfo", AppCompatActivity.MODE_PRIVATE)
        var listStr = dog_info_pref.getString("dogList", "")
        hasDog = dog_info_pref.getBoolean("hasDog", false)

        dogList = makeGson.fromJson<MutableList<DogDto>>(listStr, type.type)

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()


        walkService = retrofit.create(WalkService::class.java)
        dogService = retrofit.create(DogService::class.java)

        style = ContextThemeWrapper(context, R.style.borderLessButton)
    }

    override fun onResume() {
        super.onResume()
        walkService.getWalkWeekSummary(userId).enqueue(object:Callback<WalkWeekSummaryDto>{
            override fun onResponse(
                call: Call<WalkWeekSummaryDto>,
                response: Response<WalkWeekSummaryDto>
            ) {
                if (response.isSuccessful) {
                    var walkWeekSummaryDto = response.body()
                    profileTotalMinuteTV.text = String.format("%.1f 분",(walkWeekSummaryDto!!.second / 60.0))
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
        clickButton(0)
    }
    
    private fun clickButton(index:Int){
        var firstButton = buttonList[index]
        firstButton.textColor = resources.getColor(R.color.brown)
        firstButton.setTypeface(firstButton.typeface, Typeface.BOLD)
        firstButton.textSize = 20F
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

            if (id != -1) {
                setOnLongClickListener {
                    //다이얼로그 뿌리고 true면 삭제, retrofit 강아지 삭제 통신도 필요(cascade도 해야할듯)
                    val builder = AlertDialog.Builder(view!!.context) //context만 하면 이상하게 나옴
                    builder.setTitle("강아지 삭제")
                        .setMessage(
                            "강아지를 삭제하시겠어요?"
                        )
                        .setPositiveButton("삭제", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                dogService.deleteDog(id.toLong()).enqueue(object:Callback<Boolean>{
                                    override fun onResponse(
                                        call: Call<Boolean>,
                                        response: Response<Boolean>
                                    ) {
                                        if (response.isSuccessful) {
                                            if (response.body() == true) {
                                                profileButtonLayout.removeView(it)

                                                var index = buttonList.indexOf(it)
                                                if (index == 0) {//삭제할게 첫 인덱스면
                                                    //첫 버튼에 클릭된 효과
                                                    clickButton(1)
                                                }
                                                
                                                //buttonList, idList를 추가할때 dogList에서 추가했으므로 셋이 인덱스가 같음
                                                idList.removeAt(index) //idList에서 인덱스를 사용해 id 삭제
                                                buttonList.remove(it) //button에서 버튼 삭제
                                                dogList.removeAt(index)//강아지 삭제

                                                Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show()
                                            } else{
                                                Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                        Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        })
                        .setNegativeButton("취소") { dialog, i ->
                            dialog.dismiss()
                        }
                    val dialog = builder.create()
                    dialog.show()

                    return@setOnLongClickListener true
                }
            }
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
                    button.textSize = 20F
                } else {
                    button.textColor = resources.getColor(R.color.lightGray)
                    button.setTypeface(Typeface.create(button.typeface, Typeface.NORMAL))//BOLD할때처럼 하면 적용 안딤
                    button.textSize = 14F
                }
            }
        }
    }
}