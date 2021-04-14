package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.io.Serializable;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "WarehousePosition")
public class WarehousePosition implements Serializable {

    @PrimaryKey()
    @SerializedName("WPositionID")
    @ColumnInfo(name = "WPositionID")
    private int wPositionID;
    @SerializedName("WPositionName")
    @ColumnInfo(name = "WPositionName")
    private String wPositionName;
    @SerializedName("WPositionCode")
    @ColumnInfo(name = "WPositionCode")
    private String wPositionCode;
    @SerializedName("WObjectID")
    @ColumnInfo(name = "WObjectID")
    private int wObjectID;
    @SerializedName("ForReturn")
    @ColumnInfo(name = "ForReturn")
    private boolean forReturn;
    @SerializedName("ForPreloading")
    @ColumnInfo(name = "ForPreloading")
    private boolean forPreloading;
    @SerializedName("ForIncoming")
    @ColumnInfo(name = "ForIncoming")
    private boolean forIncoming;
    @SerializedName("ForOutgoing")
    @ColumnInfo(name = "ForOutgoing")
    private boolean forOutgoing;
    @SerializedName("WarehouseCode")
    @ColumnInfo(name = "WarehouseCode")
    private String warehouseCode;
    @SerializedName("Barcode")
    @ColumnInfo(name = "Barcode")
    private String barcode;
    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;
    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public WarehousePosition() {
    }

    public int getWPositionID() {
        return wPositionID;
    }

    public void setWPositionID(int wPositionID) {
        this.wPositionID = wPositionID;
    }

    public String getWPositionName() {
        return wPositionName;
    }

    public void setWPositionName(String wPositionName) {
        this.wPositionName = wPositionName;
    }

    public String getWPositionCode() {
        return wPositionCode;
    }

    public void setWPositionCode(String wPositionCode) {
        this.wPositionCode = wPositionCode;
    }

    public int getWObjectID() {
        return wObjectID;
    }

    public void setWObjectID(int wObjectID) {
        this.wObjectID = wObjectID;
    }

    public boolean isForReturn() {
        return forReturn;
    }

    public void setForReturn(boolean forReturn) {
        this.forReturn = forReturn;
    }

    public boolean isForPreloading() {
        return forPreloading;
    }

    public void setForPreloading(boolean forPreloading) {
        this.forPreloading = forPreloading;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public boolean isForOutgoing() {
        return forOutgoing;
    }

    public void setForOutgoing(boolean forOutgoing) {
        this.forOutgoing = forOutgoing;
    }

    public int getwPositionID() {
        return wPositionID;
    }

    public void setwPositionID(int wPositionID) {
        this.wPositionID = wPositionID;
    }

    public String getwPositionName() {
        return wPositionName;
    }

    public void setwPositionName(String wPositionName) {
        this.wPositionName = wPositionName;
    }

    public String getwPositionCode() {
        return wPositionCode;
    }

    public void setwPositionCode(String wPositionCode) {
        this.wPositionCode = wPositionCode;
    }

    public int getwObjectID() {
        return wObjectID;
    }

    public void setwObjectID(int wObjectID) {
        this.wObjectID = wObjectID;
    }

    public boolean isForIncoming() {
        return forIncoming;
    }

    public void setForIncoming(boolean forIncoming) {
        this.forIncoming = forIncoming;
    }

    @NonNull
    @Override
    public String toString() {
        return getBarcode();
    }
}
