package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;

@Entity(tableName = "ProductBox")
public class ProductBox implements Serializable {
    @PrimaryKey
    @SerializedName("ProductBoxID")
    @ColumnInfo(name = "ProductBoxID")
    private int productBoxID;
    @SerializedName("ProductBoxName")
    @ColumnInfo(name = "ProductBoxName")
    private String productBoxName;
    @SerializedName("ProductBoxCode")
    @ColumnInfo(name = "ProductBoxCode")
    private String productBoxCode;
    @SerializedName("ProductBoxBarcode")
    @ColumnInfo(name = "ProductBoxBarcode")
    private String productBoxBarcode;

    @SerializedName("SerialMustScan")
    @ColumnInfo(name = "SerialMustScan")
    private boolean serialMustScan;

    @SerializedName("ProductItemTypeID")
    @ColumnInfo(name = "ProductItemTypeID")
    private int productItemTypeID;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private String modifiedDate;

    private int colorStatus;

    private int expectedQuantity;
    private int addedQuantity;

    public ProductBox() {
    }

    @Ignore
    public ProductBox(int productBoxID, String productBoxName, String productBoxBarcode,
                      boolean serialMustScan, int expectedQuantity, int addedQuantity, String productBoxCode) {
        this.productBoxID = productBoxID;
        this.productBoxName = productBoxName;
        this.productBoxBarcode = productBoxBarcode;
        this.serialMustScan = serialMustScan;
        this.expectedQuantity = expectedQuantity;
        this.addedQuantity = addedQuantity;
        this.productBoxCode = productBoxCode;
    }

    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }

    public String getProductBoxName() {
        return productBoxName;
    }

    public void setProductBoxName(String productBoxName) {
        this.productBoxName = productBoxName;
    }

    public String getProductBoxCode() {
        return productBoxCode;
    }

    public void setProductBoxCode(String productBoxCode) {
        this.productBoxCode = productBoxCode;
    }

    public String getProductBoxBarcode() {
        return productBoxBarcode;
    }

    public void setProductBoxBarcode(String productBoxBarcode) {
        this.productBoxBarcode = productBoxBarcode;
    }

    public boolean isSerialMustScan() {
        return serialMustScan;
    }

    public void setSerialMustScan(boolean serialMustScan) {
        this.serialMustScan = serialMustScan;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getColorStatus() {
        return colorStatus;
    }

    public void setColorStatus(int colorStatus) {
        this.colorStatus = colorStatus;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(int expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }


    public int getProductItemTypeID() {
        return productItemTypeID;
    }

    public void setProductItemTypeID(int productItemTypeID) {
        this.productItemTypeID = productItemTypeID;
    }

    public int getAddedQuantity() {
        return addedQuantity;
    }

    public void setAddedQuantity(int addedQuantity) {
        this.addedQuantity = addedQuantity;
    }

    public static ProductBox newPlaceHolderInstance() {
        ProductBox placeholderProductBox = new ProductBox();
        placeholderProductBox.setProductBoxID(-1);
        placeholderProductBox.setProductBoxBarcode("NONE");
        placeholderProductBox.setColorStatus(0);
        placeholderProductBox.setProductBoxName("-Skenirajte/Odaberite poziciju-");
        return placeholderProductBox;
    }

    @NonNull
    @Override
    public String toString() {
       if(getProductBoxCode() != null)
             return getProductBoxCode() + ": " + getProductBoxName();
       else
           return getProductBoxName();
    }
}
