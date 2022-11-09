package com.example.aroundog

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.aroundog.Service.IntroService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.UserAndDogDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity(){
    private var backPressedTime : Long = 0
    private var loadingDialog: LoadingDialog? = null
    lateinit var userPermission: PermissionSupport
    lateinit var activityResult: ActivityResultLauncher<Intent>
    lateinit var permissionSupport: PermissionSupport
    lateinit var login_register: Button
    lateinit var login_find: Button
    lateinit var login_button: Button
    lateinit var login_id: TextView
    lateinit var login_pw: TextView
    lateinit var login_id_check: TextView
    lateinit var login_pw_check: TextView
    lateinit var login_check: TextView
    lateinit var login_stay_cb: CheckBox
    lateinit var user_info_pref: SharedPreferences
    lateinit var user_info_editor: SharedPreferences.Editor
    lateinit var dog_info_pref: SharedPreferences
    lateinit var dog_info_editor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setView()

        var autoLoginFail = intent.getBooleanExtra("autoLoginFail", false)
        if (autoLoginFail) {
            login_stay_cb.isChecked = true
            login_check.visibility = View.VISIBLE
        }

        //permissionSupport 생성
        permissionSupport = PermissionSupport(this, this)

        //권한 설정 페이지에 갔다와서 실행
        activityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (!permissionSupport.check()) {//권한 설정 페이지에 갔다와서도 없는 권한 있을때
                    Toast.makeText(this, "권한이 없어 종료합니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    //권한 있으면 별거 안함
                }
            }

        //권한 체크
        permissionCheck()


        user_info_pref = getSharedPreferences("userInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
        user_info_editor = user_info_pref.edit()


        // 회원가입 버튼
        if (login_register != null) {
            login_register.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        // 아이디, 비밀번호 찾기 버튼
        if (login_find != null) {
            login_find.setOnClickListener {
                val intent = Intent(this@LoginActivity,FindActivity::class.java)
                startActivity(intent)
            }
        }

        // 로그인 버튼

        // 로그인 버튼
        if (login_button != null) {
            login_button.setOnClickListener {
                val id = login_id?.text.toString()
                val pw = login_pw?.text.toString()
                if (login_id_check != null) {
                    login_id_check.visibility = View.GONE
                }
                if (login_pw_check != null) {
                    login_pw_check.visibility = View.GONE
                }
                if (login_check != null) {
                    login_check.visibility = View.GONE
                }
                if (id == "" && pw == "") { // 아이디, 비밀번호 둘 다 빈칸일 때
                    if (login_id_check != null) {
                        login_id_check.visibility = View.VISIBLE
                    }
                } else if (id == "") { // 아이디만 빈칸일 때
                    if (login_id_check != null) {
                        login_id_check.visibility = View.VISIBLE
                    }
                } else if (pw == "") { // 비밀번호만 빈칸일 때
                    if (login_pw_check != null) {
                        login_pw_check.visibility = View.VISIBLE
                    }
                } else {
                    loadingDialog?.show()

                    //로그인
                    login(id, pw)
                }
            }
        }
    }
    fun setView() {
        login_register = findViewById(R.id.login_register)
        login_find = findViewById(R.id.login_find)
        login_button = findViewById(R.id.login_button)
        login_id = findViewById(R.id.login_id)
        login_pw = findViewById(R.id.login_pw)
        login_id_check = findViewById(R.id.login_id_check)
        login_pw_check = findViewById(R.id.login_pw_check)
        login_check = findViewById(R.id.login_check)
        login_stay_cb = findViewById(R.id.login_stay_cb)
    }

    private fun login(
        id: String,
        pw: String
    ) {
        //로그를 보기 위한 Interceptor
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            //.connectTimeout(100, TimeUnit.SECONDS)
            //.readTimeout(100,TimeUnit.SECONDS)
            //.writeTimeout(100, TimeUnit.SECONDS)
            .build()
        // 데이터베이스 접속 및 확인
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)     //로그 기능 추가
            .build()

        val introAPI = retrofit.create(IntroService::class.java)

        introAPI.login(id, pw).enqueue(object : Callback<List<UserAndDogDto>> {
            override fun onResponse(
                call: Call<List<UserAndDogDto>>,
                response: Response<List<UserAndDogDto>>
            ) {
                loadingDialog?.dismiss()
                if (response.isSuccessful) { // 성공적으로 받아왔을 때
                    if (response.body()!![0].isSuccess()) { // 아이디, 비밀번호가 일치했을 때
                        val userAndDogList = response.body() // 유저 데이터를 호출한 데이터베이스로부터 받아와 변수에 저장

                        //유저 정보 저장
                        user_info_editor.putString("id", userAndDogList!![0].userId)
                        user_info_editor.putString("password", userAndDogList!![0].password)
                        user_info_editor.putInt("userAge", userAndDogList!![0].userAge)
                        user_info_editor.putInt("image", userAndDogList!![0].image)
                        user_info_editor.putString("userName", userAndDogList!![0].userName)
                        user_info_editor.putString("phone", userAndDogList!![0].phone)
                        user_info_editor.putString("email", userAndDogList!![0].email)
                        user_info_editor.putString(
                            "userGender",
                            userAndDogList!![0].userGender.toString()
                        )
                        user_info_editor.commit() // 세션 영역에 해당 유저의 정보를 넣음

                        dog_info_pref =
                            getSharedPreferences("dogInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
                        dog_info_editor = dog_info_pref.edit()
                        if (userAndDogList!![0].dogId != 0L) {//등록된 강아지가 있는지 확인
                            var dogList = mutableListOf<DogDto>()
                            //강아지 정보 리스트로 저장
                            userAndDogList!!.forEach { userAndDogDto ->
                                var dog = DogDto(
                                    userAndDogDto.dogId,
                                    userAndDogDto.dogName,
                                    userAndDogDto.dogAge,
                                    userAndDogDto.dogWeight,
                                    userAndDogDto.dogHeight,
                                    userAndDogDto.dogGender,
                                    userAndDogDto.breed,
                                    userAndDogDto.dogImgList
                                )
                                dogList.add(dog)
                            }
                            //Json 으로 만들기 위한 Gson
                            var makeGson = GsonBuilder().create()
                            var type: TypeToken<MutableList<DogDto>> =
                                object : TypeToken<MutableList<DogDto>>() {}
                            var dogStr = makeGson.toJson(dogList, type.type)

                            dog_info_editor.putBoolean("hasDog", true)
                            dog_info_editor.putString("dogList", dogStr)
                        } else {//강아지 없을때
                            dog_info_editor.putBoolean("hasDog", false)
                        }
                        dog_info_editor.commit()


                        if (login_stay_cb != null) {
                            if (login_stay_cb.isChecked) { // 로그인 상태 유지가 체크되어 있다면
                                user_info_editor.putBoolean(
                                    "autoLogin",
                                    true
                                ) // 자동 로그인 여부를 true
                                user_info_editor.commit()
                            } else { // 로그인 상태 유지 체크가 안되어 있다면
                                user_info_editor.putBoolean(
                                    "autoLogin",
                                    false
                                ) // 자동 로그인 여부를 false
                                user_info_editor.commit()
                            }

                            //메인 액티비티 시작
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else { // 아이디, 비밀번호가 일치하지 않을 때
                        if (login_check != null) {
                            login_check.visibility = View.VISIBLE
                        } // 다시 확인하라는 텍스트뷰 출력
                    }
                }
            }

            override fun onFailure(call: Call<List<UserAndDogDto>>, t: Throwable) {
                loadingDialog?.dismiss()
                Toast.makeText(applicationContext, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                    .show()
                t.printStackTrace()
            }
        })
    }

    override fun onBackPressed() {

        // 2초내 다시 클릭하면 앱 종료
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
            return
        }
        // 처음 클릭 메시지
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }

    // 권한 체크
    private fun permissionCheck() {
        //https://hellose7.tistory.com/85
        //https://debbi.tistory.com/31
        val permissions = permissionSupport.permissions
        var requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: MutableMap<String, Boolean> ->
                val denieds = result.filter { !it.value }.map { it.key }
                when {
                    denieds.isNotEmpty() -> {
                        val map = denieds.groupBy { permission ->
                            if (shouldShowRequestPermissionRationale(permission))//최소실행시 false, 거부한 권한이 있을 경우 true, 다시 묻지 않기까지 선택한 경우 false
                                "DENIED"
                            else
                                "EXPLAINED"
                        }

                        //한번 거부했을때
                        if (map["DENIED"] != null) {
                            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                            //재요청
                            ActivityCompat.requestPermissions(
                                this,
                                map["DENIED"]!!.toTypedArray(), 0
                            )
                        }

                        //두번 이상 거부해서 팝업창이 안뜰때
                        if (map["EXPLAINED"] != null) {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("권한설정")
                                .setMessage(
                                    "ArounDog을 사용하시려면 설정에서 권한을 허용해주세요."
                                )
                                .setPositiveButton("설정") { dialog, i ->
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts(
                                            "package",
                                            applicationContext.packageName,
                                            null
                                        )
                                    )
                                    activityResult.launch(intent)
                                }
                            builder.setNegativeButton("거부") { dialog, i ->
                                finish()
                            }
                            builder.setCancelable(false)
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                    else -> {
                        //모든 권한 다 있을때
                    }
                }
            }
        requestPermissionLauncher.launch(permissions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                Toast.makeText(this, "권한이 없어 종료합니다.", Toast.LENGTH_SHORT).show()
                exitProcess(1)
            }
        }
    }
}
