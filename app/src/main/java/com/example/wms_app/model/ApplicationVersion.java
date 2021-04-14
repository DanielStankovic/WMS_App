package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "ApplicationVersion")
public class ApplicationVersion {


    @PrimaryKey
    @ColumnInfo(name = "ApplicationVersionID")
    @SerializedName("ApplicationVersionID")
    private int applicationVersionID;

    @ColumnInfo(name = "VersionCode")
    @SerializedName("VersionCode")
    private int versionCode;

    @ColumnInfo(name = "VersionName")
    @SerializedName("VersionName")
    private String versionName;

    @ColumnInfo(name = "Description")
    @SerializedName("Description")
    private String description;

    @ColumnInfo(name = "DownloadLink")
    @SerializedName("DownloadLink")
    private String downloadLink;

    @ColumnInfo(name = "CreatedDate")
    @SerializedName("CreatedDate")
    @TypeConverters({TimestampConverter.class})
    private Date createdDate;


    public ApplicationVersion(int applicationVersionID, int versionCode, String versionName,
                              String description, String downloadLink, Date createdDate) {
        this.applicationVersionID = applicationVersionID;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.description = description;
        this.downloadLink = downloadLink;
        this.createdDate = createdDate;
    }

    public int getApplicationVersionID() {
        return applicationVersionID;
    }

    public void setApplicationVersionID(int applicationVersionID) {
        this.applicationVersionID = applicationVersionID;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
