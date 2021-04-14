package com.example.wms_app.dao;

import com.example.wms_app.model.Partner;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PartnerDao {

    @Insert
    void insert(Partner partner);

    @Delete
    void delete(Partner partner);

    @Query("SELECT ModifiedDate FROM Partner  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM Partner ORDER BY PartnerName")
    LiveData<List<Partner>> getPartnerList();

}
