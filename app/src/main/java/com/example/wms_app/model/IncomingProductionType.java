package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "IncomingProductionType")
public class IncomingProductionType {

    @PrimaryKey()
    @SerializedName("IncomingProductionTypeID")
    @ColumnInfo(name = "IncomingProductionTypeID")
    private int incomingProductionTypeID;

    @SerializedName("IncomingProductionTypeCode")
    @ColumnInfo(name = "IncomingProductionTypeCode")
    private String incomingProductionTypeCode;

    @SerializedName("IncomingProductionTypeDescription")
    @ColumnInfo(name = "IncomingProductionTypeDescription")
    private String incomingProductionTypeDescription;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public IncomingProductionType() {
    }

    public int getIncomingProductionTypeID() {
        return incomingProductionTypeID;
    }

    public void setIncomingProductionTypeID(int incomingProductionTypeID) {
        this.incomingProductionTypeID = incomingProductionTypeID;
    }

    public String getIncomingProductionTypeCode() {
        return incomingProductionTypeCode;
    }

    public void setIncomingProductionTypeCode(String incomingProductionTypeCode) {
        this.incomingProductionTypeCode = incomingProductionTypeCode;
    }

    public String getIncomingProductionTypeDescription() {
        return incomingProductionTypeDescription;
    }

    public void setIncomingProductionTypeDescription(String incomingProductionTypeDescription) {
        this.incomingProductionTypeDescription = incomingProductionTypeDescription;
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
