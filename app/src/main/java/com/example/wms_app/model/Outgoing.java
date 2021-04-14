package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Outgoing implements Serializable {


    @SerializedName("OutgoingID")
    private String outgoingID;
    @SerializedName("OutgoingCode")
    private String outgoingCode;
    @SerializedName("OutgoingTypeID")
    private int outgoingTypeID;
    @SerializedName("OutgoingStatusCode")
    private String outgoingStatusCode;
    @SerializedName("OutgoingDate")
    private Date outgoingDate;
    @SerializedName("OutgoingPhase")
    private int outgoingPhase;
    @SerializedName("ModifiedDate")
    private Date modifiedDate;
    @SerializedName("TransportNo")
    private String transportNo;
    @SerializedName("PartnerName")
    private String partnerName;
    @SerializedName("PartnerAddress")
    private String partnerAddress;
    @SerializedName("Description")
    private String description;
    @SerializedName("TotalNumOfProd")
    private int totalNumOfProd;
    @SerializedName("IsOffline")
    private boolean isOffline;

    @SerializedName("OutgoingTrucks")
    private List<OutgoingTruck> outgoingTrucks;
    @SerializedName("OutgoingDetails")
    private List<OutgoingDetails> outgoingDetails;


    @SerializedName("PartnerBranchName")
    private String partnerBranchName;
    @SerializedName("PartnerBranchAddress")
    private String partnerBranchAddress;
    @SerializedName("PartnerCity")
    private String partnerCity;

    @SerializedName("DocDate")
    private Date docDate;

    private boolean finished;

    private String partnerWarehouseName;

    //Ovo mi je potrebno u outgoing zbog grupne otpreme
    private List<String> outgoingIDList;


    public Outgoing() {
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean sent) {
        finished = sent;
    }

    public String getOutgoingStatusCode() {
        return outgoingStatusCode;
    }

    public void setOutgoingStatusCode(String outgoingStatusCode) {
        this.outgoingStatusCode = outgoingStatusCode;
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
    }

    public String getOutgoingCode() {
        return outgoingCode;
    }

    public void setOutgoingCode(String outgoingCode) {
        this.outgoingCode = outgoingCode;
    }

    public int getOutgoingTypeID() {
        return outgoingTypeID;
    }

    public void setOutgoingTypeID(int outgoingTypeID) {
        this.outgoingTypeID = outgoingTypeID;
    }

    public Date getOutgoingDate() {
        return outgoingDate;
    }

    public void setOutgoingDate(Date outgoingDate) {
        this.outgoingDate = outgoingDate;
    }

    public int getOutgoingPhase() {
        return outgoingPhase;
    }

    public void setOutgoingPhase(int outgoingPhase) {
        this.outgoingPhase = outgoingPhase;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public List<OutgoingTruck> getOutgoingTrucks() {
        return outgoingTrucks;
    }

    public void setOutgoingTrucks(List<OutgoingTruck> outgoingTrucks) {
        this.outgoingTrucks = outgoingTrucks;
    }

    public List<OutgoingDetails> getOutgoingDetails() {
        return outgoingDetails;
    }

    public void setOutgoingDetails(List<OutgoingDetails> outgoingDetails) {
        this.outgoingDetails = outgoingDetails;
    }

    public int getTotalNumOfProd() {
        return totalNumOfProd;
    }

    public void setTotalNumOfProd(int totalNumOfProd) {
        this.totalNumOfProd = totalNumOfProd;
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

    public String getPartnerCity() {
        return partnerCity;
    }

    public void setPartnerCity(String partnerCity) {
        this.partnerCity = partnerCity;
    }

    public String getPartnerWarehouseName() {
        return partnerWarehouseName;
    }

    public void setPartnerWarehouseName(String partnerWarehouseName) {
        this.partnerWarehouseName = partnerWarehouseName;
    }

    public List<String> getOutgoingIDList() {
        return outgoingIDList;
    }

    public void setOutgoingIDList(List<String> outgoingIDList) {
        this.outgoingIDList = outgoingIDList;
    }
}
