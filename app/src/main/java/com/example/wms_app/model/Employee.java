package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "Employee")
public class Employee {

    @PrimaryKey
    @SerializedName("EmployeeID")
    @ColumnInfo(name = "EmployeeID")
    private int employeeID;

    @SerializedName("Username")
    @ColumnInfo(name = "Username")
    private String username;

    @SerializedName("Password")
    @ColumnInfo(name = "Password")
    private String password;

    @SerializedName("SerialNumber")
    @ColumnInfo(name = "SerialNumber")
    private String serialNumber;

    @SerializedName("FullName")
    @ColumnInfo(name = "FullName")
    private String fullName;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;
    @TypeConverters({TimestampConverter.class})
    private Date lastLoginDate;

    public Employee() {
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
