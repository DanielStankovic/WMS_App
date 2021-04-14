package com.example.wms_app.model;

public class FilterDataHolder {

    private int typeID;
    private String code;
    private String partnerName;
    private String partnerCity;
    private String transport;
    private String warehouse;

    public FilterDataHolder(int typeID, String code, String partnerName,
                            String partnerCity, String transport, String warehouse) {
        this.typeID = typeID;
        this.code = code;
        this.partnerName = partnerName;
        this.partnerCity = partnerCity;
        this.transport = transport;
        this.warehouse = warehouse;
    }

    public FilterDataHolder(String code, String partnerName, String partnerCity,
                            String transport, String warehouse) {
        this.code = code;
        this.partnerName = partnerName;
        this.partnerCity = partnerCity;
        this.transport = transport;
        this.warehouse = warehouse;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerCity() {
        return partnerCity;
    }

    public void setPartnerCity(String partnerCity) {
        this.partnerCity = partnerCity;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public static FilterDataHolder getFilterDataPlaceholder() {
        return new FilterDataHolder(0, "", "", "", "", "");
    }
}
