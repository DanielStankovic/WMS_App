package com.example.wms_app.viewmodel;

import android.app.Application;
import android.content.res.Resources;

import com.example.wms_app.repository.MainActivityRepository;
import com.example.wms_app.repository.data.EmployeeRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.SyncResponse;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainActivityViewModel extends AndroidViewModel {
    private final MainActivityRepository mainActivityRepository;
    private final EmployeeRepository employeeRepository;
    private Resources resources;
    private LiveData<ApiResponse> apiResponseLiveData;
    private LiveData<SyncResponse> syncResponseLiveData;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
//        Context context = application.getApplicationContext();
//        mainActivityRepository = new MainActivityRepository(context);
        resources = application.getResources();
        mainActivityRepository = new MainActivityRepository(application.getApplicationContext());
        employeeRepository = new EmployeeRepository(application.getApplicationContext());
    }


    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        apiResponseLiveData = mainActivityRepository.getResponseMutableLiveData();
        return apiResponseLiveData;
    }

    /**
     * Dobija se LiveData SyncResponse. Njegov status moze biti SYNC_START, SYNC_INCREASE,
     * SYNC_ERROR i SYNC_FINISHED. Koristi se kada se radi sinhronizacija na loginu
     *
     * @return syncResponseLiveData
     */
    public LiveData<SyncResponse> getSyncResponseLiveData() {
        syncResponseLiveData = mainActivityRepository.getSyncResponseMutableLiveData();
        return syncResponseLiveData;
    }

    public void loginUser(String userName, String password, String deviceSerialNumber) {
        mainActivityRepository.loginUser(userName, password, deviceSerialNumber);
    }

    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeRepository.getEmployeeIDLiveData();
    }
}
