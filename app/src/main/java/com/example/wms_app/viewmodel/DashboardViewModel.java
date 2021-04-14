package com.example.wms_app.viewmodel;

import android.app.Application;

import com.example.wms_app.repository.data.EmployeeRepository;
import com.example.wms_app.repository.data.IncomingProductionTypeRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class DashboardViewModel extends AndroidViewModel {

    private final IncomingProductionTypeRepository incomingProductionTypeRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        incomingProductionTypeRepository = new IncomingProductionTypeRepository(application.getApplicationContext());
        employeeRepository = new EmployeeRepository(application.getApplicationContext());
    }

    public LiveData<List<String>> getProductionTypeListLiveData() {
        return incomingProductionTypeRepository.getProductionTypeCodeListLiveData();
    }

    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeRepository.getEmployeeIDLiveData();
    }
}
