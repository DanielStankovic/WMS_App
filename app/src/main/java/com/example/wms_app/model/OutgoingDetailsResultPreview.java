package com.example.wms_app.model;

import java.util.Date;

public class OutgoingDetailsResultPreview {
    private int quantity;
    private String productBoxName;
    private String productBoxCodeAndName;
    private String productBoxCode;
    private String positionBarcode;
    private String serialNumber;
    private int colorStatus;
    private int expectedQuantity;
    private int productBoxID;
    private int productTypeID;
    private boolean isSent;
    private Date createdDate;
    private String outgoingID;

    public OutgoingDetailsResultPreview(int quantity, String productBoxCodeAndName,
                                        String positionBarcode, String serialNumber) {
        this.quantity = quantity;
        this.productBoxCodeAndName = productBoxCodeAndName;
        this.positionBarcode = positionBarcode;
        this.serialNumber = serialNumber;
    }

    public OutgoingDetailsResultPreview(int quantity, String productBoxName,
                                        String productBoxCode, int colorStatus, int expectedQuantity,
                                        int productBoxID) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.productBoxCode = productBoxCode;
        this.colorStatus = colorStatus;
        this.expectedQuantity = expectedQuantity;
        this.productBoxID = productBoxID;
    }
    //DUSAN DODAO KONSTRUKTOR - POTREBAN ZA FILTER
    public OutgoingDetailsResultPreview(int quantity, String productBoxName,
                                        String productBoxCode, int colorStatus, int expectedQuantity,
                                        int productBoxID, int productTypeID) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.productBoxCode = productBoxCode;
        this.colorStatus = colorStatus;
        this.expectedQuantity = expectedQuantity;
        this.productBoxID = productBoxID;
        this.productTypeID = productTypeID;
    }

    public OutgoingDetailsResultPreview(int quantity, String productBoxName, String outgoingID, String productBoxCodeAndName,
                                        String positionBarcode, String serialNumber,
                                        int productBoxID, boolean isSent, Date createdDate) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.outgoingID = outgoingID;
        this.productBoxCodeAndName = productBoxCodeAndName;
        this.positionBarcode = positionBarcode;
        this.serialNumber = serialNumber;
        this.productBoxID = productBoxID;
        this.isSent = isSent;
        this.createdDate = createdDate;
    }

    public OutgoingDetailsResultPreview(int quantity, String productBoxName,
                                        String productBoxCode, String positionBarcode, String serialNumber) {
        this.quantity = quantity;
        this.productBoxName = productBoxName;
        this.productBoxCode = productBoxCode;
        this.positionBarcode = positionBarcode;
        this.serialNumber = serialNumber;
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

    public String getProductBoxCodeAndName() {
        return productBoxCodeAndName;
    }

    public void setProductBoxCodeAndName(String productBoxCodeAndName) {
        this.productBoxCodeAndName = productBoxCodeAndName;
    }

    public String getPositionBarcode() {
        return positionBarcode;
    }

    public void setPositionBarcode(String positionBarcode) {
        this.positionBarcode = positionBarcode;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getColorStatus() {
        return colorStatus;
    }

    public void setColorStatus(int colorStatus) {
        this.colorStatus = colorStatus;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(int expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public String getProductBoxCode() {
        return productBoxCode;
    }

    public void setProductBoxCode(String productBoxCode) {
        this.productBoxCode = productBoxCode;
    }

    public int getProductBoxID() {
        return productBoxID;
    }

    public void setProductBoxID(int productBoxID) {
        this.productBoxID = productBoxID;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getProductTypeID() {
        return productTypeID;
    }

    public void setProductTypeID(int productTypeID) {
        this.productTypeID = productTypeID;
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
    }
}
