package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.IncomingProductionTypeDao;
import com.example.wms_app.data.RoomDb;

import java.util.List;

import androidx.lifecycle.MutableLiveData;

public class IncomingProductionTypeRepository {

    private MutableLiveData<List<String>> productionTypeCodeListLiveData;

    public IncomingProductionTypeRepository(Context context) {
        RoomDb roomDb = RoomDb.getDatabase(context);
        IncomingProductionTypeDao incomingProductionTypeDao = roomDb.incomingProductionTypeDao();
        productionTypeCodeListLiveData = new MutableLiveData<>();
        productionTypeCodeListLiveData.setValue(incomingProductionTypeDao.getProductionTypeCodeList());
    }

    public MutableLiveData<List<String>> getProductionTypeCodeListLiveData() {
        return productionTypeCodeListLiveData;
    }
}
