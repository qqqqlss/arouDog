package com.example.aroundog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.example.aroundog.utils.DogBreedData
import kotlin.collections.HashMap

class SelectDogActivity : AppCompatActivity() {
    final val TAG = "SelectDogActivity"

    companion object {
        var selectDog = MutableLiveData<Long>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)//타이틀 없애기
        setContentView(R.layout.activity_select_dog)


        var map = getMap()//<뷰, (해당 강아지 아이디, 선택여부)>
        var listener = OnClickListener {
            if (map[it]!!.boolean == true) {//선택된 상태면 테두리 없앰
                map[it]!!.boolean = false
                it.setBackgroundResource(0)
            } else {//선택 안되어있으면 테두리 생성
                for (entry in map) {
                    //전체 해제
                    if (entry.value.boolean) {
                        entry.value.boolean = false
                        entry.key.setBackgroundResource(0)
                    }
                }
                map[it]!!.boolean = true//선택된거 true로 변경
                it.background = resources.getDrawable(R.drawable.dog_select_style, null)
            }
        }

        //리스너 등록
        for (entry in map) {
            entry.key.setOnClickListener(listener)
        }

        //버튼 클릭 리스너
       var button = findViewById<Button>(R.id.dogSelectButton)
        button.setOnClickListener {
            //선택된 레이아웃
            for (entry in map) {
                //true인거 찾음
                if (entry.value.boolean) {
                    var id = entry.value.dogId
                    selectDog.postValue(id)
                    finish()
                }
            }
        }
    }

    fun getMap(): HashMap<View, DogIdAndBoolean> {
        var husky = findViewById<LinearLayout>(R.id.Husky)
        var samoyed = findViewById<LinearLayout>(R.id.Samoyed)
        var retreiever = findViewById<LinearLayout>(R.id.Retreiever)
        var shephered = findViewById<LinearLayout>(R.id.Shephered)
        var malamute = findViewById<LinearLayout>(R.id.Malamute)
        var beagle = findViewById<LinearLayout>(R.id.Beagle)
        var bordercollie = findViewById<LinearLayout>(R.id.Brdercollie)
        var bulldog = findViewById<LinearLayout>(R.id.Bulldog)
        var shiba = findViewById<LinearLayout>(R.id.Shiba)
        var welshcorgi = findViewById<LinearLayout>(R.id.Welshcorgi)
        var chihuahua = findViewById<LinearLayout>(R.id.Chihuahua)
        var maltese = findViewById<LinearLayout>(R.id.Maltese)
        var poodle = findViewById<LinearLayout>(R.id.Poodle)
        var shihtzu = findViewById<LinearLayout>(R.id.Shihtzu)
        var yorkshireterrier = findViewById<LinearLayout>(R.id.Yorkshireterrier)
        var dogBigEtc = findViewById<LinearLayout>(R.id.dogBigEtc)
        var dogMediumEct = findViewById<LinearLayout>(R.id.dogMediumEct)
        var dogSmallEct = findViewById<LinearLayout>(R.id.dogSmallEct)

        var map = HashMap<View, DogIdAndBoolean>()
        map[husky] = DogIdAndBoolean(DogBreedData.HUSKY, false)
        map[samoyed] = DogIdAndBoolean(DogBreedData.SAMOYED, false)
        map[retreiever] = DogIdAndBoolean(DogBreedData.RETRIEVER, false)
        map[shephered] = DogIdAndBoolean(DogBreedData.SHEPHERD, false)
        map[malamute] = DogIdAndBoolean(DogBreedData.MALAMUTE, false)
        map[beagle] = DogIdAndBoolean(DogBreedData.BEAGLE, false)
        map[bordercollie] = DogIdAndBoolean(DogBreedData.BORDERCOLLIE, false)
        map[bulldog] = DogIdAndBoolean(DogBreedData.BULLDOG, false)
        map[shiba] = DogIdAndBoolean(DogBreedData.SHIBA, false)
        map[welshcorgi] = DogIdAndBoolean(DogBreedData.WELSHCORGI, false)
        map[chihuahua] = DogIdAndBoolean(DogBreedData.CHIHUAHUA, false)
        map[maltese] = DogIdAndBoolean(DogBreedData.MALTESE, false)
        map[poodle] = DogIdAndBoolean(DogBreedData.POODLE, false)
        map[shihtzu] = DogIdAndBoolean(DogBreedData.SHIHTZU, false)
        map[yorkshireterrier] = DogIdAndBoolean(DogBreedData.YORKSHIRETERRIER, false)
        map[dogBigEtc] = DogIdAndBoolean(DogBreedData.DOGBITECT, false)
        map[dogMediumEct] = DogIdAndBoolean(DogBreedData.DOGMEDIUMECT, false)
        map[dogSmallEct] = DogIdAndBoolean(DogBreedData.DOGSMALLECT, false)
        return map
    }

    inner class DogIdAndBoolean(var dogId:Long, var boolean:Boolean){
    }
}
