package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InventoryWrapper implements Serializable {


    @SerializedName("Inventory")
    private Inventory inventory;
    @SerializedName("InventoryDetailsResult")
    private List<InventoryDetailsResult> inventoryDetailsResults;


    public InventoryWrapper(){
        inventoryDetailsResults = new ArrayList<InventoryDetailsResult>();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public List<InventoryDetailsResult> getInventoryDetailsResults() {
        return inventoryDetailsResults;
    }

    public void setInventoryDetailsResults(List<InventoryDetailsResult> inventoryDetailsResults) {
        this.inventoryDetailsResults = inventoryDetailsResults;
    }
}
