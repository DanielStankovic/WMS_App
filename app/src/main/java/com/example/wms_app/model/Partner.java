package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "Partner")
public class Partner {

    @PrimaryKey()
    @SerializedName("PartnerID")
    @ColumnInfo(name = "PartnerID")
    private int partnerID;

    @SerializedName("PartnerCode")
    @ColumnInfo(name = "PartnerCode")
    private String partnerCode;

    @SerializedName("PartnerName")
    @ColumnInfo(name = "PartnerName")
    private String partnerName;

    @SerializedName("PartnerShortName")
    @ColumnInfo(name = "PartnerShortName")
    private String partnerShortName;

    @SerializedName("Address")
    @ColumnInfo(name = "Address")
    private String address;

    @SerializedName("City")
    @ColumnInfo(name = "City")
    private String city;

    @SerializedName("CityCode")
    @ColumnInfo(name = "CityCode")
    private String cityCode;

    @SerializedName("Phone")
    @ColumnInfo(name = "Phone")
    private String phone;

    @SerializedName("Fax")
    @ColumnInfo(name = "Fax")
    private String fax;

    @SerializedName("Email")
    @ColumnInfo(name = "Email")
    private String email;

    @SerializedName("TaxCode")
    @ColumnInfo(name = "TaxCode")
    private String taxCode;

    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;

    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;


    public Partner(){
    }

    public int getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(int partnerID) {
        this.partnerID = partnerID;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerShortName() {
        return partnerShortName;
    }

    public void setPartnerShortName(String partnerShortName) {
        this.partnerShortName = partnerShortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
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
