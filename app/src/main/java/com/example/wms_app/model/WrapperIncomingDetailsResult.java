package com.example.wms_app.model;

public class WrapperIncomingDetailsResult {

    private String incomingID;
    private IncomingDetailsResult incomingDetailsResult;

    public WrapperIncomingDetailsResult(String incomingID, IncomingDetailsResult incomingDetailsResult) {
        this.incomingID = incomingID;
        this.incomingDetailsResult = incomingDetailsResult;
    }

    public String getIncomingID() {
        return incomingID;
    }

    public void setIncomingID(String incomingID) {
        this.incomingID = incomingID;
    }

    public IncomingDetailsResult getIncomingDetailsResult() {
        return incomingDetailsResult;
    }

    public void setIncomingDetailsResult(IncomingDetailsResult incomingDetailsResult) {
        this.incomingDetailsResult = incomingDetailsResult;
    }
}
