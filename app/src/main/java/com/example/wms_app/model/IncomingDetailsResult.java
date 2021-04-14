package com.example.wms_app.model;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

import androidx.room.Ignore;
import androidx.room.TypeConverters;

public class IncomingDetailsResult implements Serializable, Cloneable {

    @SerializedName("IncomingID")
    private String incomingId;
    @SerializedName("ProductBoxID")
    private int productBoxID;
    @SerializedName("Quantity")
    private double quantity;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("WPositionID")
    private int wPositionID;
    @SerializedName("WSubPositionID")
    private int wSubPositionID;
    @SerializedName("EmployeeID")
    private int employeeID;
    @SerializedName("CreateDate")
    @TypeConverters({TimestampConverter.class})
    private Date createDate;
    @SerializedName("Scanned")
    private boolean scanned;
    @SerializedName("OnIncoming")
    private boolean onIncoming;
    @SerializedName("StringDate")
    private String stringDate;
    @SerializedName("IsReserved")
    private boolean reserved;
    private String warehousePositionBarcode;
    private boolean sent;

    @ServerTimestamp
    private Date firebaseCreatedDate;
    @SerializedName("IdrFirebaseID")
    private String idrFirebaseID;


    public IncomingDetailsResult() {
    }

    @Ignore
    public IncomingDetailsResult(Date createDate, int employeeID, boolean onIncoming,
                                 int productBoxID, double quantity, boolean scanned,
                                 boolean sent, String serialNo, boolean reserved) {
        this.productBoxID = productBoxID;
        this.quantity = quantity;
        this.serialNo = serialNo;
        this.employeeID = employeeID;
        this.createDate = createDate;
        this.scanned = scanned;
        this.onIncoming = onIncoming;
        this.sent = sent;
        this.reserved = reserved;
        this.stringDate = Utility.getStringFromDateForServer(createDate);
    }

    public String getIncomingId() {
        return incomingId;
    }

    public void setIncomingId(String incomingId) {
        this.incomingId = incomingId;
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

    public int getwSubPositionID() {
        return wSubPositionID;
    }

    public void setwSubPositionID(int wSubPositionID) {
        this.wSubPositionID = wSubPositionID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeid) {
        employeeID = employeeid;
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

    public void setScanned(boolean isScanned) {
        scanned = isScanned;
    }

    public String getStringDate() {
        return stringDate;
    }

    public boolean isOnIncoming() {
        return onIncoming;
    }

    public void setOnIncoming(boolean onIncoming) {
        this.onIncoming = onIncoming;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public String getWarehousePositionBarcode() {
        return warehousePositionBarcode;
    }

    public void setWarehousePositionBarcode(String warehousePositionBarcode) {
        this.warehousePositionBarcode = warehousePositionBarcode;
    }

    public Date getFirebaseCreatedDate() {
        return firebaseCreatedDate;
    }

    public void setFirebaseCreatedDate(Date firebaseCreatedDate) {
        this.firebaseCreatedDate = firebaseCreatedDate;
    }

    public String getIdrFirebaseID() {
        return idrFirebaseID;
    }

    public void setIdrFirebaseID(String idrFirebaseID) {
        this.idrFirebaseID = idrFirebaseID;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


}
