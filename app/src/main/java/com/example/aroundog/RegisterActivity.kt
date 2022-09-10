package com.example.aroundog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.aroundog.Model.CheckSuccess
import com.example.aroundog.Service.IntroService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private val TAG = "Register"
    private var register_button: Button? = null
    private var register_id_validate: ImageButton? = null
    private var register_id: EditText? = null
    private var register_pw: EditText? = null
    private var register_pw2: EditText? = null
    private var register_name: EditText? = null
    private var register_number: EditText? = null
    private var register_email: EditText? = null
    private var toolbar: Toolbar? = null
    private var register_check: TextView? = null
    private var register_id_check: TextView? = null
    private var register_pw_check: TextView? = null
    private var register_validate_check: TextView? = null
    private var register_id_length_check: TextView? = null
    private var register_pw_length_check: TextView? = null
    private var register_age: TextView? = null
    private var register_man: RadioButton? = null
    private var register_woman: RadioButton? = null
    private var builder: AlertDialog.Builder? = null
    //private val dialog: AlertDialog? = null
    private var validate = 2 // 아이디 확인값, 확인을 아예 안했을 때 2, 확인은 했지만 맞지 않았을 때 1, 확인이 완료됐을 때 0
    private var idLengthCheck = false
    private var pwLengthCheck = false
    private var loadingDialog: LoadingDialog? = null
    private var userCardView: CardView? = null
    private var userProfile: ImageView? = null
    private var userProfileDialog: Dialog? = null
    private var userImage = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        findViewById() // 아이템 맞추기

        // 툴바 생성
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)

        userCardView!!.setOnClickListener {
            userProfileDialog =
                userProfile?.let { it1 ->
                    UserProfileDialog(this@RegisterActivity, object : UserProfileDialog.Select {
                        override fun clickProfile(userImg: Int) {
                            userImage = userImg
                        }
                    }, it1)
                }
            userProfileDialog!!.show()
        }


        // 아이디 중복 확인 버튼을 눌렀을 때
        register_id_validate!!.setOnClickListener {
            // 텍스트뷰 영역 비활성화
            register_validate_check!!.visibility = View.GONE
            register_id_check!!.visibility = View.GONE
            val id = register_id!!.text.toString() // 아이디값을 받아옴
            if (id == "") { // 아이디 입력값이 없을 때
                register_id_check!!.visibility = View.VISIBLE
                register_id_check!!.setTextColor(Color.RED)
                register_id_check!!.text = "아이디를 입력해주세요."
                validate = 1
            } else if (id.length < 6) { // 유효성을 충족하지 않았을 때
                register_id_length_check!!.setTextColor(Color.RED)
            } else {
                register_id_length_check!!.setTextColor(-0xb946a5)
                Log.d(TAG, id)
                loadingDialog!!.show()

                //로그를 보기 위한 Interceptor
                val interceptor = HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client: OkHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    //.connectTimeout(100, TimeUnit.SECONDS)
                    //.readTimeout(100, TimeUnit.SECONDS)
                    //.writeTimeout(100, TimeUnit.SECONDS)
                    .build()
                // 데이터베이스 접속 및 확인
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                val validateAPI: IntroService = retrofit.create(IntroService::class.java)

                validateAPI.idValidate(id).enqueue(object : Callback<Boolean> {
                    override fun onResponse(
                        call: Call<Boolean>,
                        response: Response<Boolean>
                    ) {
                        loadingDialog!!.dismiss()
                        if (response.isSuccessful) { // 성공적으로 받아왔을 때
                            if (response.body() == false) { // 중복되는 아이디가 없을 때
                                register_id_validate!!.setImageResource(R.drawable.ic_register_check)
                                validate = 0
                            } else { // 중복되는 아이디가 있을 때
                                register_id_validate!!.setImageResource(R.drawable.ic_register_not_check)
                                register_id_check!!.visibility = View.VISIBLE
                                register_id_check!!.setTextColor(Color.RED)
                                register_id_check!!.text = "존재하는 아이디입니다! 다른 아이디를 입력해주세요."
                                validate = 1
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "오류가 발생했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<Boolean>,
                        t: Throwable
                    ) {
                        loadingDialog!!.dismiss()
                        Toast.makeText(applicationContext, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                            .show()
                        t.printStackTrace()
                    }
                })
            }
        }

        // 회원가입 버튼을 눌렀을 때
        register_button?.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "userImage : $userImage")


            // 값을 모두 받아온다
            val id = register_id!!.text.toString()
            val password = register_pw!!.text.toString()
            val password2 = register_pw2!!.text.toString()
            val name = register_name!!.text.toString()
            val number = register_number!!.text.toString()
            val email = register_email!!.text.toString()
            val age = register_age!!.text.toString()
            val man = register_man!!.isChecked
            val woman = register_woman!!.isChecked
            val checkRadio = (man || woman)

            if (validate == 2) { // 아이디 체크를 완전히 하지 않았을 때
                register_validate_check!!.visibility = View.VISIBLE
            }

            // 빈 값이 존재할 경우
            if (id == "" || password == "" || password2 == "" || name == "" || number == "" || email == "" || age == "" || !checkRadio) {
                register_check!!.visibility = View.VISIBLE // 값을 채워넣으라는 안내문 출력
                return@OnClickListener
            } else { // 값을 모두 넣었을 때
                //성별 저장
                var strRadio = if (man) {
                    "MAN"
                } else {
                    "WOMAN"
                }

                // 비밀번호는 재확인 비밀번호와 일치하는지, 아이디 중복확인은 완전히 완료하였는지,
                // 아이디, 비밀번호는 조건에 맞게끔 입력하였는지 검사 후 일치하면 데이터베이스를 받아오는 단계로 넘어감
                if (password == password2 && validate == 0 && idLengthCheck && pwLengthCheck) {
                    loadingDialog!!.show()
                    val retrofit: Retrofit = Retrofit.Builder()
                        .baseUrl(BuildConfig.SERVER)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val introAPI: IntroService = retrofit.create(IntroService::class.java)
                    introAPI.signUp(id, password, userImage, name, number, email, strRadio, age)
                        .enqueue(object : Callback<CheckSuccess?> {
                            override fun onResponse(
                                call: Call<CheckSuccess?>,
                                response: Response<CheckSuccess?>
                            ) {
                                loadingDialog!!.dismiss()
                                if (response.isSuccessful && response.body() != null) { // 성공적으로 받아왔을 때
                                    val intent = Intent(
                                        this@RegisterActivity,
                                        MainActivity2::class.java
                                    ) // 로그인 화면으로
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "회원가입 도중 오류가 발생했습니다. 다시 시도해주세요.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(
                                        this@RegisterActivity,
                                        MainActivity2::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                            }

                            override fun onFailure(call: Call<CheckSuccess?>, t: Throwable) {
                                loadingDialog!!.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "서버 네트워크가 닫혀있습니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent =
                                    Intent(this@RegisterActivity, MainActivity2::class.java)
                                startActivity(intent)
                                finish()
                                t.printStackTrace()
                            }
                        })
                }
            }
        })
    }

    // 메뉴별로 역할 지정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun findViewById() {
        toolbar = findViewById(R.id.register_toolbar)
        register_button = findViewById(R.id.register_button)
        register_id_validate = findViewById(R.id.register_id_validate)
        register_id = findViewById(R.id.register_id)
        register_pw = findViewById(R.id.register_pw)
        register_pw2 = findViewById(R.id.register_pw2)
        register_name = findViewById(R.id.register_name)
        register_number = findViewById(R.id.register_number)
        register_email = findViewById(R.id.register_email)
        register_check = findViewById(R.id.register_check)
        register_id_check = findViewById(R.id.register_id_check)
        register_pw_check = findViewById(R.id.register_pw_check)
        register_validate_check = findViewById(R.id.register_validate_check)
        register_id_length_check = findViewById(R.id.register_id_length_check)
        register_pw_length_check = findViewById(R.id.register_pw_length_check)
        register_age = findViewById(R.id.register_age)
        register_man = findViewById(R.id.register_man)
        register_woman = findViewById(R.id.register_woman)
        userCardView = findViewById(R.id.register_user_cardView)
        userProfile = findViewById(R.id.register_user_profile)
        loadingDialog = LoadingDialog(this, 3)
        register_name!!.setFilters(arrayOf(textSetFilter("kor"))) // 한글만 나오게 설정
        builder = AlertDialog.Builder(this@RegisterActivity)

        // 아이디 글자 수가 6자가 넘게끔 설정
        register_id!!.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                idLengthCheck = if (register_id!!.length() < 6) {
                    register_id_length_check!!.setTextColor(Color.RED)
                    false
                } else {
                    register_id_length_check!!.setTextColor(-0xb946a5)
                    true
                }
            }
        })

        // 비밀번호 영문 / 숫자 조합 설정
        val VALID_PASSWOLD_REGEX_ALPHA_NUM =
            Pattern.compile("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#$%^&*])(?=.*[0-9!@#$%^&*]).{8,16}$")
        // 8자리 ~ 16자리까지 가능
        register_pw!!.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            // EditText 영역을 입력하고 다른 영역으로 이동할 때 리스너 발생
            // 변화가 감지됐을 때
            val pw = register_pw!!.getText().toString()
            val pw2 = register_pw2!!.getText().toString()
            val matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(pw) // 기존에 설정했던 제한과 입력한 패스워드 비교
            pwLengthCheck = matcher.matches()
            if (!hasFocus) {
                if (pwLengthCheck == false) {
                    register_pw_length_check!!.setTextColor(Color.RED)
                } else {
                    register_pw_length_check!!.setTextColor(-0xb946a5)
                }
                if (pw2 != "" && pw != pw2) {
                    register_pw_check!!.setTextColor(Color.RED)
                    register_pw_check!!.setText("비밀번호가 일치하지 않습니다! 다시 입력해주세요.")
                    register_pw_check!!.setVisibility(View.VISIBLE)
                } else if (pw2 != "" && pw == pw2) {
                    register_pw_check!!.setTextColor(-0xb946a5)
                    register_pw_check!!.setText("비밀번호가 일치해요.")
                    register_pw_check!!.setVisibility(View.VISIBLE)
                }
            }
        })
        register_pw2!!.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            val pw = register_pw!!.getText().toString()
            val pw2 = register_pw2!!.getText().toString()
            if (!hasFocus) {
                if (pw != pw2) {
                    register_pw_check!!.setTextColor(Color.RED)
                    register_pw_check!!.setText("비밀번호가 일치하지 않습니다! 다시 입력해주세요.")
                    register_pw_check!!.setVisibility(View.VISIBLE)
                } else {
                    register_pw_check!!.setTextColor(-0xb946a5)
                    register_pw_check!!.setText("비밀번호가 일치해요.")
                    register_pw_check!!.setVisibility(View.VISIBLE)
                }
            }
        })
    }

    fun textSetFilter(lang: String): InputFilter {
        val ps: Pattern
        ps = if (lang == "kor") {
            Pattern.compile("^[ㄱ-ㅣ가-힣\\s]*$") //한글 및 공백문자만 허용
        } else {
            Pattern.compile("[a-zA-Z\\s-]*$") //영어 및 하이픈 문자만 허용
        }
        return (label@ InputFilter { source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int ->
            if (!ps.matcher(source).matches()) {
                return@InputFilter ""
            }
            null
        })
    }
}