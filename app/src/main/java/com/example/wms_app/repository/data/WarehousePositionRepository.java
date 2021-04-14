package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.WarehousePositionDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.WarehousePosition;

import java.util.List;

import androidx.lifecycle.MutableLiveData;

public class WarehousePositionRepository {

    private MutableLiveData<List<WarehousePosition>> warehousePosMutableLiveData;
    private WarehousePositionDao warehousePositionDao;

    public WarehousePositionRepository(Context context) {
        RoomDb db = RoomDb.getDatabase(context);
        warehousePositionDao = db.warehousePositionDao();
        warehousePosMutableLiveData = new MutableLiveData<>();
        warehousePosMutableLiveData.setValue(warehousePositionDao.getWarehousePositionList());
    }

    public MutableLiveData<List<WarehousePosition>> getWarehousePosMutableLiveData() {
        return warehousePosMutableLiveData;
    }

    public WarehousePosition getWarehousePositionByID(int positionID) {
        return warehousePositionDao.getWarehousePositionByID(positionID);
    }

    public String getWarehouseBarcodeByID(int positionID) {
        return warehousePositionDao.getWarehousePositionBarCodeByID(positionID);
    }

    public WarehousePosition getWarehousePositionByBarcode(String scannedCode) {
        return warehousePositionDao.getWarehousePositionByBarcode(scannedCode);
    }

    public String getPositionBarcodeByWarehouseName(String partnerWarehouseName) {
        String barcode = warehousePositionDao.getPositionBarcodeByWarehouseName(partnerWarehouseName);
        return barcode == null ? "" : barcode;
    }
}
