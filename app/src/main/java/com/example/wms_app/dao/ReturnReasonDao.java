package com.example.wms_app.dao;

import com.example.wms_app.model.ReturnReason;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ReturnReasonDao {

    @Insert
    void insert(ReturnReason returnReason);

    @Delete
    void delete(ReturnReason returnReason);

    @Query("SELECT ModifiedDate FROM ReturnReason  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM ReturnReason ORDER BY ReturnReasonCode")
    LiveData<List<ReturnReason>> getReturnReasonList();
}
