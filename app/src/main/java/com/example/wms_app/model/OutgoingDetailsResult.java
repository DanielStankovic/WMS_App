package com.example.wms_app.model;

import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.gson.annotations.SerializedName;
import com.example.wms_app.utilities.TimestampConverter;
import com.example.wms_app.utilities.Utility;

import java.io.Serializable;
import java.util.Date;

public class OutgoingDetailsResult implements Serializable, Cloneable {

    @SerializedName("OutgoingID")
    private String outgoingID;
    @SerializedName("ProductBoxID")
    private int productBoxID;
    @SerializedName("Quantity")
    private double quantity;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("WPositionBarcode")
    private String wPositionBarcode;
    @SerializedName("EmployeeID")
    private int employeeID;
    @SerializedName("CreateDate")
    @TypeConverters({TimestampConverter.class})
    private Date createDate;
    @SerializedName("Scanned")
    private boolean scanned;

    @SerializedName("StringDate")
    private String stringDate;

    private boolean sent;

    @Exclude
    private boolean isReserveQtyPromptAsked;

    @ServerTimestamp
    private Date firebaseCreatedDate;

    @SerializedName("OdrFirebaseID")
    private String odrFirebaseID;

    public OutgoingDetailsResult() {
    }


    @Ignore
    public OutgoingDetailsResult(int productBoxID, String serialNo, double quantity,
                                 String wPositionBarcode, boolean scanned,
                                 boolean isReserveQtyPromptAsked, int employeeID) {
        this.productBoxID = productBoxID;
        this.quantity = quantity;
        this.serialNo = serialNo;
        this.wPositionBarcode = wPositionBarcode;
        this.scanned = scanned;
        this.isReserveQtyPromptAsked = isReserveQtyPromptAsked;
        this.createDate = new Date();
        this.employeeID = employeeID;
        this.stringDate = Utility.getStringFromDateForServer(this.createDate);
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
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

    public String getwPositionBarcode() {
        return wPositionBarcode;
    }

    public void setwPositionBarcode(String wPositionBarcode) {
        this.wPositionBarcode = wPositionBarcode;
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

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Exclude
    public boolean isReserveQtyPromptAsked() {
        return isReserveQtyPromptAsked;
    }

    @Exclude
    public void setReserveQtyPromptAsked(boolean reserveQtyPromptAsked) {
        isReserveQtyPromptAsked = reserveQtyPromptAsked;
    }

    public Date getFirebaseCreatedDate() {
        return firebaseCreatedDate;
    }

    public void setFirebaseCreatedDate(Date firebaseCreatedDate) {
        this.firebaseCreatedDate = firebaseCreatedDate;
    }

    public String getOdrFirebaseID() {
        return odrFirebaseID;
    }

    public void setOdrFirebaseID(String odrFirebaseID) {
        this.odrFirebaseID = odrFirebaseID;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
