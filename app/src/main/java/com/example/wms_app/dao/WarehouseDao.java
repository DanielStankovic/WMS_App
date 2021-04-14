package com.example.wms_app.dao;

import com.example.wms_app.model.Warehouse;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WarehouseDao {

    @Insert
    void insert(Warehouse warehouse);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Warehouse> warehouseList);

    @Delete
    void delete(Warehouse warehouse);

    @Query("DELETE FROM Warehouse WHERE IsActive = 0")
    void deleteInactive();

    @Query("SELECT ModifiedDate FROM Warehouse  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT -1 as WarehouseID, '--Odaberite magacin--' as WarehouseName, '' as WarehouseCode, 1 as IsActive FROM Warehouse " +
            "UNION " +
            "SELECT WarehouseID, WarehouseName, WarehouseCode, IsActive FROM Warehouse " +
            "ORDER BY WarehouseName")
    LiveData<List<Warehouse>> getWarehouseList();

    @Query("SELECT WarehouseName FROM Warehouse WHERE IsActive = 1")
    LiveData<List<String>> getAllWarehousesName();
}
