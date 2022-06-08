package com.example.aroundog.Model;

import com.google.gson.annotations.SerializedName;

public class CheckSuccess {

    @SerializedName("success")
    private boolean success;

    public CheckSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
