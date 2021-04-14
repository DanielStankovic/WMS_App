package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Incoming implements Serializable {
    @SerializedName("IncomingID")
    private String incomingID;
    @SerializedName("IncomingCode")
    private String incomingCode;
    @SerializedName("IncomingTypeCode")
    private String incomingTypeCode;
    @SerializedName("IncomingProductionTypeCode")
    private String incomingProductionTypeCode;
    @SerializedName("IncomingStatusCode")
    private String incomingStatusCode;
    @SerializedName("IncomingDate")
    private Date incomingDate;
    @SerializedName("ModifiedDate")
    private Date modifiedDate;
    @SerializedName("TransportNo")
    private String transportNo;
    @SerializedName("PartnerName")
    private String partnerName;
    @SerializedName("PartnerAddress")
    private String partnerAddress;
    @SerializedName("PartnerCity")
    private String partnerCity;
    @SerializedName("PartnerBranchName")
    private String partnerBranchName;
    @SerializedName("PartnerBranchAddress")
    private String partnerBranchAddress;
    @SerializedName("Description")
    private String description;
    @SerializedName("TotalNumOfProd")
    private int totalNumOfProd;
    @SerializedName("IsOffline")
    private boolean isOffline;

    @SerializedName("DocDate")
    private Date docDate;


    @SerializedName("IncomingTrucks")
    private List<IncomingTruck> incomingTrucks;
    @SerializedName("IncomingDetails")
    private List<IncomingDetails> incomingDetails;
    @SerializedName("IncomingSerialNo")
    private List<IncomingSerialNo> incomingSerialNo;

    private boolean finished;

    private String partnerWarehouseName;

    //Ovo mi je potrebno u incoming zbog grupnog prijema
    private List<String> incomingIDList;
    private List<Integer> uniqueProductBoxIDList;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean sent) {
        finished = sent;
    }

    public Incoming() {
    }

    public String getIncomingID() {
        return incomingID;
    }

    public void setIncomingID(String incomingID) {
        this.incomingID = incomingID;
    }

    public String getIncomingCode() {
        return incomingCode;
    }

    public void setIncomingCode(String incomingCode) {
        this.incomingCode = incomingCode;
    }

    public String getIncomingTypeCode() {
        return incomingTypeCode;
    }

    public void setIncomingTypeCode(String incomingTypeCode) {
        this.incomingTypeCode = incomingTypeCode;
    }

    public String getIncomingProductionTypeCode() {
        return incomingProductionTypeCode;
    }

    public void setIncomingProductionTypeCode(String incomingProductionTypeCode) {
        this.incomingProductionTypeCode = incomingProductionTypeCode;
    }

    public String getIncomingStatusCode() {
        return incomingStatusCode;
    }

    public void setIncomingStatusCode(String incomingStatusCode) {
        this.incomingStatusCode = incomingStatusCode;
    }

    public Date getIncomingDate() {
        return incomingDate;
    }

    public void setIncomingDate(Date incomingDate) {
        this.incomingDate = incomingDate;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerAddress() {
        return partnerAddress;
    }

    public void setPartnerAddress(String partnerAddress) {
        this.partnerAddress = partnerAddress;
    }

    public String getPartnerCity() {
        return partnerCity;
    }

    public void setPartnerCity(String partnerCity) {
        this.partnerCity = partnerCity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalNumOfProd() {
        return totalNumOfProd;
    }

    public void setTotalNumOfProd(int totalNumOfProd) {
        this.totalNumOfProd = totalNumOfProd;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getTransportNo() {
        return transportNo;
    }

    public void setTransportNo(String transportNo) {
        this.transportNo = transportNo;
    }

    public List<IncomingTruck> getIncomingTrucks() {
        return incomingTrucks;
    }

    public void setIncomingTrucks(List<IncomingTruck> incomingTrucks) {
        this.incomingTrucks = incomingTrucks;
    }

    public List<IncomingDetails> getIncomingDetails() {
        return incomingDetails;
    }

    public void setIncomingDetails(List<IncomingDetails> incomingDetails) {
        this.incomingDetails = incomingDetails;
    }

    public List<IncomingSerialNo> getIncomingSerialNo() {
        return incomingSerialNo;
    }

    public void setIncomingSerialNo(List<IncomingSerialNo> incomingSerialNo) {
        this.incomingSerialNo = incomingSerialNo;
    }

    public String getPartnerBranchName() {
        return partnerBranchName;
    }

    public void setPartnerBranchName(String partnerBranchName) {
        this.partnerBranchName = partnerBranchName;
    }

    public String getPartnerBranchAddress() {
        return partnerBranchAddress;
    }

    public void setPartnerBranchAddress(String partnerBranchAddress) {
        this.partnerBranchAddress = partnerBranchAddress;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }

    public String getPartnerWarehouseName() {
        return partnerWarehouseName;
    }

    public void setPartnerWarehouseName(String partnerWarehouseName) {
        this.partnerWarehouseName = partnerWarehouseName;
    }

    public List<String> getIncomingIDList() {
        return incomingIDList;
    }

    public void setIncomingIDList(List<String> incomingIDList) {
        this.incomingIDList = incomingIDList;
    }

    public List<Integer> getUniqueProductBoxIDList() {
        return uniqueProductBoxIDList;
    }

    public void setUniqueProductBoxIDList(List<Integer> uniqueProductBoxIDList) {
        this.uniqueProductBoxIDList = uniqueProductBoxIDList;
    }
}
