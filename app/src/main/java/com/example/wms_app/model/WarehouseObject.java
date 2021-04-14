package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "WarehouseObject")
public class WarehouseObject {


    @PrimaryKey()
    @SerializedName("WObjectID")
    @ColumnInfo(name = "WObjectID")
    private int wObjectID;
    @SerializedName("WObjectName")
    @ColumnInfo(name = "WObjectName")
    private String wObjectName;
    @SerializedName("WObjectCode")
    @ColumnInfo(name = "WObjectCode")
    private String wObjectCode;
    @SerializedName("WarehouseID")
    @ColumnInfo(name = "WarehouseID")
    private int warehouseID;
    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;
    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public WarehouseObject() {
    }

    public int getWObjectID() {
        return wObjectID;
    }

    public void setWObjectID(int wObjectID) {
        this.wObjectID = wObjectID;
    }

    public String getWObjectName() {
        return wObjectName;
    }

    public void setWObjectName(String wObjectName) {
        this.wObjectName = wObjectName;
    }

    public String getWObjectCode() {
        return wObjectCode;
    }

    public void setWObjectCode(String wObjectCode) {
        this.wObjectCode = wObjectCode;
    }

    public int getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(int warehouseID) {
        this.warehouseID = warehouseID;
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
