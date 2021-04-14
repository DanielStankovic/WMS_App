package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.IncomingTypeDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.IncomingType;

import java.util.List;

import androidx.lifecycle.LiveData;

public class IncomingTypeRepository {
    private LiveData<List<IncomingType>> incomingTypeLiveData;

    public IncomingTypeRepository(Context context) {

        RoomDb roomDb = RoomDb.getDatabase(context);
        IncomingTypeDao incomingTypeDao = roomDb.incomingTypeDao();
        incomingTypeLiveData = incomingTypeDao.getIncomingTypeList();
    }

    public LiveData<List<IncomingType>> getIncomingTypeLiveData() {
        return incomingTypeLiveData;
    }
}
