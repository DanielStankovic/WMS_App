package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "Product")
public class Product implements Serializable {
    @PrimaryKey()
    @SerializedName("ProductID")
    @ColumnInfo(name = "ProductID")
    private int productID;

    @SerializedName("ProductName")
    @ColumnInfo(name = "ProductName")
    private String productName;

    @SerializedName("ProductCode")
    @ColumnInfo(name = "ProductCode")
    private String productCode;

    @SerializedName("ProductCodeSAP")
    @ColumnInfo(name = "ProductCodeSAP")
    private String productCodeSAP;

    @SerializedName("ProductCodeBBIS")
    @ColumnInfo(name = "ProductCodeBBIS")
    private String productCodeBBIS;

    @SerializedName("ProductCategoryID")
    @ColumnInfo(name = "ProductCategoryID")
    private int productCategoryID;

    @SerializedName("Barcode")
    @ColumnInfo(name = "Barcode")
    private String barcode;

    @SerializedName("BarcodeTransport")
    @ColumnInfo(name = "BarcodeTransport")
    private String barcodeTransport;

    @SerializedName("BarcodeAdditional")
    @ColumnInfo(name = "BarcodeAdditional")
    private String barcodeAdditional;

    @SerializedName("Cubic")
    @ColumnInfo(name = "Cubic")
    private double cubic;

    @SerializedName("Weight")
    @ColumnInfo(name = "Weight")
    private double weight;

    @SerializedName("MeasureUnit")
    @ColumnInfo(name = "MeasureUnit")
    private String measureUnit;

    @SerializedName("PackageQuantity")
    @ColumnInfo(name = "PackageQuantity")
    private int packageQuantity;

    @SerializedName("PalletQuantity")
    @ColumnInfo(name = "PalletQuantity")
    private int palletQuantity;

    @SerializedName("SerialMustScan")
    @ColumnInfo(name = "SerialMustScan")
    private boolean serialMustScan;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private String modifiedDate;

    private int colorStatus;




    public Product(){}

    @Ignore
    public Product(String productName, String productCode, String barcode, String barcodeAdditional, int packageQuantity) {
        this.productName = productName;
        this.productCode = productCode;
        this.barcode = barcode;
        this.barcodeAdditional = barcodeAdditional;
        this.packageQuantity = packageQuantity;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCodeSAP() {
        return productCodeSAP;
    }

    public void setProductCodeSAP(String productCodeSAP) {
        this.productCodeSAP = productCodeSAP;
    }

    public String getProductCodeBBIS() {
        return productCodeBBIS;
    }

    public void setProductCodeBBIS(String productCodeBBIS) {
        this.productCodeBBIS = productCodeBBIS;
    }

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcodeTransport() {
        return barcodeTransport;
    }

    public void setBarcodeTransport(String barcodeTransport) {
        this.barcodeTransport = barcodeTransport;
    }

    public String getBarcodeAdditional() {
        return barcodeAdditional;
    }

    public void setBarcodeAdditional(String barcodeAdditional) {
        this.barcodeAdditional = barcodeAdditional;
    }

    public double getCubic() {
        return cubic;
    }

    public void setCubic(double cubic) {
        this.cubic = cubic;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public int getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(int packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public int getPalletQuantity() {
        return palletQuantity;
    }

    public void setPalletQuantity(int palletQuantity) {
        this.palletQuantity = palletQuantity;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getColorStatus() {
        return colorStatus;
    }

    public void setColorStatus(int colorStatus) {
        this.colorStatus = colorStatus;
    }


    @NonNull
    @Override
    public String toString() {
        return getProductName();
    }

    public static Product getPlaceholderProduct(){
        Product placeholderProduct = new Product();
        placeholderProduct.setProductID(-1);
        placeholderProduct.setProductCode("HEADER SPINNER");
        placeholderProduct.setBarcode("NONE");
        placeholderProduct.setProductName("-Skenirajte/Odaberite poziciju-");
        return placeholderProduct;
    }
}
