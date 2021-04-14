package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.TruckDao;
import com.example.wms_app.data.RoomDb;

import java.util.List;

import androidx.lifecycle.MutableLiveData;

public class TruckRepository {

    private MutableLiveData<List<String>> licencePlateListMutLiveData;

    public TruckRepository(Context context) {
        RoomDb db = RoomDb.getDatabase(context);
        TruckDao truckDao = db.truckDao();
        licencePlateListMutLiveData = new MutableLiveData<>();
        licencePlateListMutLiveData.setValue(truckDao.getLicencePlate());
    }

    public MutableLiveData<List<String>> getLicencePlateListMutLiveData() {
        return licencePlateListMutLiveData;
    }
}
