package com.example.aroundog

import android.location.Location
import java.io.Serializable

class LastLocation(location: Location): Serializable {
    var location: Location

    init {
        this.location=location
    }
}
