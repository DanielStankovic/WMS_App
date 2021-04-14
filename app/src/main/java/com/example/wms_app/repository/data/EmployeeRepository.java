package com.example.wms_app.repository.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wms_app.dao.EmployeeDao;
import com.example.wms_app.data.RoomDb;

public class EmployeeRepository {

    private final MutableLiveData<Integer> employeeIDMutableLiveData;

    public EmployeeRepository(Context context) {
        RoomDb roomDb = RoomDb.getDatabase(context);
        EmployeeDao employeeDao = roomDb.employeeDao();
        employeeIDMutableLiveData = new MutableLiveData<>();
        employeeIDMutableLiveData.setValue(employeeDao.getEmployeeID());
    }

    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeIDMutableLiveData;
    }
}
