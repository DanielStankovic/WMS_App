package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OutgoingForServerWrapper {

    @SerializedName("OutgoingID")
    private String outgoingID;
    @SerializedName("OutgoingStatusCode")
    private String outgoingStatusCode;
    @SerializedName("OutgoingDetailsResultList")
    private List<OutgoingDetailsResult> outgoingDetailsResultList;
    @SerializedName("OutgoingTruckResultList")
    private List<OutgoingTruckResult> outgoingTruckResultList;

    public OutgoingForServerWrapper(String outgoingID, String outgoingStatusCode,
                                    List<OutgoingDetailsResult> outgoingDetailsResultList,
                                    List<OutgoingTruckResult> outgoingTruckResultList) {
        this.outgoingID = outgoingID;
        this.outgoingStatusCode = outgoingStatusCode;
        this.outgoingDetailsResultList = outgoingDetailsResultList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(OutgoingDetailsResult::getOutgoingID)
                                .thenComparing(OutgoingDetailsResult::getProductBoxID)
                                .thenComparing(OutgoingDetailsResult::getQuantity)
                                .thenComparing(OutgoingDetailsResult::getSerialNo)
                                .thenComparing(OutgoingDetailsResult::getwPositionBarcode)
                                .thenComparing(OutgoingDetailsResult::getEmployeeID)
                                .thenComparing(OutgoingDetailsResult::getCreateDate)
                                .thenComparing(OutgoingDetailsResult::isScanned)
                                .thenComparing(OutgoingDetailsResult::getOdrFirebaseID)
                        )), ArrayList::new));
        this.outgoingTruckResultList = outgoingTruckResultList;
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
    }

    public String getOutgoingStatusCode() {
        return outgoingStatusCode;
    }

    public void setOutgoingStatusCode(String outgoingStatusCode) {
        this.outgoingStatusCode = outgoingStatusCode;
    }

    public List<OutgoingDetailsResult> getOutgoingDetailsResultList() {
        return outgoingDetailsResultList;
    }

    public void setOutgoingDetailsResultList(List<OutgoingDetailsResult> outgoingDetailsResultList) {
        this.outgoingDetailsResultList = outgoingDetailsResultList;
    }

    public List<OutgoingTruckResult> getOutgoingTruckResultList() {
        return outgoingTruckResultList;
    }

    public void setOutgoingTruckResultList(List<OutgoingTruckResult> outgoingTruckResultList) {
        this.outgoingTruckResultList = outgoingTruckResultList;
    }
}
