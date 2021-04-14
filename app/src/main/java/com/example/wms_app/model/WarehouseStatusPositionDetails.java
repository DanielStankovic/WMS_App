package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import androidx.room.Ignore;

public class WarehouseStatusPositionDetails implements Serializable {

    @SerializedName("ProductBoxID")
    private int productBoxID;
    @SerializedName("Quantity")
    private int quantity;
    @SerializedName("ReservedQuantity")
    private int reservedQuantity;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("ModifiedDate")
    private Date modifiedDate;


    public WarehouseStatusPositionDetails() {
    }

    @Ignore
    public WarehouseStatusPositionDetails(int productBoxID, int quantity) {
        this.productBoxID = productBoxID;
        this.quantity = quantity;
    }


    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }


    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }


    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
