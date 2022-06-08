package com.example.aroundog.Model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("success")
    private boolean success;
    @SerializedName("id")
    private String id;
    @SerializedName("password")
    private String password;
    @SerializedName("image")
    private int image;
    @SerializedName("name")
    private String name;
    @SerializedName("phone")
    private String phone;
    @SerializedName("email")
    private String email;

    public User(boolean success, String id, String password, int image, String name, String phone, String email) {
        this.success = success;
        this.id = id;
        this.password = password;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
