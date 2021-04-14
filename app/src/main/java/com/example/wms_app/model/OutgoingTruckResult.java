package com.example.wms_app.model;

import androidx.room.Ignore;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

public class OutgoingTruckResult implements Serializable {

    @SerializedName("OutgoingID")
    private String outgoingID;
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
    @SerializedName("OdrFirebaseID")
    private String otrFirebaseID;

    public OutgoingTruckResult() {
    }

    @Ignore
    public OutgoingTruckResult(String outgoingID, String truckDriver, String licencePlate,
                               boolean sent, int employeeID, Date createdDate) {
        this.outgoingID = outgoingID;
        this.truckDriver = truckDriver;
        this.licencePlate = licencePlate;
        this.employeeID = employeeID;
        this.createdDate = createdDate;
        this.stringDate = Utility.getStringFromDateForServer(createdDate);
        ;
        this.sent = sent;
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
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


    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Date getFirebaseCreatedDate() {
        return firebaseCreatedDate;
    }

    public void setFirebaseCreatedDate(Date firebaseCreatedDate) {
        this.firebaseCreatedDate = firebaseCreatedDate;
    }

    public String getOtrFirebaseID() {
        return otrFirebaseID;
    }

    public void setOtrFirebaseID(String otrFirebaseID) {
        this.otrFirebaseID = otrFirebaseID;
    }
}
