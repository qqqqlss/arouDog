package com.example.aroundog

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import com.example.aroundog.Model.DogBreed
import com.example.aroundog.Service.CoordinateService
import com.example.aroundog.Service.UserService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.sdk25.coroutines.onClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SelectWarningDog : AppCompatActivity() {
    var TAG = "SELECTWARNINGDOG"
    lateinit var warningDogs: TextView
    lateinit var spinner: Spinner
    lateinit var husky: LinearLayout
    lateinit var samoyed: LinearLayout
    lateinit var retreiever: LinearLayout
    lateinit var shephered: LinearLayout
    lateinit var malamute: LinearLayout
    lateinit var beagle: LinearLayout
    lateinit var bordercollie: LinearLayout
    lateinit var bulldog: LinearLayout
    lateinit var shiba: LinearLayout
    lateinit var welshcorgi: LinearLayout
    lateinit var chihuahua: LinearLayout
    lateinit var maltese: LinearLayout
    lateinit var poodle: LinearLayout
    lateinit var shihtzu: LinearLayout
    lateinit var yorkshireterrier: LinearLayout
    lateinit var dogBigEtc: LinearLayout
    lateinit var dogMediumEct: LinearLayout
    lateinit var dogSmallEct: LinearLayout
    lateinit var bigCheck: CheckBox
    lateinit var mediumCheck: CheckBox
    lateinit var smallCheck: CheckBox
    lateinit var warningDogSelectButton: Button

    //retrofit
    lateinit var userService: UserService

    //버튼 클릭여부 확인
    var isBigChecked = false
    var isMediumChecked = false
    var isSmallChecked = false

    //<"크기", <뷰, {"종 이름", 선택여부}>>
    var dogData = mutableMapOf<String, LinkedHashMap<View, DogNameAndBoolean>>()

    lateinit var userId: String

    lateinit var user_info_editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_warning_dog)

        //뷰 바인딩
        setView()

        //dogData초기화
        initdogData()

        //리스너 등록
        setListener()

        //저장된 id 정보 가져오기
        var user_info_pref =
            this.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        user_info_editor = user_info_pref.edit()
        userId = user_info_pref.getString("id", "error").toString()


        //레트로핏 초기화
        initRetrofit()

        //기피 강아지 목록 가져오기
        getHateDogs()

    }

    /**
     * Retrofit초기화
     */
    private fun initRetrofit() {
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        var retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
        userService = retrofit.create(UserService::class.java)

    }

    /**
     * 서버에서 싫어하는 강아지 목록 가져옴
     */
    private fun getHateDogs(){
        userService.getHateDog(userId).enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        var strHateDog = response.body()
                        for (size in dogData) {
                            for (entry in size.value) {
                                if (strHateDog!!.contains(entry.value.dogBreed.eng)) {
                                    entry.key.callOnClick()
                                    entry.value.boolean = true
                                }
                            }
                        }
                        setSelectText(" | ")
                    } catch (e:Exception) {
                        Log.d(TAG, "$e")
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "기피 강아지 불러오기 실패", t)
            }
        })
    }

    fun setView() {
        warningDogs = findViewById(R.id.warningDogs)
        spinner = findViewById(R.id.warningSpinner)
        husky = findViewById(R.id.warningHusky)
        samoyed = findViewById(R.id.warningSamoyed)
        retreiever = findViewById(R.id.warningRetreiever)
        shephered = findViewById(R.id.warningShephered)
        malamute = findViewById(R.id.warningMalamute)
        beagle = findViewById(R.id.warningBeagle)
        bordercollie = findViewById(R.id.warningBrdercollie)
        bulldog = findViewById(R.id.warningBulldog)
        shiba = findViewById(R.id.warningShiba)
        welshcorgi = findViewById(R.id.warningWelshcorgi)
        chihuahua = findViewById(R.id.warningChihuahua)
        maltese = findViewById(R.id.warningMaltese)
        poodle = findViewById(R.id.warningPoodle)
        shihtzu = findViewById(R.id.warningShihtzu)
        yorkshireterrier = findViewById(R.id.warningYorkshireterrier)
        dogBigEtc = findViewById(R.id.warningDogBigEtc)
        dogMediumEct = findViewById(R.id.warningDogMediumEct)
        dogSmallEct = findViewById(R.id.warningDogSmallEct)
        bigCheck = findViewById(R.id.selectWarningBigCheck)
        mediumCheck = findViewById(R.id.selectWarningMediumCheck)
        smallCheck = findViewById(R.id.selectWarningSmallCheck)
        warningDogSelectButton = findViewById(R.id.warningDogSelectButton)
    }


    fun initdogData() {
        var bigMap = LinkedHashMap<View, DogNameAndBoolean>()
        bigMap[husky] = DogNameAndBoolean(DogBreed.HUSKY, false)
        bigMap[samoyed] = DogNameAndBoolean(DogBreed.SAMOYED, false)
        bigMap[retreiever] = DogNameAndBoolean(DogBreed.RETRIEVER, false)
        bigMap[shephered] = DogNameAndBoolean(DogBreed.SHEPHERD, false)
        bigMap[malamute] = DogNameAndBoolean(DogBreed.MALAMUTE, false)
        bigMap[dogBigEtc] = DogNameAndBoolean(DogBreed.DOGBITECT, false)
        dogData["big"] = bigMap

        var mediumMap = LinkedHashMap<View, DogNameAndBoolean>()
        mediumMap[beagle] = DogNameAndBoolean(DogBreed.BEAGLE, false)
        mediumMap[bordercollie] = DogNameAndBoolean(DogBreed.BORDERCOLLIE, false)
        mediumMap[bulldog] = DogNameAndBoolean(DogBreed.BULLDOG, false)
        mediumMap[shiba] = DogNameAndBoolean(DogBreed.SHIBA, false)
        mediumMap[welshcorgi] = DogNameAndBoolean(DogBreed.WELSHCORGI, false)
        mediumMap[dogMediumEct] = DogNameAndBoolean(DogBreed.DOGMEDIUMECT, false)
        dogData["medium"] = mediumMap

        var smallMap = LinkedHashMap<View, DogNameAndBoolean>()
        smallMap[chihuahua] = DogNameAndBoolean(DogBreed.CHIHUAHUA, false)
        smallMap[maltese] = DogNameAndBoolean(DogBreed.MALTESE, false)
        smallMap[poodle] = DogNameAndBoolean(DogBreed.POODLE, false)
        smallMap[shihtzu] = DogNameAndBoolean(DogBreed.SHIHTZU, false)
        smallMap[yorkshireterrier] = DogNameAndBoolean(DogBreed.YORKSHIRETERRIER, false)
        smallMap[dogSmallEct] = DogNameAndBoolean(DogBreed.DOGSMALLECT, false)
        dogData["small"] = smallMap
    }


    private fun setListener() {
        //각 사진에 적용할 리스너 생성
        var listener = OnClickListener {
            for (size in dogData) {
                var dogInSizeMap = size.value //LinkedHashMap<View, DogNameAndBoolean>
                if (dogInSizeMap.containsKey(it)) {
                    if (dogInSizeMap[it]!!.boolean) {//선택된 상태면 테두리 없앰
                        dogInSizeMap[it]!!.boolean = false
                        it.setBackgroundResource(0)
                    } else {//선택 안되어있으면 테두리 생성
                        dogInSizeMap[it]!!.boolean = true//선택된거 true로 변경
                        it.background = resources.getDrawable(R.drawable.dog_select_style, null)
                    }
                    setSelectText(" | ")
                }
            }

            //모두 선택되었는지 확인하고 모두 선택되면 체크버튼 눌리게
            //체크버튼 활성화 된 후에 하나라도 취소하면 체크버튼 눌린거 해제하기
            isAllSelected("big")
            isAllSelected("medium")
            isAllSelected("small")
        }

        //리스너 등록
        for (sizeMap in dogData) {
            for (map in sizeMap.value) {
                map.key.setOnClickListener(listener)
            }
        }

        //대형견 버튼
        bigCheck.setOnClickListener {
            //함수로 만들었었는데 코틀린 함수는 매개변수를 변경할 수 없는 문제가 있었음
            if (!isBigChecked) {//대형견 전체 버튼이 체크되어있지 않을때
                for (map in dogData["big"]!!) {
                    map.key.background =
                        resources.getDrawable(R.drawable.dog_select_style, null)//클릭한 효과
                    map.value.boolean = true
                }
            } else {
                for (map in dogData["big"]!!) {
                    map.key.setBackgroundResource(0)
                    map.value.boolean = false
                }
            }
            isBigChecked = !isBigChecked
            setSelectText(" | ")
        }

        //중형견 버튼
        mediumCheck.setOnClickListener {
            //함수로 만들었었는데 코틀린 함수는 매개변수를 변경할 수 없는 문제가 있었음
            if (!isMediumChecked) {//대형견 전체 버튼이 체크되어있지 않을때
                for (map in dogData["medium"]!!) {
                    map.key.background =
                        resources.getDrawable(R.drawable.dog_select_style, null)//클릭한 효과
                    map.value.boolean = true
                }
            } else {
                for (map in dogData["medium"]!!) {
                    map.key.setBackgroundResource(0)
                    map.value.boolean = false
                }
            }
            isMediumChecked = !isMediumChecked
            setSelectText(" | ")
        }

        //소형견 버튼
        smallCheck.setOnClickListener {
            //함수로 만들었었는데 코틀린 함수는 매개변수를 변경할 수 없는 문제가 있었음
            if (!isSmallChecked) {//대형견 전체 버튼이 체크되어있지 않을때
                for (map in dogData["small"]!!) {
                    map.key.background =
                        resources.getDrawable(R.drawable.dog_select_style, null)//클릭한 효과
                    map.value.boolean = true
                }
            } else {
                for (map in dogData["small"]!!) {
                    map.key.setBackgroundResource(0)
                    map.value.boolean = false
                }
            }
            isSmallChecked = !isSmallChecked
            setSelectText(" | ")
        }

        //선택완료 버튼
        warningDogSelectButton.setOnClickListener {
            //str이 빈문자열인채로 올라갈 경우 받아올때 문제 발생
            //글자 생성
            var str = "$"
            for (size in dogData) {
                for (entry in size.value) {
                    if (entry.value.boolean) {
                        if (str.equals("$"))
                            str += entry.value.dogBreed.eng
                        else
                            str += "%" + entry.value.dogBreed.eng
                    }
                }
            }

            //retrofit 전송
            userService.updateHateDog(userId, str).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        if (response.body() == true) {
                            //세션 영역에 저장
                            user_info_editor.putString("hateDogs", str)
                            user_info_editor.commit()
                            Toast.makeText(applicationContext, "업데이트 성공", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "업데이트 실패", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.d(TAG, "업데이트 실패",t)
                    Toast.makeText(applicationContext, "업데이트 실패", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }
    }

    private fun isAllSelected(size: String) {
        //대, 중, 소를 돌면서 다 체크되었으면 체크버튼 클릭된 상태로 만들기
        var isAllSelect = true
        for (map in dogData[size]!!) {
            if (!map.value.boolean) {
                isAllSelect = false
            }
        }

        if (isAllSelect) {//모두 선택되었으면
            if (size == "big") {//대형견일때 버튼 누른 효과, 버튼이 눌린상태일때 true인 isBigChecked를 true로
                bigCheck.isChecked = true
                isBigChecked = true
            } else if (size == "medium") {
                mediumCheck.isChecked = true
                isMediumChecked = true
            } else {
                smallCheck.isChecked = true
                isSmallChecked = true
            }
        } else {
            if (size == "big") {
                bigCheck.isChecked = false
                isBigChecked = false
            } else if (size == "medium") {
                mediumCheck.isChecked = false
                isMediumChecked = false
            } else {
                smallCheck.isChecked = false
                isSmallChecked = false
            }
        }
    }

    fun setSelectText(add: String) {
        var str = ""
        var bigStr = ""
        var mediumStr = ""
        var smallStr = ""
        for (entry in dogData["big"]!!.values) {
            if (entry.boolean) {
                if (bigStr.equals(""))
                    bigStr += entry.dogBreed.kor
                else
                    bigStr += add + entry.dogBreed.kor
            }
        }
        for (entry in dogData["medium"]!!.values) {
            if (entry.boolean) {
                if (mediumStr.equals(""))
                    mediumStr += entry.dogBreed.kor
                else
                    mediumStr += add + entry.dogBreed.kor
            }
        }
        for (entry in dogData["small"]!!.values) {
            if (entry.boolean) {
                if (smallStr.equals(""))
                    smallStr += entry.dogBreed.kor
                else
                    smallStr += add + entry.dogBreed.kor
            }
        }

        str = bigStr + "\n" + mediumStr + "\n" + smallStr
        Log.d(TAG, "선택한 강아지 : $str")
        warningDogs.text = str
    }


    inner class DogNameAndBoolean(var dogBreed: DogBreed, var boolean: Boolean) {
        override fun toString(): String {
            return "DogNameAndBoolean(dogBreed='$dogBreed', boolean=$boolean)"
        }
    }
}