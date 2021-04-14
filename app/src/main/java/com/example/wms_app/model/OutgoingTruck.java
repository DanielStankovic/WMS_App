package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OutgoingTruck implements Serializable {


    @SerializedName("TruckDriver")
    private String truckDriver;
    @SerializedName("LicencePlate")
    private String licencePlate;

    public OutgoingTruck() {
    }

    public String getTruckDriver() {
        return truckDriver;
    }

    public void setTruckDriver(String truckDriver) {
        this.truckDriver = truckDriver;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }
}
