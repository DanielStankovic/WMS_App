package com.example.wms_app.model;

import java.util.Date;

public class IncomingDetailsResultLocal {

    private int quantity;
    private String productBoxName;
    private String productBoxNameAndCode;
    private String serialNumber;
    private int productBoxID;
    private String incomingID;
    private boolean reserved;
    private boolean sent;
    private String positionBarcode;
    private Date createdDate;

    public IncomingDetailsResultLocal(int quantity, String productBoxName, String productBoxNameAndCode,
                                      String serialNumber, int productBoxID, String incomingID,
                                      boolean reserved, boolean sent, String positionBarcode, Date createdDate) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.productBoxNameAndCode = productBoxNameAndCode;
        this.serialNumber = serialNumber;
        this.productBoxID = productBoxID;
        this.incomingID = incomingID;
        this.reserved = reserved;
        this.sent = sent;
        this.positionBarcode = positionBarcode;
        this.createdDate = createdDate;
    }

    public IncomingDetailsResultLocal(int quantity, String productBoxName, String productBoxNameAndCode,
                                      String serialNumber, int productBoxID, String incomingID,
                                      boolean reserved, Date createdDate) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.productBoxNameAndCode = productBoxNameAndCode;
        this.serialNumber = serialNumber;
        this.productBoxID = productBoxID;
        this.incomingID = incomingID;
        this.reserved = reserved;
        this.createdDate = createdDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductBoxName() {
        return productBoxName;
    }

    public void setProductBoxName(String productBoxName) {
        this.productBoxName = productBoxName;
    }

    public String getProductBoxNameAndCode() {
        return productBoxNameAndCode;
    }

    public void setProductBoxNameAndCode(String productBoxNameAndCode) {
        this.productBoxNameAndCode = productBoxNameAndCode;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }

    public String getIncomingID() {
        return incomingID;
    }

    public void setIncomingID(String incomingID) {
        this.incomingID = incomingID;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getPositionBarcode() {
        return positionBarcode;
    }

    public void setPositionBarcode(String positionBarcode) {
        this.positionBarcode = positionBarcode;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
