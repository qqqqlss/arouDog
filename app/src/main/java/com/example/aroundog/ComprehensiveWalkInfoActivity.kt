package com.example.aroundog

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.Model.WalkRecyclerViewAdapter
import com.example.aroundog.Model.WalkRecyclerViewItem
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.AllWalkInformationDto
import com.example.aroundog.dto.MonthInformationDto
import com.google.gson.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class ComprehensiveWalkInfoActivity : AppCompatActivity() {
    val TAG = "COMPREHENSIVEWALKINFOACTIVITY"

    //뷰 바인딩
    lateinit var calendar: MaterialCalendarView
    lateinit var totalSecond: TextView
    lateinit var totalCount: TextView
    lateinit var totalDistance: TextView
    lateinit var monthSecond: TextView
    lateinit var monthCount: TextView
    lateinit var monthDistance: TextView
    lateinit var monthSummaryData:TextView
    lateinit var walkRecyclerView:RecyclerView

    //전체 산책 정보
    lateinit var allWalkInfoData:AllWalkInformationDto

    //산책 정보가 있는 날 저장
    var hasWalkDates = HashSet<CalendarDay>()

    var mLayoutManager = LinearLayoutManager(this);
    var defaultList = ArrayList<WalkRecyclerViewItem>()//어댑터 초기화를 위한 리스트
    var adapter = WalkRecyclerViewAdapter(defaultList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprehensive_walk_info)

//        //https://sinwho.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-recyclerview-%EC%A0%9C%EC%9D%BC-%EC%9C%84%EB%A1%9C-%EC%9D%B4%EB%8F%99%EC%8B%9C%ED%82%A4%EA%B8%B0
//        https://bumjae.tistory.com/6
//        https://hanyeop.tistory.com/255
//
//
//        https://youngest-programming.tistory.com/374
//
//        https://onlyfor-me-blog.tistory.com/437
//
//        https://github.com/prolificinteractive/material-calendarview/wiki/Decorators
//        https://dpdpwl.tistory.com/3
//        https://gdbagooni.tistory.com/19
//
//        https://velog.io/@nezhitsya/%ED%95%9C%EC%9D%B4%EC%9D%8C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-Custom-Calendar

        //뷰 바인딩
        setView()

        //년 월 표시방법 변경
        updateCalendarHeader()

        //리사이클러뷰 설정
        walkRecyclerView.layoutManager = mLayoutManager;
        walkRecyclerView.adapter = adapter

        //월 변경될때
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

                //월 전체 산책 정보 리사이클러뷰에 추가
                //리사이클러 뷰에 들어갈 데이터 바꿔치기
                var temp = ArrayList<WalkRecyclerViewItem>()
                val dateStr = ""+ date.year + "-" + date.month
                val monthDataList = allWalkInfoData.monthData[dateStr]
                if (!monthDataList.isNullOrEmpty()) {
                    for (monthData in monthDataList!!) {
                        var hourStr = monthData.startTime.format(DateTimeFormatter.ofPattern("a HH:mm"))
                        temp.add(
                            WalkRecyclerViewItem(
                                monthData.walkId,
                                monthData.startTime.dayOfMonth,
                                monthData.second,
                                monthData.distance,
                                hourStr
                            )
                        )
                    }
                }
                //리스트뷰 업데이트
                //비어있으면 비어있는채로 업데이트
                adapter.swap(temp)
            }
        }

        calendar.setOnDateChangedListener { widget, date, selected ->
            if (this::allWalkInfoData.isInitialized) {
                var day = CalendarDay.from(date.year, date.month, date.day)
                if (hasWalkDates.contains(day)) {
                    //선택 효과 o
                    widget.addDecorators(HasWalkDecorator(hasWalkDates), SelectDecorator(date))

                    //리사이클러뷰에 보이는 첫번째 목록으로 해당 일 선택
                    val dateStr = "" + date.year + "-" + date.month
                    val monthDataList = allWalkInfoData.monthData[dateStr]
                    var index = 0
                    if (!monthDataList.isNullOrEmpty()) {
                        for (monthInformationDto in monthDataList) {
                            val startTime = monthInformationDto.startTime
                            if (date == CalendarDay.from(
                                    startTime.year,
                                    startTime.monthValue,
                                    startTime.dayOfMonth
                                )
                            ) {
                                index = monthDataList.indexOf(monthInformationDto)
                                break
                            }
                        }
                        //원하는 아이템을 리사이클러뷰 맨 위로 올리기
//                        (walkRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index,0)

                        //스크롤 부드럽게
                        val snapMode: Int = LinearSmoothScroller.SNAP_TO_START
                        val smoothScroller = object : LinearSmoothScroller(applicationContext) {
                            override fun getVerticalSnapPreference(): Int = snapMode
                            override fun getHorizontalSnapPreference(): Int = snapMode
                        }
                        smoothScroller.targetPosition = index
                        mLayoutManager?.startSmoothScroll(smoothScroller)
                    }
                }
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

        //레트로핏 설정
        var retrofit = setRetrofit()

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

                        var todayYearMonth = YearMonth.now()
                        var nowYearMonthStr = "" + todayYearMonth.year + "-" + todayYearMonth.monthValue

                        //월 정보 가져옴
                        val monthData = allWalkInfoData.summaryMonthData[nowYearMonthStr]

                        //오늘을 기준으로 월 산책 요약 정보 설정
                        if (!monthData.isNullOrEmpty()) {
                            monthSecond.text = String.format("%.1f", monthData[0] / 60.0 / 60.0) + " 시간"
                            monthDistance.text = String.format("%.2f", monthData[1] / 1000.0) + " KM"
                            monthCount.text = monthData[2].toString() + " 회"
                        }

                        //월에 따라 변하게
                        monthSummaryData.text = "" + todayYearMonth.monthValue +" 월 산책 정보"

                        //월 전체 산책 정보 리사이클러뷰에 추가
                        var temp = ArrayList<WalkRecyclerViewItem>()
                        val monthDataList = allWalkInfoData.monthData[nowYearMonthStr]
                        for (monthData in monthDataList!!) {
                            var hourStr = monthData.startTime.format(DateTimeFormatter.ofPattern("a HH:mm"))
                            temp.add(WalkRecyclerViewItem(monthData.walkId, monthData.startTime.dayOfMonth, monthData.second, monthData.distance, hourStr))
                        }

                        //리스트뷰 업데이트
                        adapter.swap(temp)

                        //산책 있는 날 목록 저장
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
                                hasWalkDates.add(calendarDay)
                            }
                        }
                        calendar.addDecorator(HasWalkDecorator(hasWalkDates))//산책 있는날 원 추가
                        calendar.addDecorators(DayDisableDecorator(hasWalkDates))//산책 없는날 선택 안되게
                    } else {

                    }
                }
            }

            override fun onFailure(call: Call<AllWalkInformationDto>, t: Throwable) {
                Log.d(TAG, "실패 ",t)
            }

        })
