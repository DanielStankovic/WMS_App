package com.example.wms_app.model;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

import androidx.room.Ignore;

public class IncomingTruckResult implements Serializable {

    @SerializedName("IncomingID")
    private String incomingID;
    @SerializedName("TruckDriver")
    private String truckDriver;
    @SerializedName("LicencePlate")
    private String licencePlate;
    @SerializedName("EmployeeID")
    private int employeeID;
    @SerializedName("CreatedDate")
    private Date createdDate;
    @SerializedName("StringDate")
    private String stringDate;
    private boolean sent;

    @ServerTimestamp
    private Date firebaseCreatedDate;
    @SerializedName("IdrFirebaseID")
    private String itrFirebaseID;

    public IncomingTruckResult() {
    }

    @Ignore
    public IncomingTruckResult(String incomingID, String truckDriver, String licencePlate, boolean sent, int employeeID, Date createdDate) {
        this.incomingID = incomingID;
        this.truckDriver = truckDriver;
        this.licencePlate = licencePlate;
        this.sent = sent;
        this.employeeID = employeeID;
        this.createdDate = createdDate;
        this.stringDate = Utility.getStringFromDateForServer(createdDate);
    }




    public String getIncomingID() {
        return incomingID;
    }

    public void setIncomingID(String incomingID) {
        this.incomingID = incomingID;
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

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        this.stringDate = Utility.getStringFromDateForServer(createdDate);
    }

    public String getStringDate() {
        return stringDate;
    }

    public Date getFirebaseCreatedDate() {
        return firebaseCreatedDate;
    }

    public void setFirebaseCreatedDate(Date firebaseCreatedDate) {
        this.firebaseCreatedDate = firebaseCreatedDate;
    }

    public String getItrFirebaseID() {
        return itrFirebaseID;
    }

    public void setItrFirebaseID(String itrFirebaseID) {
        this.itrFirebaseID = itrFirebaseID;
    }
}
