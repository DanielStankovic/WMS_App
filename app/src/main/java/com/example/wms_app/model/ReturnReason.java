package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "ReturnReason")
public class ReturnReason {

    @PrimaryKey()
    @SerializedName("ReturnReasonID")
    @ColumnInfo(name = "ReturnReasonID")
    private int returnReasonID;
    @SerializedName("ReturnReasonCode")
    @ColumnInfo(name = "ReturnReasonCode")
    private String returnReasonCode;
    @SerializedName("ReturnReasonText")
    @ColumnInfo(name = "ReturnReasonText")
    private String returnReasonText;
    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;
    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public ReturnReason() {
    }

    public int getReturnReasonID() {
        return returnReasonID;
    }

    public void setReturnReasonID(int returnReasonID) {
        this.returnReasonID = returnReasonID;
    }

    public String getReturnReasonCode() {
        return returnReasonCode;
    }

    public void setReturnReasonCode(String returnReasonCode) {
        this.returnReasonCode = returnReasonCode;
    }

    public String getReturnReasonText() {
        return returnReasonText;
    }

    public void setReturnReasonText(String returnReasonText) {
        this.returnReasonText = returnReasonText;
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
}
