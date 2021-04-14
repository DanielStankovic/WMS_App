package com.example.wms_app.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "ProductItemType")
public class ProductItemType implements Serializable {

    @PrimaryKey()
    @SerializedName("ProductItemTypeID")
    @ColumnInfo(name = "ProductItemTypeID")
    private int productItemTypeID;

    @SerializedName("Code")
    @ColumnInfo(name = "Code")
    private String code;

    @SerializedName("Name")
    @ColumnInfo(name = "Name")
    private String name;

    @SerializedName("Type")
    @ColumnInfo(name = "Type")
    private String type;

    @SerializedName("TypeName")
    @ColumnInfo(name = "TypeName")
    private String typeName;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;


    public ProductItemType(){}

    public int getProductItemTypeID() {
        return productItemTypeID;
    }

    public void setProductItemTypeID(int productItemTypeID) {
        this.productItemTypeID = productItemTypeID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
