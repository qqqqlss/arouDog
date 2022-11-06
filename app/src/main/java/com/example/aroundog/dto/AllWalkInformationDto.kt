package com.example.aroundog.dto

import java.time.YearMonth

data class AllWalkInformationDto(val summaryTotalData:Array<Long>, val summaryMonthData:Map<String, Array<Long>>, val monthData:Map<String, ArrayList<MonthInformationDto>>, val hasWalkData:Boolean) {
}