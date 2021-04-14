package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class IncomingForServerWrapper {

    @SerializedName("IncomingID")
    private String incomingID;
    @SerializedName("IncomingStatusCode")
    private String incomingStatusCode;
    @SerializedName("IncomingDetailsResultList")
    private List<IncomingDetailsResult> incomingDetailsResultList;
    @SerializedName("IncomingTruckResultList")
    private List<IncomingTruckResult> incomingTruckResultList;

    public IncomingForServerWrapper(String incomingID, String incomingStatusCode,
                                    List<IncomingDetailsResult> incomingDetailsResultList,
                                    List<IncomingTruckResult> incomingTruckResultList) {
        this.incomingID = incomingID;
        this.incomingStatusCode = incomingStatusCode;
        this.incomingDetailsResultList = incomingDetailsResultList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(IncomingDetailsResult::getIncomingId)
                                .thenComparing(IncomingDetailsResult::getProductBoxID)
                                .thenComparing(IncomingDetailsResult::getQuantity)
                                .thenComparing(IncomingDetailsResult::getSerialNo)
                                .thenComparing(IncomingDetailsResult::getwPositionID)
                                .thenComparing(IncomingDetailsResult::getwSubPositionID)
                                .thenComparing(IncomingDetailsResult::getEmployeeID)
                                .thenComparing(IncomingDetailsResult::getCreateDate)
                                .thenComparing(IncomingDetailsResult::isScanned)
                                .thenComparing(IncomingDetailsResult::isReserved)

                        )), ArrayList::new));
        this.incomingTruckResultList = incomingTruckResultList;
    }

    public String getIncomingID() {
        return incomingID;
    }

    public void setIncomingID(String incomingID) {
        this.incomingID = incomingID;
    }

    public String getIncomingStatusCode() {
        return incomingStatusCode;
    }

    public void setIncomingStatusCode(String incomingStatusID) {
        this.incomingStatusCode = incomingStatusID;
    }

    public List<IncomingDetailsResult> getIncomingDetailsResultList() {
        return incomingDetailsResultList;
    }

    public void setIncomingDetailsResultList(List<IncomingDetailsResult> incomingDetailsResultList) {
        this.incomingDetailsResultList = incomingDetailsResultList;
    }

    public List<IncomingTruckResult> getIncomingTruckResultList() {
        return incomingTruckResultList;
    }

    public void setIncomingTruckResultList(List<IncomingTruckResult> incomingTruckResultList) {
        this.incomingTruckResultList = incomingTruckResultList;
    }
}
