package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "ProductCategory")
public class ProductCategory {
    @PrimaryKey()
    @SerializedName("ProductCategoryID")
    @ColumnInfo(name = "ProductCategoryID")
    private int productCategoryID;

    @SerializedName("ProductCategoryName")
    @ColumnInfo(name = "ProductCategoryName")
    private String productCategoryName;

    @SerializedName("ProductCategoryCode")
    @ColumnInfo(name = "ProductCategoryCode")
    private String productCategoryCode;


    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private String modifiedDate;

    public ProductCategory(){}

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getProductCategoryCode() {
        return productCategoryCode;
    }

    public void setProductCategoryCode(String productCategoryCode) {
        this.productCategoryCode = productCategoryCode;
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
}
