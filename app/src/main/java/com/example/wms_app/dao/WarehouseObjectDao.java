package com.example.wms_app.dao;

import com.example.wms_app.model.WarehouseObject;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WarehouseObjectDao {
    @Insert
    void insert(WarehouseObject warehouseObject);

    @Delete
    void delete(WarehouseObject warehouseObject);

    @Query("SELECT ModifiedDate FROM WarehouseObject  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM WarehouseObject ORDER BY WObjectName")
    LiveData<List<WarehouseObject>> getWarehouseObjectList();
}
