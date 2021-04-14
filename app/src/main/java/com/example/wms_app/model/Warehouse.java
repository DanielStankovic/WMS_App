package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "Warehouse")
public class Warehouse {

    @PrimaryKey()
    @SerializedName("WarehouseID")
    @ColumnInfo(name = "WarehouseID")
    private int warehouseID;
    @SerializedName("WarehouseName")
    @ColumnInfo(name = "WarehouseName")
    private String warehouseName;
    @SerializedName("WarehouseCode")
    @ColumnInfo(name = "WarehouseCode")
    private String warehouseCode;
    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;
    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public Warehouse() {
    }

    public int getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(int warehouseID) {
        this.warehouseID = warehouseID;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
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
