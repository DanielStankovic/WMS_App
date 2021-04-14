package com.example.wms_app.model;

public class WrapperOutgoingDetailsResult {
    private String outgoingID;
    private OutgoingDetailsResult outgoingDetailsResult;

    public WrapperOutgoingDetailsResult(String outgoingID, OutgoingDetailsResult outgoingDetailsResult) {
        this.outgoingID = outgoingID;
        this.outgoingDetailsResult = outgoingDetailsResult;
    }

    public String getOutgoingID() {
        return outgoingID;
    }

    public void setOutgoingID(String outgoingID) {
        this.outgoingID = outgoingID;
    }

    public OutgoingDetailsResult getOutgoingDetailsResult() {
        return outgoingDetailsResult;
    }

    public void setOutgoingDetailsResult(OutgoingDetailsResult outgoingDetailsResult) {
        this.outgoingDetailsResult = outgoingDetailsResult;
    }
}
