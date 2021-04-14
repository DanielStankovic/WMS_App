package com.example.wms_app.dao;

import com.example.wms_app.model.WarehousePosition;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WarehousePositionDao {

    @Insert
    void insert(WarehousePosition warehousePosition);

    @Delete
    void delete(WarehousePosition warehousePosition);

    @Query("SELECT ModifiedDate FROM WarehousePosition  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM WarehousePosition ORDER BY WPositionName")
    List<WarehousePosition> getWarehousePositionList();

    @Query("SELECT * FROM WarehousePosition WHERE ForPreloading = 0 ORDER BY WPositionName")
    LiveData<List<WarehousePosition>> getWarehousePositionForProducts();

    @Query("SELECT * FROM WarehousePosition WHERE ForPreloading = 1 ORDER BY WPositionName")
    LiveData<List<WarehousePosition>> getPreloadingPositionList();

    @Query("SELECT * FROM WarehousePosition WHERE Barcode = :scannedCode LIMIT 1")
    WarehousePosition getWarehousePositionByBarcode(String scannedCode);

    @Query("SELECT Barcode FROM WarehousePosition WHERE WPositionID = :wPositionID")
    String getWarehousePositionBarCodeByID(int wPositionID);

    @Query("DELETE FROM WarehousePosition")
    void deleteAllWarehousePositions();

    @Query("SELECT * FROM WarehousePosition WHERE WPositionID = :positionID LIMIT 1")
    WarehousePosition getWarehousePositionByID(int positionID);

    @Query("SELECT wp.Barcode FROM WarehousePosition wp " +
            "INNER JOIN Warehouse wh ON wh.WarehouseCode = wp.WarehouseCode " +
            "WHERE wh.WarehouseName = :partnerWarehouseName AND wp.ForPreloading = 1 " +
            "ORDER BY WPositionName " +
            "LIMIT 1")
    String getPositionBarcodeByWarehouseName(String partnerWarehouseName);
}
