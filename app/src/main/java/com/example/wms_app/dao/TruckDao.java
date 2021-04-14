package com.example.wms_app.dao;

import com.example.wms_app.model.Truck;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TruckDao {
    @Insert
    void insert(Truck truck);

    @Delete
    void delete(Truck truck);

    @Query("SELECT ModifiedDate FROM Truck  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM Truck ORDER BY TruckName")
    LiveData<List<Truck>> getTruckList();

    @Query("SELECT DISTINCT LicencePlate FROM Truck ORDER BY LicencePlate")
    List<String> getLicencePlate();
}
