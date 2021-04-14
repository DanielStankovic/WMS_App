package com.example.wms_app.model;

import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

public class Inventory  implements Serializable{

    @SerializedName("InventoryID")
    private String inventoryID;

    @SerializedName("EmployeeID")
    private int employeeID;

    @TypeConverters({TimestampConverter.class})
    private Date docDate;

    @SerializedName("StringDocDate")
    private String stringDocDate;

    @SerializedName("InventoryStatusID")
    private int inventoryStatusID;


    private boolean finished;

    public Inventory (){}

    @Ignore
    public Inventory(int employeeId, boolean b, Date date, String inventoryID, int inventoryStatusID) {
        this.employeeID = employeeId;
        finished = b;
        docDate = date;
        this.stringDocDate = Utility.getStringFromDateForServer(date);
        this.inventoryID = inventoryID;
        this.inventoryStatusID = inventoryStatusID;
    }

    public String getInventoryID() {
        return inventoryID;
    }

    public void setInventoryID(String inventoryID) {
        this.inventoryID = inventoryID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
        this.stringDocDate = Utility.getStringFromDateForServer(docDate);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getInventoryStatusID() {
        return inventoryStatusID;
    }

    public void setInventoryStatusID(int inventoryStatusID) {
        this.inventoryStatusID = inventoryStatusID;
    }
    public String getStringDocDate() {
        return stringDocDate;
    }

}
