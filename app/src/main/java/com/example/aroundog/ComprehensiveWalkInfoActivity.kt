package com.example.aroundog

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import com.example.aroundog.Service.DogService
import com.example.aroundog.Service.UserService
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.AllWalkInformationDto
import com.example.aroundog.dto.MonthInformationDto
import com.google.gson.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashSet

class ComprehensiveWalkInfoActivity : AppCompatActivity() {
    val TAG = "COMPREHENSIVEWALKINFOACTIVITY"
    lateinit var calendar: MaterialCalendarView

    lateinit var totalSecond: TextView
    lateinit var totalCount: TextView
    lateinit var totalDistance: TextView
    lateinit var monthSecond: TextView
    lateinit var monthCount: TextView
    lateinit var monthDistance: TextView
    lateinit var monthSummaryData:TextView

    lateinit var allWalkInfoData:AllWalkInformationDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprehensive_walk_info)

        setView()

        calendar.setOnMonthChangedListener { widget, date ->
            monthSummaryData.text = "" + date.month +" 월 산책 정보"
            if (this::allWalkInfoData.isInitialized) {
                var yearMonth = ""+date.year + "-" + date.month
                //초, m
                if (allWalkInfoData.summaryMonthData.containsKey(yearMonth)) {
                    monthSecond.text = String.format("%.1f", (allWalkInfoData.summaryMonthData[yearMonth]!![0] / 60.0 / 60.0)) + "시간"
                    monthDistance.text = String.format("%.2f", (allWalkInfoData.summaryMonthData[yearMonth]!![1] / 1000.0))+ " KM"
                    monthCount.text = allWalkInfoData.summaryMonthData[yearMonth]!![2].toString() + " 회"
                }
                else {
                    monthSecond.text = "0 시간"
                    monthDistance.text = "0 KM"
                    monthCount.text = "0 회"
                }

                //리스트 뷰애 들어갈 데이터 바꿔치기
            }
        }
        
        //retrofit 정보 받아와야함
        //전체 산책 요약정보, 전체 월 요약 정보
        //유저 산책 정보리스트
        //걍 전체 정보 가져오쟈
        //저장된 id 정보 가져오기
        var user_info_pref =
            this.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        var userId = user_info_pref.getString("id", "error").toString()
        var jsonLocalDateTimeDeserializer = object:JsonDeserializer<LocalDateTime>{
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): LocalDateTime {
                return LocalDateTime.parse(json!!.asString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            }

        }
        var gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, jsonLocalDateTimeDeserializer).create()


        var retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WalkService::class.java)

        var dates = HashSet<CalendarDay>()

        retrofit.getAllWalkInfo(userId).enqueue(object : Callback<AllWalkInformationDto> {
            override fun onResponse(
                call: Call<AllWalkInformationDto>,
                response: Response<AllWalkInformationDto>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.hasWalkData) {
                        allWalkInfoData = response.body()!!

                        //전체 산책 요약 정보 설정
                        totalSecond.text = String.format("%.1f", (allWalkInfoData.summaryTotalData[0] / 60.0 / 60.0)) + "시간"
                        totalDistance.text = String.format("%.2f", (allWalkInfoData.summaryTotalData[1] / 1000.0)) + " KM"
                        totalCount.text = allWalkInfoData.summaryTotalData[2].toString() + " 회"

                        var nowYearMonth = YearMonth.now()
                        var nowYearMonthStr = "" + nowYearMonth.year + "-" + nowYearMonth.monthValue
                        val monthData = allWalkInfoData.summaryMonthData[nowYearMonthStr]
                        
                        //오늘을 기준으로 월 산책 요약 정보 설정
                        if (!monthData.isNullOrEmpty()) {
                            monthSecond.text = String.format("%.1f", monthData[0] / 60.0 / 60.0) + " 시간"
                            monthDistance.text = String.format("%.2f", monthData[1] / 1000.0) + " KM"
                            monthCount.text = monthData[2].toString() + " 회"
                        }

                        monthSummaryData.text = "" + nowYearMonth.monthValue +" 월 산책 정보"

                        for (data in allWalkInfoData.monthData) {
                            var yearMonth = data.key //"like 2022-10"

                            var monthWalkInfoList:ArrayList<MonthInformationDto> = data.value
                            for (monthInformationDto in monthWalkInfoList) {
                                var startTime = monthInformationDto.startTime
                                val calendarDay = CalendarDay.from(
                                    startTime.year,
                                    startTime.monthValue,
                                    startTime.dayOfMonth
                                )
                                //달력의 데코레이터 추가에 사용하는 dates에 날짜 추가
                                dates.add(calendarDay)
                            }
                        }
                        calendar.addDecorator(EventDecorator(dates))
                    } else {

                    }
                }
            }

            override fun onFailure(call: Call<AllWalkInformationDto>, t: Throwable) {
                Log.d(TAG, "실패 ",t)
            }

        })
    }

    fun setView(){
        calendar = findViewById(R.id.calendarView)

        totalSecond = findViewById(R.id.totalSecond)
        totalCount = findViewById(R.id.totalCount)
        totalDistance = findViewById(R.id.totalDistance)

        monthSummaryData = findViewById(R.id.monthSummaryData)
        monthSecond = findViewById(R.id.monthSecond)
        monthCount = findViewById(R.id.monthCount)
        monthDistance = findViewById(R.id.monthDistance)







    }
    class EventDecorator(dates: Collection<CalendarDay>): DayViewDecorator {

        var dates: HashSet<CalendarDay> = HashSet(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(10F, Color.parseColor("#ebdab3")))
        }
    }
}
