package com.example.aroundog

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.aroundog.Model.Gender
import com.example.aroundog.Service.IntroService
import com.example.aroundog.Service.UserService
import com.example.aroundog.fragments.ProfileFragment
import com.example.aroundog.utils.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileEditActivity : AppCompatActivity() {
    val TAG = "ProfileEditActivity"
    lateinit var profileEditId: TextView
    lateinit var profileEditName: EditText
    lateinit var profileEditAge: EditText
    lateinit var profileEditRadio: RadioGroup
    lateinit var profileEditEmail: EditText
    lateinit var profileEditPhone: EditText
    lateinit var profileEditCheck: TextView
    lateinit var profileEditButton: Button
    var userCardView: CardView? = null
    var userProfileDialog: Dialog? = null
    var profileEditProfile: ImageView? = null
    var userImage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val retrofit= Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserService::class.java)

        //유저 정보 가져옴
        val userInfo = intent.getSerializableExtra("user") as UserData
        setView(userInfo)

        //프로필 이미지 지정
        userImage = userInfo.image
        if (userImage == 1) {
            profileEditProfile!!.setImageResource(R.drawable.profile_1)
        }else if (userImage == 2) {
            profileEditProfile!!.setImageResource(R.drawable.profile_2)

        } else {
            profileEditProfile!!.setImageResource(R.drawable.profile_3)
        }

        //프로필 이미지 선택시
        userCardView!!.setOnClickListener {
            userProfileDialog =
                profileEditProfile?.let { it1 ->
                    UserProfileDialog(this@ProfileEditActivity, object : UserProfileDialog.Select {
                        override fun clickProfile(userImg: Int) {
                            userImage = userImg
                        }
                    }, it1)
                }
            userProfileDialog!!.show()
        }

        //수정완료 버튼 클릭 시
        profileEditButton.setOnClickListener {
            var user_info_pref = getSharedPreferences("userInfo", MODE_PRIVATE) // 세션 영역에 저장할 유저 정보
            var user_info_editor = user_info_pref.edit()


            var id = profileEditId.text.toString()
            var name = profileEditName.text.toString()
            var age = profileEditAge.text.toString()
            var email = profileEditEmail.text.toString()
            var phone = profileEditPhone.text.toString()
            var gender = if (profileEditRadio.checkedRadioButtonId == R.id.profileEditMan) {
                Gender.MAN
            } else {
                Gender.WOMAN
            }

            if (name == "" || age == "" || email == "" || phone == "") {
                profileEditCheck.visibility = View.VISIBLE
                return@setOnClickListener
            }else{
                retrofit.updateUser(id, name, age.toInt(), email, phone, gender, userImage).enqueue(object:Callback<Boolean>{
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful) {
                            if (response.body() == true) {
                                //유저 정보 저장
                                user_info_editor.putInt("userAge", age.toInt())
                                user_info_editor.putInt("image", userImage)
                                user_info_editor.putString("userName", name)
                                user_info_editor.putString("phone", phone)
                                user_info_editor.putString("email", email)
                                user_info_editor.putString("userGender", gender.toString())
                                user_info_editor.commit() // 세션 영역에 해당 유저의 정보를 넣음
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
    }

    private fun setView(userInfo: UserData) {
        profileEditId = findViewById(R.id.profileEditId)
        profileEditName = findViewById(R.id.profileEditName)
        profileEditAge = findViewById(R.id.profileEditAge)
        profileEditRadio = findViewById(R.id.profileEditRadio)
        profileEditEmail = findViewById(R.id.profileEditEmail)
        profileEditPhone = findViewById(R.id.profileEditPhone)
        profileEditCheck = findViewById(R.id.profileEditCheck)
        profileEditButton = findViewById(R.id.profileEditButton)

        //값 지정
        profileEditId.text = userInfo.id
        profileEditName.setText(userInfo.userName)
        profileEditAge.setText(userInfo.userAge.toString())
        profileEditEmail.setText(userInfo.email)
        profileEditPhone.setText(userInfo.phone)
        if (userInfo.userGender == Gender.WOMAN) {
            profileEditRadio!!.check(R.id.profileEditMan)
        } else {
            profileEditRadio!!.check(R.id.profileEditWoman)
        }

        userCardView = findViewById(R.id.profileEditCardView)
        profileEditProfile = findViewById(R.id.profileEditProfile)
    }
}