//        var dates = HashSet<CalendarDay>()
//        dates.add(CalendarDay.from(2022, 11,5))
//        calendar.addDecorator(EventDecorator(dates))
    }

    private fun updateCalendarHeader() {
        calendar.setTitleFormatter(object : TitleFormatter {
            override fun format(day: CalendarDay?): CharSequence {
                return "${day!!.year}년  ${day.month}월"
            }
        })
    }

    private fun setRetrofit(): WalkService {
        var jsonLocalDateTimeDeserializer = object : JsonDeserializer<LocalDateTime> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): LocalDateTime {
                return LocalDateTime.parse(
                    json!!.asString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                )
            }

        }
        var gson = GsonBuilder().registerTypeAdapter(
            LocalDateTime::class.java,
            jsonLocalDateTimeDeserializer
        ).create()


        var retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WalkService::class.java)
        return retrofit
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

        walkRecyclerView = findViewById(R.id.walkRecyclerView)


    }
    class HasWalkDecorator(dates: Collection<CalendarDay>): DayViewDecorator {

        var dates: HashSet<CalendarDay> = HashSet(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(12F, Color.parseColor("#ebdab3")))
        }
    }

    class DayDisableDecorator(dates: Collection<CalendarDay>): DayViewDecorator {

        var dates: HashSet<CalendarDay> = HashSet(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return !dates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setDaysDisabled(true)
        }
    }
    class SelectDecorator(selectDay: CalendarDay): DayViewDecorator {
        var selectDay = selectDay
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return selectDay == day
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(12F, Color.parseColor("#675E4D")))
        }
    }
}
