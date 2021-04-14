package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

public class LoginModel {

    @SerializedName("Username")
    private String username;
    @SerializedName("Password")
    private String password;
    @SerializedName("DeviceSerialNumber")
    private String deviceSerialNumber;

    public LoginModel(String username, String password, String deviceSerialNumber) {
        this.username = username;
        this.password = password;
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }
}
