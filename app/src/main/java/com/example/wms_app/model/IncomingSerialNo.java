package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class IncomingSerialNo implements Serializable {

    @SerializedName("ProductID")
    private int productBoxID;
    @SerializedName("SerialNo")
    private String serialNo;
    private boolean isPresent;

    public IncomingSerialNo() {
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

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }
}
