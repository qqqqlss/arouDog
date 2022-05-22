package com.example.aroundog

import android.location.Location
import com.naver.maps.geometry.LatLng
import java.io.Serializable

class SerialLatLng(latLng: LatLng):Serializable {
    var latLng: LatLng

    init {
        this.latLng = latLng
    }

}
