package com.example.wms_app.viewmodel.inventory;

import android.app.Application;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wms_app.R;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.repository.data.EmployeeRepository;
import com.example.wms_app.repository.data.ProductBoxRepository;
import com.example.wms_app.repository.data.WarehousePositionRepository;
import com.example.wms_app.repository.inventory.InventoryActivityRepository;
import com.example.wms_app.utilities.ApiResponse;

import java.util.ArrayList;
import java.util.List;


public class InventoryViewModel extends AndroidViewModel {


    private WarehousePositionRepository warehousePositionRepository;
    private InventoryActivityRepository inventoryActivityRepository;
    private final Resources resources;
    private final EmployeeRepository employeeRepository;
    private ProductBoxRepository productBoxRepository;
    private LiveData<List<WarehousePosition>> mAllPositions;
    private LiveData<List<ProductBox>> mAllProducts;
    private LiveData<String> mCurrentInventoryID;
    private MutableLiveData<List<InventoryDetailsResult>> idrLastL = new MutableLiveData<>();


    public InventoryViewModel(@NonNull Application application) {
        super(application);

        resources = application.getResources();
        inventoryActivityRepository = new InventoryActivityRepository(application.getApplicationContext());
        warehousePositionRepository = new WarehousePositionRepository(application);
        productBoxRepository = new ProductBoxRepository(application.getApplicationContext());
        employeeRepository = new EmployeeRepository(application.getApplicationContext());

        mAllPositions = warehousePositionRepository.getWarehousePosMutableLiveData();
        mAllProducts = productBoxRepository.getAllProductBox();
    }

    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        return inventoryActivityRepository.getResponseMutableLiveData();
    }


    public LiveData<List<WarehousePosition>> getAllPositions() {
        return mAllPositions;
    }

    public LiveData<List<ProductBox>> getAllProducts() {
        return mAllProducts;
    }

    public void syncInventory(int employeeID) {
        if (employeeID == -1) {
            inventoryActivityRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }
        inventoryActivityRepository.checkInventoryOpen(employeeID);
    }


    //za rad sa privremenom listom
    public void setIdrLastList(List<InventoryDetailsResult> idrLast) {
        idrLastL.setValue(idrLast);
    }

    public LiveData<List<InventoryDetailsResult>> getIdrLastList() {
        return idrLastL;
    }

    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeRepository.getEmployeeIDLiveData();
    }

    public void emptyTempList() {
        List<InventoryDetailsResult> idr = new ArrayList<>();
        setIdrLastList(idr);
    }

    public void undoTempList() {
        List<InventoryDetailsResult> idr = getIdrLastList().getValue();
        if (idr != null && idr.size() > 0) {
            idr.remove(idr.size() - 1);
            setIdrLastList(idr);
        }
    }

    public void deleteIdrLastListItem(int index) {
        List<InventoryDetailsResult> tempList = idrLastL.getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(index);
            setIdrLastList(tempList);
        }
    }

    public LiveData<List<InventoryDetailsResult>> getInventoryDetailsResult(){
        return inventoryActivityRepository.getInventoryDetailsResultMutLiveData();
    }

    public LiveData<String> getCurrentInventoryID(){
        return  inventoryActivityRepository.getInventoryIDLiveData();
    }
    public void setCurrentInventoryID(){
        mCurrentInventoryID = inventoryActivityRepository.getInventoryIDLiveData();
    }

    public void pushInventoryResult(List<InventoryDetailsResult> idr, String inventoryID){
        List<InventoryDetailsResult> list = new ArrayList<>();
        setIdrLastList(list);
        inventoryActivityRepository.pushInventoryResult(idr, inventoryID);
    }

    public void syncInventoryDetailsResult(String inventoryID){
        inventoryActivityRepository.syncInventoryDetailsResult(inventoryID);
    }

    //region RAD sa InventoryDoneFragment
    public void deleteProductFromPosition(InventoryDetailsResult idr){
        inventoryActivityRepository.deleteProductFromPosition(idr);
    }

    public void syncIncomingDetailsResult(String inventoryID){
        inventoryActivityRepository.syncInventoryDetailsResult(inventoryID);
    }

    public void sendInventoryToServer() {
        inventoryActivityRepository.sendInventoryToServer(getCurrentInventoryID().getValue());
    }

}
