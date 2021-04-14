package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OutgoingDetails implements Serializable {


    @SerializedName("ProductBoxID")
    private int productBoxID;
    @SerializedName("Quantity")
    private double quantity;
    @SerializedName("Weight")
    private double weight;

    public OutgoingDetails() {
    }

    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}
