package com.example.wms_app.dao;

import com.example.wms_app.model.IncomingType;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface IncomingTypeDao {
    @Insert
    void insert(IncomingType incomingType);

    @Delete
    void delete(IncomingType incomingType);

    @Query("SELECT ModifiedDate FROM IncomingType  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT 0 as IncomingTypeID, 'SVI' as IncomingTypeName, '0' as IncomingTypeCode, '' as Description, 1 as IsActive, date('now') as ModifiedDate FROM IncomingType " +
            "UNION " +
            "SELECT IncomingTypeID, IncomingTypeName, IncomingTypeCode, Description, IsActive, ModifiedDate FROM IncomingType ORDER BY IncomingTypeID")
    LiveData<List<IncomingType>> getIncomingTypeList();
}
