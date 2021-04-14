package com.example.wms_app.repository.data;

import android.app.Application;

import com.example.wms_app.dao.PartnerDao;
import com.example.wms_app.data.RoomDb;

public class PartnerRepository {
    private PartnerDao mPartnerDao;

    public PartnerRepository(Application application) {
        RoomDb db = RoomDb.getDatabase(application);
        mPartnerDao = db.partnerDao();
    }
}
