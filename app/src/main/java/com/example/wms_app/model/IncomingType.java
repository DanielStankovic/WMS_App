package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "IncomingType")
public class IncomingType {
    @PrimaryKey()
    @SerializedName("IncomingTypeID")
    @ColumnInfo(name = "IncomingTypeID")
    private int incomingTypeID;

    @SerializedName("IncomingTypeName")
    @ColumnInfo(name = "IncomingTypeName")
    private String incomingTypeName;

    @SerializedName("IncomingTypeCode")
    @ColumnInfo(name = "IncomingTypeCode")
    private String incomingTypeCode;

    @SerializedName("Description")
    @ColumnInfo(name = "Description")
    private String description;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public IncomingType() {
    }

    public int getIncomingTypeID() {
        return incomingTypeID;
    }

    public void setIncomingTypeID(int incomingTypeID) {
        this.incomingTypeID = incomingTypeID;
    }

    public String getIncomingTypeName() {
        return incomingTypeName;
    }

    public void setIncomingTypeName(String incomingTypeName) {
        this.incomingTypeName = incomingTypeName;
    }

    public String getIncomingTypeCode() {
        return incomingTypeCode;
    }

    public void setIncomingTypeCode(String incomingTypeCode) {
        this.incomingTypeCode = incomingTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
