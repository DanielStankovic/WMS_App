package com.example.wms_app.model;

import java.io.Serializable;
import java.util.List;

public class OutgoingGrouped implements Serializable {

    private String period;
    private int totalNumOfProds;
    private int totalNumOfOutgoings;
    private String productNames;
    private String outgoingCodes;
    private List<OutgoingDetails> outgoingDetailsList;
    private List<String> outgoingIDList;

    public OutgoingGrouped() {
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getTotalNumOfProds() {
        return totalNumOfProds;
    }

    public void setTotalNumOfProds(int totalNumOfProds) {
        this.totalNumOfProds = totalNumOfProds;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public List<OutgoingDetails> getOutgoingDetailsList() {
        return outgoingDetailsList;
    }

    public void setOutgoingDetailsList(List<OutgoingDetails> outgoingDetailsList) {
        this.outgoingDetailsList = outgoingDetailsList;
    }

    public List<String> getOutgoingIDList() {
        return outgoingIDList;
    }

    public void setOutgoingIDList(List<String> outgoingIDList) {
        this.outgoingIDList = outgoingIDList;
    }

    public int getTotalNumOfOutgoings() {
        return totalNumOfOutgoings;
    }

    public void setTotalNumOfOutgoings(int totalNumOfOutgoings) {
        this.totalNumOfOutgoings = totalNumOfOutgoings;
    }

    public String getOutgoingCodes() {
        return outgoingCodes;
    }

    public void setOutgoingCodes(String outgoingCodes) {
        this.outgoingCodes = outgoingCodes;
    }
}
