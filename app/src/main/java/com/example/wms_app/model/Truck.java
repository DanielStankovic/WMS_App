package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "Truck")
public class Truck {

    @PrimaryKey()
    @SerializedName("TruckID")
    @ColumnInfo(name = "TruckID")
    private int truckID;
    @SerializedName("TruckName")
    @ColumnInfo(name = "TruckName")
    private String truckName;
    @SerializedName("LicencePlate")
    @ColumnInfo(name = "LicencePlate")
    private String licencePlate;
    @SerializedName("Cubic")
    @ColumnInfo(name = "Cubic")
    private double cubic;
    @SerializedName("IsActive")
    @ColumnInfo(name = "IsActive")
    private boolean isActive;
    @SerializedName("ModifiedDate")
    @ColumnInfo(name = "ModifiedDate")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedDate;

    public Truck() {
    }

    public int getTruckID() {
        return truckID;
    }

    public void setTruckID(int truckID) {
        this.truckID = truckID;
    }

    public String getTruckName() {
        return truckName;
    }

    public void setTruckName(String truckName) {
        this.truckName = truckName;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public double getCubic() {
        return cubic;
    }

    public void setCubic(double cubic) {
        this.cubic = cubic;
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
