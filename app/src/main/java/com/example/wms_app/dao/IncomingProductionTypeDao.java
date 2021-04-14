package com.example.wms_app.dao;

import com.example.wms_app.model.IncomingProductionType;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface IncomingProductionTypeDao {

    @Insert
    void insert(IncomingProductionType incomingProductionType);

    @Delete
    void delete(IncomingProductionType incomingProductionType);

    @Query("SELECT ModifiedDate FROM IncomingProductionType  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT DISTINCT IncomingProductionTypeCode FROM IncomingProductionType ORDER BY IncomingProductionTypeCode")
    List<String> getProductionTypeCodeList();
}
