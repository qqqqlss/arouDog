package com.example.aroundog.utils

class DogBreedData {
    companion object{
        const val DOGLENGTH = 18L
        const val HUSKY = 1L
        const val SAMOYED = 2L
        const val RETRIEVER = 3L
        const val SHEPHERD = 4L
        const val MALAMUTE = 5L
        const val BEAGLE = 6L
        const val BORDERCOLLIE =7L
        const val BULLDOG =8L
        const val SHIBA =9L
        const val WELSHCORGI =10L
        const val CHIHUAHUA =11L
        const val MALTESE =12L
        const val POODLE =13L
        const val SHIHTZU =14L
        const val YORKSHIRETERRIER =15L
        const val DOGBITECT = 16L
        const val DOGMEDIUMECT = 17L
        const val DOGSMALLECT = 18L

        @JvmField
        val dogList = getMap()

        fun getMap():HashMap<Long, String>{
            val map = HashMap<Long, String>()
            map[HUSKY] = "허스키"
            map[SAMOYED] = "사모예드"
            map[RETRIEVER] = "리트리버"
            map[SHEPHERD] = "셰퍼드"
            map[MALAMUTE] = "말라뮤트"
            map[BEAGLE] = "비글"
            map[BORDERCOLLIE] = "보더콜리"
            map[BULLDOG] = "불독"
            map[SHIBA] = "시바"
            map[WELSHCORGI] = "웰시코기"
            map[CHIHUAHUA] = "치와와"
            map[MALTESE] = "말티즈"
            map[POODLE] = "푸들"
            map[SHIHTZU] = "시츄"
            map[YORKSHIRETERRIER] = "요크셔테리어"
            map[DOGBITECT] = "대형견 - 기타"
            map[DOGMEDIUMECT] = "중형견 - 기타"
            map[DOGSMALLECT] = "소형견 - 기타"
            return map
        }

        @JvmStatic
        fun getBreed(id:Long):String{
            for (entry in dogList) {
                if(entry.key == id)
                    return entry.value
            }
            return "오류"
        }

        @JvmStatic
        fun getId(name:String):Long{
            for (entry in dogList) {
                if (entry.value == name) {
                    return entry.key
                }
            }
            return -1L
        }
    }
}