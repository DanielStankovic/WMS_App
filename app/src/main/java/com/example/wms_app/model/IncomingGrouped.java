package com.example.wms_app.model;

import java.io.Serializable;
import java.util.List;

public class IncomingGrouped implements Serializable {

    private String period;
    private int totalNumOfProds;
    private int totalNumOfIncomings;
    private String productNames;
    private String incomingCodes;
    private List<IncomingDetails> incomingDetailsList;
    private List<String> incomingIDList;
    private List<Integer> uniqueProductBoxIDList;

    public IncomingGrouped() {
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

    public List<IncomingDetails> getIncomingDetailsList() {
        return incomingDetailsList;
    }

    public void setIncomingDetailsList(List<IncomingDetails> incomingDetailsList) {
        this.incomingDetailsList = incomingDetailsList;
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

    public int getTotalNumOfIncomings() {
        return totalNumOfIncomings;
    }

    public void setTotalNumOfIncomings(int totalNumOfIncomings) {
        this.totalNumOfIncomings = totalNumOfIncomings;
    }

    public String getIncomingCodes() {
        return incomingCodes;
    }

    public void setIncomingCodes(String incomingCodes) {
        this.incomingCodes = incomingCodes;
    }
}
