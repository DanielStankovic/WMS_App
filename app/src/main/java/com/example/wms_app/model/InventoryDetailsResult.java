package com.example.wms_app.model;

import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

public class InventoryDetailsResult implements Serializable {

    @SerializedName("InventoryID")
    private String inventoryID;
    @SerializedName("ProductBoxID")
    private int productBoxID;
    @SerializedName("Quantity")
    private double quantity;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("WPositionID")
    private int wPositionID;

    @SerializedName("EmployeeID")
    private int employeeID;

    @SerializedName("CreateDate")
    @TypeConverters({TimestampConverter.class})
    private Date createDate;
    @SerializedName("Scanned")
    private boolean scanned;

    @SerializedName("StringDate")
    private String stringDate;

    private String warehousePositionBarcode;
    private boolean sent;

    public InventoryDetailsResult() {
    }

    public String getInventoryID() {
        return inventoryID;
    }

    public void setInventoryID(String inventoryID) {
        this.inventoryID = inventoryID;
    }

    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getwPositionID() {
        return wPositionID;
    }

    public void setwPositionID(int wPositionID) {
        this.wPositionID = wPositionID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
        this.stringDate = Utility.getStringFromDateForServer(createDate);
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getStringDate() {
        return stringDate;
    }

    public String getWarehousePositionBarcode() {
        return warehousePositionBarcode;
    }

    public void setWarehousePositionBarcode(String warehousePositionBarcode) {
        this.warehousePositionBarcode = warehousePositionBarcode;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
