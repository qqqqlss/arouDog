package com.example.aroundog.dto;

import com.example.aroundog.Model.Gender;

public class UserDto {
    private String userId;
    private String password;
    private Integer age;
    private int image;
    private String userName;
    private String phone;
    private String email;
    private Gender gender;
    private Boolean success;

    public Boolean isSuccess() {
        return success;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public Integer getAge() {
        return age;
    }

    public int getImage() {
        return image;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public UserDto() {

    }

    public UserDto(String userId, String password, Integer age, int image, String userName, String phone, String email, Gender gender, Boolean success) {
        this.userId = userId;
        this.password = password;
        this.age = age;
        this.image = image;
        this.userName = userName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.success = success;
    }
}
