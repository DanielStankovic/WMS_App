package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WarehouseStatusPosition implements Serializable {


    @SerializedName("WPositionID")
    private int wPositionID;
    @SerializedName("WSubPositionID")
    private int wSubPositionID;
    @SerializedName("ForPreloading")
    private boolean forPreloading;
    @SerializedName("WarehouseCode")
    private String warehouseCode;
    @SerializedName("WspDetails")
    private List<WarehouseStatusPositionDetails> wspDetails;

    private boolean isLocked;

    private int lockedEmployeeID;
    private String warehousePositionBarcode;

    public WarehouseStatusPosition() {
    }


    public int getwPositionID() {
        return wPositionID;
    }

    public void setwPositionID(int wPositionID) {
        this.wPositionID = wPositionID;
    }

    public int getwSubPositionID() {
        return wSubPositionID;
    }

    public void setwSubPositionID(int wSubPositionID) {
        this.wSubPositionID = wSubPositionID;
    }

    public boolean isForPreloading() {
        return forPreloading;
    }

    public void setForPreloading(boolean forPreloading) {
        this.forPreloading = forPreloading;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCOde) {
        this.warehouseCode = warehouseCOde;
    }

    public List<WarehouseStatusPositionDetails> getWspDetails() {
        return wspDetails;
    }

    public void setWspDetails(List<WarehouseStatusPositionDetails> wspDetails) {
        this.wspDetails = wspDetails;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getLockedEmployeeID() {
        return lockedEmployeeID;
    }

    public void setLockedEmployeeID(int lockedEmployeeID) {
        this.lockedEmployeeID = lockedEmployeeID;
    }

    public String getWarehousePositionBarcode() {
        return warehousePositionBarcode;
    }

    public void setWarehousePositionBarcode(String warehousePositionBarcode) {
        this.warehousePositionBarcode = warehousePositionBarcode;
    }
}
