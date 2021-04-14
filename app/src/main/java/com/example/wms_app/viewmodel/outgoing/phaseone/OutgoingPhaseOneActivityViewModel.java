package com.example.wms_app.viewmodel.outgoing.phaseone;

import android.app.Application;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wms_app.R;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.repository.WarehouseRepository;
import com.example.wms_app.repository.outgoing.phaseone.OutgoingPhaseOneActivityRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OutgoingPhaseOneActivityViewModel extends AndroidViewModel {

    private final OutgoingPhaseOneActivityRepository outgoingPhaseOneActivityRepository;
    private MediatorLiveData<List<Outgoing>> outgoingMediatorLiveData;
    private MutableLiveData<EnumOutgoingStyle> enumOutgoingStyleMutableLiveData;
    private final WarehouseRepository warehouseRepository;
    private final Resources resources;
    private int employeeIDDb;

    public OutgoingPhaseOneActivityViewModel(@NonNull Application application) {
        super(application);
        outgoingPhaseOneActivityRepository = new OutgoingPhaseOneActivityRepository(application.getApplicationContext());
        warehouseRepository = new WarehouseRepository(application.getApplicationContext());
        resources = application.getResources();
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();
    }

    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        return outgoingPhaseOneActivityRepository.getResponseMutableLiveData();
    }

    public LiveData<Date> getFilterDateLiveData() {
        return outgoingPhaseOneActivityRepository.getFilterDateMutableLiveData();
    }

    public void setFilterDate(Date dateTo) {
        outgoingPhaseOneActivityRepository.getFilterDateMutableLiveData().setValue(dateTo);
    }

    public LiveData<FilterDataHolder> getFilterDataHolderLiveData() {
        return outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData();
    }

    public void setFilterDataHolder(FilterDataHolder filterDataHolder) {
        outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData().setValue(filterDataHolder);
    }

    public LiveData<List<String>> getCitiesLiveData() {
        return outgoingPhaseOneActivityRepository.getCitiesMutableLiveData();
    }

//    public void syncOutgoing(Date dateTo) {
//        outgoingPhaseOneActivityRepository.syncOutgoings(dateTo);
//    }

//    public void unregisterRealTimeOutgoings() {
//        outgoingPhaseOneActivityRepository.unregisterRealTimeOutgoings();
//    }

    public MutableLiveData<EnumOutgoingStyle> getEnumStyleLiveData() {
        if (enumOutgoingStyleMutableLiveData == null) {
            enumOutgoingStyleMutableLiveData = new MutableLiveData<>();
        }

        return enumOutgoingStyleMutableLiveData;
    }

    public void refreshApiResponseStatus() {
        outgoingPhaseOneActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public LiveData<List<String>> getWarehousesLiveData() {
        return warehouseRepository.getAllWarehousesNames();
    }

    public MediatorLiveData<List<Outgoing>> getOutgoingMediatorLiveData() {
        if (outgoingMediatorLiveData == null) {
            outgoingMediatorLiveData = new MediatorLiveData<>();
            outgoingMediatorLiveData.addSource(outgoingPhaseOneActivityRepository.getOutgoingListMutableLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseOneActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData())));
            outgoingMediatorLiveData.addSource(outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseOneActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData())));
            outgoingMediatorLiveData.addSource(getEnumStyleLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseOneActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseOneActivityRepository.getFilterDataHolderMutableLiveData())));

        }
        return outgoingMediatorLiveData;
    }

    private List<Outgoing> filterOutgoings(MutableLiveData<List<Outgoing>> incomingListMutableLiveData, MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData) {
        List<Outgoing> filteredList = new ArrayList<>();
        FilterDataHolder filterDataHolder = filterDataHolderMutableLiveData.getValue();
//        List<Predicate<FilterDataHolder>> allPredicates = new ArrayList<Predicate<FilterDataHolder>>();
//        allPredicates.add(fdh -> );
        if (incomingListMutableLiveData.getValue() != null) {
            filteredList = incomingListMutableLiveData.getValue()
                    .stream()
                    .filter(x -> x.getOutgoingCode().toLowerCase().contains(filterDataHolder.getCode().toLowerCase())
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

    public void setOutgoingStyle(EnumOutgoingStyle currentOutgoingStyle) {
        getEnumStyleLiveData().setValue(currentOutgoingStyle);
    }

    public void registerRealTimeOutgoings(Date dateTo) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }
        outgoingPhaseOneActivityRepository.registerRealTimeOutgoings(dateTo, employeeID);
    }

    public void unregisterRealTimeOutgoings() {
        outgoingPhaseOneActivityRepository.unregisterRealTimeOutgoings();
    }
}
