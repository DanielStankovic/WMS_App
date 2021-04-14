package com.example.wms_app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.wms_app.dao.WarehouseDao;
import com.example.wms_app.data.RoomDb;

import java.util.List;

public class WarehouseRepository {
    private WarehouseDao warehouseDao;

    public WarehouseRepository(Context context){
        RoomDb db = RoomDb.getDatabase(context);
        warehouseDao = db.warehouseDao();
    }

    public LiveData<List<String>> getAllWarehousesNames(){
        return warehouseDao.getAllWarehousesName();
    }

}
