package com.example.aroundog.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aroundog.*
import com.example.aroundog.Model.Gender
import com.example.aroundog.Service.DogService
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.WalkWeekSummaryDto
import com.example.aroundog.utils.UserData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.textColor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class ProfileFragment : Fragment() {
    val TAG = "PROFILEFRAGMENT"
    lateinit var userName:String
    var dogList:MutableList<DogDto> = mutableListOf()//소유 강아지 리스트
    lateinit var profileUserNameTV:TextView
    lateinit var profileUserConfig:ImageButton
    lateinit var profileButtonLayout: LinearLayout

    lateinit var retrofit:Retrofit
    lateinit var walkService:WalkService
    lateinit var dogService:DogService
    lateinit var userId:String

    lateinit var profileTotalMinuteTV:TextView
    lateinit var profileTotalDistanceTV:TextView
    lateinit var profileTotalCountTV:TextView
    lateinit var profileSelectWarningDog:TextView
    lateinit var walkInfo:LinearLayout
    lateinit var logout:TextView

    var hasDog:Boolean = false

    lateinit var style:ContextThemeWrapper

    lateinit var userData:UserData

    lateinit var user_info_pref:SharedPreferences
    lateinit var dog_info_pref:SharedPreferences


    var buttonAndFragmentMap = LinkedHashMap<Button, DogIdAndFragment>()
    var buttonL = mutableListOf<Button>()

    init{

        //AddDogActivity의 newDogData값이 변경될 때 실행
        AddDogActivity.newDogData.observe(this){dogDto->
            //강아지 목록에서 추가
            dogList.add(dogDto)

            //새 버튼 생성
            var newButton = addButton(dogDto.dogName, "button"+dogDto.dogId)
            newButton.setOnClickListener(clickListener)
            newButton.setOnLongClickListener(longClickListener)

            //새 프래그먼트 생성
            var newFragment = DogFragment.newInstanceWithDog(dogDto)
            var daf = DogIdAndFragment(dogDto.dogId, newFragment)

            //마지막 버튼과 마지막 프래그먼트(강아지 추가 화면)
            var lastButton = buttonL.last()
            var lastDaf = buttonAndFragmentMap[lastButton]!!
            buttonL.removeLast()
            buttonAndFragmentMap.remove(lastButton)

            //새 버튼 추가 후 강아지 추가 화면 추가
            add(newButton, daf)
            add(lastButton, lastDaf)

            //뷰에서 버튼 전체 삭제
            profileButtonLayout.removeAllViews()

            //버튼 리스트의 순서대로 버튼 추가
            for (button in buttonL) {
                profileButtonLayout.addView(button)
            }

           //추가 강아지 화면 뷰에 추가
            childFragmentManager.beginTransaction()
                .add(R.id.dogInfoFragment, newFragment, newButton.tag.toString()).commit()

            //새 프래그먼트 보이게
            showFragment(newFragment)

            //새 버튼 클릭 상태
            clickButton(newButton)
        }

        //강아지 정보가 수정되면 강아지 이름 버튼 텍스트 변경
        DogEditActivity.editDogInfo.observe(this){
            for (entry in buttonAndFragmentMap) {
                if (entry.value.dogId == it.dogId) {
                    entry.key.text = it.dogName
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var makeGson = GsonBuilder().create()
        var type: TypeToken<MutableList<DogDto>> = object: TypeToken<MutableList<DogDto>>(){}

        //유저 정보 업데이트
        getSharedUserData()

        dog_info_pref =
            requireActivity().getSharedPreferences("dogInfo", AppCompatActivity.MODE_PRIVATE)
        var listStr = dog_info_pref.getString("dogList", "")
        hasDog = dog_info_pref.getBoolean("hasDog", false)

        if (listStr != "") {
            dogList = makeGson.fromJson<MutableList<DogDto>>(listStr, type.type)
        }

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

        //유저 정보 업데이트
        getSharedUserData()
        profileUserNameTV.text = userName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        profileUserNameTV.text = userName

        profileUserConfig.setOnClickListener {
            val intent = Intent(context, ProfileEditActivity::class.java)
            intent.putExtra("user", userData)
            it.context.startActivity(intent)
        }

        profileSelectWarningDog.setOnClickListener{
            val intent = Intent(context, SelectWarningDog::class.java)
            intent.putExtra("user", userData)
            it.context.startActivity(intent)
        }

        walkInfo.setOnClickListener {
            val intent = Intent(context, ComprehensiveWalkInfoActivity::class.java)
            intent.putExtra("user", userData)
            it.context.startActivity(intent)
        }

        logout.setOnClickListener {
            //다이얼로그 뿌리고 true면 삭제, retrofit 강아지 삭제 통신도 필요(cascade도 해야할듯)
            val builder = AlertDialog.Builder(view!!.context) //context만 하면 이상하게 나옴
            builder.setTitle("로그아웃")
                .setMessage(
                    "로그아웃하시겠습니까?"
                )
                .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        user_info_pref.edit().clear().commit()
                        dog_info_pref.edit().clear().commit()
                        var intent = Intent(context, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                })
                .setNegativeButton("취소") { dialog, i ->
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }

        addDogFragments()

        return view
    }

    /**
     * @param button 버튼
     * @param daf DogIdAndFragment
     */
    fun add(button:Button, daf:DogIdAndFragment) {
        buttonL.add(button)
        buttonAndFragmentMap[button] = daf
    }

    /**
     * @param button 버튼
     * @return 삭제된 인덱스
     */
    fun delete(button:Button):Int {
        var index = buttonL.indexOf(button)
        buttonL.remove(button)
        val fragment = buttonAndFragmentMap.get(button)!!.fragment
        childFragmentManager.beginTransaction().remove(fragment).commit()
        return index
    }

    private fun addDogFragments() {
        if(hasDog) {//강아지 있을때

            if (dogList != null) {
                dogList.forEach { dogDto ->
                    var button = addButton(dogDto.dogName, "button"+dogDto.dogId)
                    button.setOnClickListener(clickListener)
                    button.setOnLongClickListener(longClickListener)

                    var dogId = dogDto.dogId
                    var fragment = DogFragment.newInstanceWithDog(dogDto)
                    var daf = DogIdAndFragment(dogId, fragment)
                    add(button, daf)
//                    buttonAndFragmentMap[button] = daf
                }//forEach
            }//if
        }//hasDog

        //강아지 추가 프래그먼트
        var addDogButton = addButton("+", "buttonAdd")
        addDogButton.setOnClickListener(clickListener)

        var addFragment = DogFragment.newInstanceAddDog()
        var daf = DogIdAndFragment(-1L, addFragment)
        add(addDogButton, daf)

        //커밋은 바로 수행되는게 아님.
        //executePendingTransactions()로 즉시 수행
        //프래그먼트 컨테이너에 추가
        for (entry in buttonAndFragmentMap) {
            var button = entry.key
            var daf = entry.value
            var fragment = daf.fragment
            childFragmentManager.beginTransaction().add(R.id.dogInfoFragment, fragment, button.tag.toString()).commit()
            Log.d(TAG, "프래그먼트 추가 - button: ${button.tag}, fragment.tag: ${fragment.tag}")
        }
        childFragmentManager.executePendingTransactions()

        //첫번째 버튼 클릭
        var firstButton = buttonL[0]
        var firstFragment = buttonAndFragmentMap[firstButton]!!.fragment
        clickButton(firstButton)
        showFragment(firstFragment)

        //화면에 버튼 추가
        for (button in buttonL) {
            profileButtonLayout.addView(button)
        }
    }

    private fun addButton(buttonStr:String, tagStr:String): Button {
        var button = Button(style, null, R.style.borderLessButton).apply {
            text = buttonStr
            tag = tagStr
            textColor = resources.getColor(R.color.lightGray)
            textSize = 14F
            setTypeface(this.typeface, Typeface.NORMAL)
        }
        return button
    }

    private fun clickButton(clickButton: Button) {
        for (button in buttonL) {
            if (button == clickButton) {
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
    
    private fun clickButton(index:Int){
        var button = buttonL[index]
        clickButton(button)
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
        profileSelectWarningDog = view.findViewById(R.id.profileSelectWarningDog)
        walkInfo =  view.findViewById(R.id.walkInfo)
        logout = view.findViewById(R.id.logout)
        return view
    }

    private fun getSharedUserData() {
        user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userName = user_info_pref.getString("userName", "").toString()
        userId = user_info_pref.getString("id", "").toString()


        var id = user_info_pref.getString("id", "").toString()
        var password = user_info_pref.getString("password", "").toString()
        var userAge = user_info_pref.getInt("userAge", 0)
        var image = user_info_pref.getInt("image", 0)
        var userName = user_info_pref.getString("userName", "").toString()
        var phone = user_info_pref.getString("phone", "").toString()
        var email = user_info_pref.getString("email", "").toString()
        var userGender =
            if (user_info_pref.getString("userGender", "").toString().equals(Gender.MAN)) {
                Gender.MAN
            } else {
                Gender.WOMAN
            }
        userData = UserData(id, password, userAge, image, userName, phone, email, userGender)
    }

    /**
     * @param fragment 보여질 프래그먼트
     */
    fun showFragment(fragment: Fragment) {
        for (entry in buttonAndFragmentMap) {
            var entryFragment = entry.value.fragment
            if (entryFragment == fragment) {
                childFragmentManager.beginTransaction().show(entryFragment).commit()
            } else {
                childFragmentManager.beginTransaction().hide(entryFragment).commit()
            }
        }
        childFragmentManager.executePendingTransactions()
    }

    var clickListener = View.OnClickListener { view ->
        var fragment = buttonAndFragmentMap[view]!!.fragment
        showFragment(fragment)
        clickButton(view as Button)
    }

    var longClickListener = View.OnLongClickListener { buttonView ->
        //다이얼로그 뿌리고 true면 삭제, retrofit 강아지 삭제 통신도 필요(cascade도 해야할듯)
        val builder = AlertDialog.Builder(view!!.context) //context만 하면 이상하게 나옴
        builder.setTitle("강아지 삭제")
            .setMessage(
                "강아지를 삭제하시겠어요?"
            )
            .setPositiveButton("삭제", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                    var dogId = buttonAndFragmentMap[buttonView]!!.dogId
                    //레트로핏
                    dogService.deleteDog(dogId).enqueue(object : Callback<Boolean> {
                        override fun onResponse(
                            call: Call<Boolean>,
                            response: Response<Boolean>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body() == true) {
                                    //뷰에서 버튼 삭제
                                    profileButtonLayout.removeView(buttonView)

                                    var fragment =
                                        buttonAndFragmentMap[buttonView]!!.fragment

                                    //강아지 목록에서 제거
                                    for (dogDto in dogList) {
                                        if (dogDto.dogId == dogId) {
                                            dogList.remove(dogDto)
                                            break
                                        }
                                    }

                                    Log.d(TAG, "삭제된 버튼: ${buttonView!!.tag} 프래그먼트: ${fragment.tag}")
                                    //리스트와 맵에서 삭제, 삭제된 버튼의 인덱스 리턴
                                    var deleteIndex = delete(buttonView as Button)

                                    //삭제된 버튼 하나 전
                                    var preButton:Button

                                    //삭제된 인덱스가 0이면
                                    if (deleteIndex <= 0) {
                                        //이전 버튼을 첫번째 인덱스로
                                        preButton = buttonL[0]
                                    } else {
                                        //이전 버튼 지정
                                        preButton = buttonL[deleteIndex - 1]
                                    }

                                    //이전 프래그먼트 보여주고 클릭상태로
                                    var preFragment = buttonAndFragmentMap[preButton]!!.fragment
                                    showFragment(preFragment)
                                    clickButton(preButton)


                                    Log.d(TAG, "보여질 버튼: ${preButton.tag} 프래그먼트: ${preFragment.tag}")
                                    Toast.makeText(context,"삭제 완료",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context,"삭제 실패",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<Boolean>, t: Throwable) {
                            Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
            })
            .setNegativeButton("취소") { dialog, i ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
        return@OnLongClickListener true
    }

    inner class DogIdAndFragment(var dogId:Long, var fragment:Fragment){
        override fun toString(): String {
            return "DogIdAndFragment(dogId=$dogId, fragment=${fragment.tag})"
        }
    }
}