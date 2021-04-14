package com.example.wms_app.viewmodel.outgoing.phasetwo;

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
import com.example.wms_app.repository.outgoing.phasetwo.OutgoingPhaseTwoActivityRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OutgoingPhaseTwoActivityViewModel extends AndroidViewModel {

    private OutgoingPhaseTwoActivityRepository outgoingPhaseTwoActivityRepository;
    private MediatorLiveData<List<Outgoing>> outgoingMediatorLiveData;
    private MutableLiveData<EnumOutgoingStyle> enumOutgoingStyleMutableLiveData;
    private LiveData<List<String>> warehousesLiveData;
    private final WarehouseRepository warehouseRepository;
    private final Resources resources;
    private final int employeeIDDb;

    public OutgoingPhaseTwoActivityViewModel(@NonNull Application application) {
        super(application);
        outgoingPhaseTwoActivityRepository = new OutgoingPhaseTwoActivityRepository(application.getApplicationContext());
        warehouseRepository = new WarehouseRepository(application.getApplicationContext());
        resources = application.getResources();
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();

    }

    public LiveData<ApiResponse> getApiResponseLiveData() {
        return outgoingPhaseTwoActivityRepository.getResponseMutableLiveData();
    }

    public void refreshApiResponseStatus() {
        outgoingPhaseTwoActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public MediatorLiveData<List<Outgoing>> getOutgoingMediatorLiveData() {
        if (outgoingMediatorLiveData == null) {
            outgoingMediatorLiveData = new MediatorLiveData<>();
            outgoingMediatorLiveData.addSource(outgoingPhaseTwoActivityRepository.getOutgoingListMutableLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseTwoActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData())));
            outgoingMediatorLiveData.addSource(outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseTwoActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData())));
            outgoingMediatorLiveData.addSource(getEnumStyleLiveData(),
                    value -> outgoingMediatorLiveData.setValue(filterOutgoings(outgoingPhaseTwoActivityRepository.getOutgoingListMutableLiveData(), outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData())));

        }
        return outgoingMediatorLiveData;
    }

    public LiveData<Date> getFilterDateLiveData() {
        return outgoingPhaseTwoActivityRepository.getFilterDateMutableLiveData();
    }

    public LiveData<FilterDataHolder> getFilterDataHolderLiveData() {
        return outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData();
    }

    public LiveData<List<String>> getCitiesLiveData() {
        return outgoingPhaseTwoActivityRepository.getCitiesMutableLiveData();
    }
    public LiveData<List<String>> getWarehousesLiveData() {
        warehousesLiveData = warehouseRepository.getAllWarehousesNames();
        return warehousesLiveData;
    }

    public void setFilterDate(Date dateTo) {
        outgoingPhaseTwoActivityRepository.getFilterDateMutableLiveData().setValue(dateTo);
    }

    public void setFilterDataHolder(FilterDataHolder filterDataHolder) {
        outgoingPhaseTwoActivityRepository.getFilterDataHolderMutableLiveData().setValue(filterDataHolder);
    }

    public MutableLiveData<EnumOutgoingStyle> getEnumStyleLiveData() {
        if (enumOutgoingStyleMutableLiveData == null) {
            enumOutgoingStyleMutableLiveData = new MutableLiveData<>();
        }

        return enumOutgoingStyleMutableLiveData;
    }

//    public void syncOutgoing(Date dateTo) {
//        outgoingPhaseTwoActivityRepository.syncOutgoings(dateTo);
//    }

    private List<Outgoing> filterOutgoings(MutableLiveData<List<Outgoing>> incomingListMutableLiveData, MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData) {
        List<Outgoing> filteredList = new ArrayList<>();
        FilterDataHolder filterDataHolder = filterDataHolderMutableLiveData.getValue();
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

    public void registerRealTimeOutgoings(Date dateTo) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseTwoActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }
        outgoingPhaseTwoActivityRepository.registerRealTimeOutgoings(dateTo, employeeID);
    }

    public void unregisterRealTimeOutgoings() {
        outgoingPhaseTwoActivityRepository.unregisterRealTimeOutgoings();
    }

    public void setOutgoingStyle(EnumOutgoingStyle currentOutgoingStyle) {
        getEnumStyleLiveData().setValue(currentOutgoingStyle);
    }

    public void sendAllOutgoing() {
        outgoingPhaseTwoActivityRepository.getResponseMutableLiveData().setValue(
                ApiResponse.prompt(resources.getString(R.string.outgoings_send_prompt),
                        (dialogInterface, i) -> outgoingPhaseTwoActivityRepository.sendAllOutgoing()));
    }
}
