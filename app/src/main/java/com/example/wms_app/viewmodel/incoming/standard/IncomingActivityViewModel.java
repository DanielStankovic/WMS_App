package com.example.wms_app.viewmodel.incoming.standard;

import android.app.Application;

import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingType;
import com.example.wms_app.repository.WarehouseRepository;
import com.example.wms_app.repository.incoming.standard.IncomingActivityRepository;
import com.example.wms_app.repository.data.IncomingTypeRepository;
import com.example.wms_app.utilities.ApiResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

public class IncomingActivityViewModel extends AndroidViewModel {

    private IncomingActivityRepository incomingActivityRepository;
    private IncomingTypeRepository incomingTypeRepository;
    private WarehouseRepository warehouseRepository;
    private LiveData<ApiResponse> apiResponseLiveData;
    private LiveData<List<Incoming>> incomingListLiveData;
    private LiveData<Date> filterDateLiveData;
    private LiveData<FilterDataHolder> filterDataHolderLiveData;
    private LiveData<List<IncomingType>> incomingTypeListLiveData;
    private MediatorLiveData<List<Incoming>> incomingMediatorLiveData;
    private LiveData<List<String>> citiesLiveData;
    private LiveData<List<String>> warehousesLiveData;
    private MutableLiveData<EnumIncomingStyle> enumIncomingStyleMutableLiveData;


    public IncomingActivityViewModel(@NonNull Application application) {
        super(application);
        incomingActivityRepository = new IncomingActivityRepository(application.getApplicationContext());
        incomingTypeRepository = new IncomingTypeRepository(application.getApplicationContext());
        warehouseRepository = new WarehouseRepository(application.getApplicationContext());
    }

    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        apiResponseLiveData = incomingActivityRepository.getResponseMutableLiveData();
        return apiResponseLiveData;
    }

    public LiveData<List<Incoming>> getIncomingListLiveData() {
        incomingListLiveData = incomingActivityRepository.getIncomingListMutableLiveData();
        return incomingListLiveData;
    }

    public LiveData<Date> getFilterDateLiveData() {
        filterDateLiveData = incomingActivityRepository.getFilterDateMutableLiveData();
        return filterDateLiveData;
    }

    public LiveData<FilterDataHolder> getFilterDataHolderLiveData() {
        filterDataHolderLiveData = incomingActivityRepository.getFilterDataHolderMutableLiveData();
        return filterDataHolderLiveData;
    }
    public LiveData<List<IncomingType>> getIncomingTypeListLiveData() {
        incomingTypeListLiveData = incomingTypeRepository.getIncomingTypeLiveData();
        return incomingTypeListLiveData;
    }

    public LiveData<List<String>> getCitiesLiveData() {
        citiesLiveData = incomingActivityRepository.getCitiesMutableLiveData();
        return citiesLiveData;
    }

    public LiveData<List<String>> getWarehousesLiveData() {
        warehousesLiveData = warehouseRepository.getAllWarehousesNames();
        return warehousesLiveData;
    }

    private MutableLiveData<EnumIncomingStyle> getEnumIncomingStyleMutableLiveData() {
        if (enumIncomingStyleMutableLiveData == null)
            enumIncomingStyleMutableLiveData = new MutableLiveData<>();
        return enumIncomingStyleMutableLiveData;
    }

    public MediatorLiveData<List<Incoming>> getIncomingMediatorLiveData() {
        if (incomingMediatorLiveData == null) {
            incomingMediatorLiveData = new MediatorLiveData<>();
            incomingMediatorLiveData.addSource(incomingActivityRepository.getIncomingListMutableLiveData(),
                    value -> incomingMediatorLiveData.setValue(filterIncomings(incomingActivityRepository.getIncomingListMutableLiveData(), incomingActivityRepository.getFilterDataHolderMutableLiveData())));
            incomingMediatorLiveData.addSource(incomingActivityRepository.getFilterDataHolderMutableLiveData(),
                    value -> incomingMediatorLiveData.setValue(filterIncomings(incomingActivityRepository.getIncomingListMutableLiveData(), incomingActivityRepository.getFilterDataHolderMutableLiveData())));
            incomingMediatorLiveData.addSource(getEnumIncomingStyleMutableLiveData(),
                    value -> incomingMediatorLiveData.setValue(filterIncomings(incomingActivityRepository.getIncomingListMutableLiveData(), incomingActivityRepository.getFilterDataHolderMutableLiveData())));

        }
        return incomingMediatorLiveData;
    }

    private List<Incoming> filterIncomings(MutableLiveData<List<Incoming>> incomingListMutableLiveData, MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData) {
        List<Incoming> filteredList = new ArrayList<>();
        FilterDataHolder filterDataHolder = filterDataHolderMutableLiveData.getValue();
//        List<Predicate<FilterDataHolder>> allPredicates = new ArrayList<Predicate<FilterDataHolder>>();
//        allPredicates.add(fdh -> );
        if(incomingListMutableLiveData.getValue() != null) {
            filteredList = incomingListMutableLiveData.getValue()
                    .stream()
                    .filter(x -> x.getIncomingCode().toLowerCase().contains(filterDataHolder.getCode().toLowerCase())
                                    && x.getPartnerName().toLowerCase().contains(filterDataHolder.getPartnerName().toLowerCase())
                                    && x.getPartnerCity().toLowerCase().contains(filterDataHolder.getPartnerCity().toLowerCase())
                                    && x.getTransportNo().toLowerCase().contains(filterDataHolder.getTransport().toLowerCase())
                                    && x.getPartnerWarehouseName().toLowerCase().contains(filterDataHolder.getWarehouse().toLowerCase())
//                    && (x.getIncomingTypeCode() == filterDataHolder.getTypeID() || filterDataHolder.getTypeID() == 0))
                    )
                    .collect(Collectors.toList());

        }

        return filteredList;

    }

    public void registerRealTimeIncomings(Date dateTo){
        incomingActivityRepository.registerRealTimeIncomings(dateTo);
    }
    public void unregisterRealTimeIncomings(){
        incomingActivityRepository.unregisterRealTimeIncomings();
    }

    public void setFilterDate(Date dateTo) {
        incomingActivityRepository.getFilterDateMutableLiveData().setValue(dateTo);
    }

    public void setFilterDataHolder(FilterDataHolder filterDataHolder) {
        incomingActivityRepository.getFilterDataHolderMutableLiveData().setValue(filterDataHolder);
    }

    public void sendIncomingToServer() {
        incomingActivityRepository.sendIncomingToServer();
    }

    public void refreshApiResponseStatus() {
        incomingActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public void setOutgoingStyle(EnumIncomingStyle currentIncomingStyle) {
        getEnumIncomingStyleMutableLiveData().setValue(currentIncomingStyle);
    }
}
