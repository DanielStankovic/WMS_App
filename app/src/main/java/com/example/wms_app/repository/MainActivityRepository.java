package com.example.wms_app.repository;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.wms_app.R;
import com.example.wms_app.dao.EmployeeDao;
import com.example.wms_app.dao.WarehousePositionDao;
import com.example.wms_app.data.Api;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.ApplicationVersion;
import com.example.wms_app.model.Employee;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingProductionType;
import com.example.wms_app.model.IncomingType;
import com.example.wms_app.model.LoginModel;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.Partner;
import com.example.wms_app.model.PostRequest;
import com.example.wms_app.model.Product;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ProductCategory;
import com.example.wms_app.model.ProductItemType;
import com.example.wms_app.model.ReturnReason;
import com.example.wms_app.model.Truck;
import com.example.wms_app.model.Warehouse;
import com.example.wms_app.model.WarehouseObject;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.SyncResponse;
import com.example.wms_app.utilities.Utility;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityRepository {

    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu

    private MutableLiveData<SyncResponse> syncResponseMutableLiveData;

    private final Resources resources; //resursi za stringove

    private final RoomDb roomDatabase; // objekat room baze

    private final FirebaseFirestore firebaseFirestore;

    private int employeeIDDb = -1;

//    public MainActivityRepository(Context context) {
//
//        ApiClient.getApiClient().create(Api.class) = ApiClient.getApiClient().create(Api.class);
////        resources = context.getResources();
//        roomDatabase = RoomDb.getDatabase(context);
////        firebaseFirestore = FirebaseFirestore.getInstance();
//
//    }

    public MainActivityRepository(Context context) {

        this.resources = context.getResources();
        this.roomDatabase = RoomDb.getDatabase(context);
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        employeeIDDb = this.roomDatabase.employeeDao().getEmployeeID();
//        ApiClient.getApiClient().create(Api.class) = ApiClient.getApiClient().create(Api.class);

//        try {
//
////        resources = context.getResources();
////            roomDatabase = RoomDb.getDatabase(context);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
    }

    public MutableLiveData<SyncResponse> getSyncResponseMutableLiveData() {
        if (syncResponseMutableLiveData == null)
            syncResponseMutableLiveData = new MutableLiveData<>();

        return syncResponseMutableLiveData;
    }

    public void loginUser(String userName, String password, String deviceSerialNumber) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        Call<GenericResponse<Employee>> call = ApiClient.getApiClient().create(Api.class).loginUser(new LoginModel(userName, password, deviceSerialNumber));
        call.enqueue(new Callback<GenericResponse<Employee>>() {
            @Override
            public void onResponse(Call<GenericResponse<Employee>> call, Response<GenericResponse<Employee>> response) {
                try {
                    if (Utility.checkResponseFromServer(response)) {
                        GenericResponse<Employee> genericResponse = response.body();
                        if (genericResponse.isSuccess()) {
                            EmployeeDao employeeDao = roomDatabase.employeeDao();
                            //Logika za brisanje pozicija ako se employeeID sa servera i iz lokalne baze pre brisanja razlikuju
                            if (genericResponse.getData().getEmployeeID() != employeeDao.getEmployeeID()) {
                                WarehousePositionDao warehousePositionDao = roomDatabase.warehousePositionDao();
                                warehousePositionDao.deleteAllWarehousePositions();
                            }
                            employeeDao.deleteAllEmployee();
                            if (employeeDao.insert(genericResponse.getData()) > 0) {

                                Constants.EMPLOYEE_ID = genericResponse.getData().getEmployeeID();
                                employeeIDDb = employeeDao.getEmployeeID();
                                firebaseLogin(genericResponse.getData());
                            } else {
                                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_employee_insert)));
                            }
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(genericResponse.getMessage()));
                        }
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_login)));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse<Employee>> call, Throwable t) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.server_communication_error, t.getMessage())));
            }
        });

    }

    private void firebaseLogin(Employee employeeFromFb) {
        employeeFromFb.setLastLoginDate(new Date());
        CollectionReference loggedEmployees = firebaseFirestore.collection("employee");
        DocumentReference currentEmployee = loggedEmployees.document(Integer.toString(employeeFromFb.getEmployeeID()));
        currentEmployee.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            //Znaci da vec postoji na firebaseu pa ide provera za serijski broj
                            Employee employee = documentSnapshot.toObject(Employee.class);
                            if (employee != null) {
                                if (employeeFromFb.getSerialNumber().equals(employee.getSerialNumber())) {

                                    getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.sync_started)));
                                } else {
                                    currentEmployee.set(employeeFromFb).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.already_logged_another_device)));

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.firebase_emp_parse_error)));

                                        }
                                    });
                                }
                            } else {
                                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.firebase_emp_parse_error)));
                            }
                        } else {
                            //Znaci da ne postoji na Firebaseu pa mora prvo insert
                            currentEmployee.set(employeeFromFb).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.sync_started)));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.firebase_emp_insert_error)));
                                }
                            });
                        }
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_getting_emp_from_fb)));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });
    }

    public void syncApplicationVersion() throws IOException {
        Call<GenericResponse<ApplicationVersion>> call = ApiClient.getApiClient().create(Api.class).getApplicationVersion();
        Response<GenericResponse<ApplicationVersion>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<ApplicationVersion> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                roomDatabase.applicationVersionDao().delete(genericResponse.getData());
                if (Constants.versionCode == genericResponse.getData().getVersionCode()) {

                    roomDatabase.applicationVersionDao().insert(genericResponse.getData());
                }
                Constants.newVersionCode = genericResponse.getData().getVersionCode();
                Constants.newVersionName = genericResponse.getData().getVersionName();
                Constants.newVersionDownloadLink = genericResponse.getData().getDownloadLink();
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_app_version));
        }
    }

    public void syncProductCategory() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.productCategoryDao().getModifiedDate();
        Call<GenericResponse<List<ProductCategory>>> call = ApiClient.getApiClient().create(Api.class).getProductCategoryByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<ProductCategory>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<ProductCategory>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (ProductCategory productCategory : genericResponse.getData()) {
                    roomDatabase.productCategoryDao().delete(productCategory);
                    if (productCategory.isActive()) {
                        roomDatabase.productCategoryDao().insert(productCategory);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_product_category));
        }
    }

    public void syncProduct() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.productDao().getModifiedDate();
        Call<GenericResponse<List<Product>>> call = ApiClient.getApiClient().create(Api.class).getProductByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<Product>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<Product>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (Product product : genericResponse.getData()) {
                    roomDatabase.productDao().delete(product);
                    if (product.isActive()) {
                        roomDatabase.productDao().insert(product);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_product));
        }
    }

    public void syncPartner() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.partnerDao().getModifiedDate();
        Call<GenericResponse<List<Partner>>> call = ApiClient.getApiClient().create(Api.class).getPartnerByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<Partner>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<Partner>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (Partner partner : genericResponse.getData()) {
                    roomDatabase.partnerDao().delete(partner);
                    if (partner.isActive()) {
                        roomDatabase.partnerDao().insert(partner);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_partner));
        }
    }

    public void syncWarehouse() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.warehouseDao().getModifiedDate();
        Call<GenericResponse<List<Warehouse>>> call = ApiClient.getApiClient().create(Api.class).getWarehouseByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<Warehouse>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<Warehouse>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (Warehouse warehouse : genericResponse.getData()) {
                    roomDatabase.warehouseDao().delete(warehouse);
                    if (warehouse.isActive()) {
                        roomDatabase.warehouseDao().insert(warehouse);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_warehouse));
        }
    }

    public void syncWarehousePosition() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.warehousePositionDao().getModifiedDate();
        Call<GenericResponse<List<WarehousePosition>>> call = ApiClient.getApiClient().create(Api.class).getWarehousePosByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<WarehousePosition>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<WarehousePosition>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (WarehousePosition warehousePosition : genericResponse.getData()) {
                    roomDatabase.warehousePositionDao().delete(warehousePosition);
                    if (warehousePosition.isActive()) {
                        roomDatabase.warehousePositionDao().insert(warehousePosition);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_w_pos));
        }
    }

    public void syncWarehouseObject() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.warehouseObjectDao().getModifiedDate();
        Call<GenericResponse<List<WarehouseObject>>> call = ApiClient.getApiClient().create(Api.class).getWarehouseObjByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<WarehouseObject>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<WarehouseObject>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (WarehouseObject warehouseObject : genericResponse.getData()) {
                    roomDatabase.warehouseObjectDao().delete(warehouseObject);
                    if (warehouseObject.isActive()) {
                        roomDatabase.warehouseObjectDao().insert(warehouseObject);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_object));
        }
    }

    public void syncTruck() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }

        String modifiedDate = roomDatabase.truckDao().getModifiedDate();
        Call<GenericResponse<List<Truck>>> call = ApiClient.getApiClient().create(Api.class).getTruckByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<Truck>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<Truck>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (Truck truck : genericResponse.getData()) {
                    roomDatabase.truckDao().delete(truck);
                    if (truck.isActive()) {
                        roomDatabase.truckDao().insert(truck);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_truck));
        }
    }

    public void syncProductBox() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }

        String modifiedDate = roomDatabase.productBoxDao().getModifiedDate();
        Call<GenericResponse<List<ProductBox>>> call = ApiClient.getApiClient().create(Api.class).getProductBoxByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<ProductBox>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<ProductBox>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (ProductBox productBox : genericResponse.getData()) {
                    roomDatabase.productBoxDao().delete(productBox);
                    if (productBox.isActive()) {
                        roomDatabase.productBoxDao().insert(productBox);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_product_box));
        }
    }

    public void syncProductItemType() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }

        String modifiedDate = roomDatabase.productItemTypeDao().getModifiedDate();
        Call<GenericResponse<List<ProductItemType>>> call = ApiClient.getApiClient().create(Api.class).getProductItemTypeByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<ProductItemType>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<ProductItemType>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (ProductItemType productItemType : genericResponse.getData()) {
                        roomDatabase.productItemTypeDao().delete(productItemType);
                        roomDatabase.productItemTypeDao().insert(productItemType);
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_product_item));
        }
    }

    public void syncReturnReason()  throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.returnReasonDao().getModifiedDate();
        Call<GenericResponse<List<ReturnReason>>> call = ApiClient.getApiClient().create(Api.class).getReturnReasonByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<ReturnReason>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<ReturnReason>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (ReturnReason returnReason : genericResponse.getData()) {
                    roomDatabase.returnReasonDao().delete(returnReason);
                    if (returnReason.isActive()) {
                        roomDatabase.returnReasonDao().insert(returnReason);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_return_reason));
        }
    }

    public void syncIncomingType()  throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        String modifiedDate = roomDatabase.incomingTypeDao().getModifiedDate();
        Call<GenericResponse<List<IncomingType>>> call = ApiClient.getApiClient().create(Api.class).getIncomingTypeByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<IncomingType>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<IncomingType>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (IncomingType incomingType : genericResponse.getData()) {
                    roomDatabase.incomingTypeDao().delete(incomingType);
                    if (incomingType.isActive()) {
                        roomDatabase.incomingTypeDao().insert(incomingType);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_incoming_type));
        }
    }


    public void syncIncomingProductionType() throws IOException {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }

        String modifiedDate = roomDatabase.incomingProductionTypeDao().getModifiedDate();
        Call<GenericResponse<List<IncomingProductionType>>> call = ApiClient.getApiClient().create(Api.class).getIncomingProductionTypeByModDate(
                new PostRequest(modifiedDate == null ? "1900-01-01" : modifiedDate, emplID)
        );
        Response<GenericResponse<List<IncomingProductionType>>> response = call.execute();
        if (Utility.checkResponseFromServer(response)) {
            GenericResponse<List<IncomingProductionType>> genericResponse = response.body();
            if (genericResponse.isSuccess()) {
                for (IncomingProductionType incomingProductionType : genericResponse.getData()) {
                    roomDatabase.incomingProductionTypeDao().delete(incomingProductionType);
                    if (incomingProductionType.isActive()) {
                        roomDatabase.incomingProductionTypeDao().insert(incomingProductionType);
                    }
                }
            } else {
                throw new RuntimeException(genericResponse.getMessage());
            }
        } else {
            throw new RuntimeException(resources.getString(R.string.sync_error_incoming_production_type));
        }
    }

    public void syncIncoming() {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }
        final String[] modifiedDate = {null};

        final CollectionReference allIncomings = firebaseFirestore.collection("incomings");

        Query query = allIncomings.orderBy("modifiedDate").limitToLast(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Date modDate = null;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        modDate = document.getDate("modifiedDate");
                    }
                    if (modDate != null) {
                        modifiedDate[0] = Utility.getStringFromDateForServer(modDate);
                    }

                    Call<List<Incoming>> call = ApiClient.getApiClient().create(Api.class).getIncomingFirebase(
                            new PostRequest(modifiedDate[0] == null ? "1900-01-01" : modifiedDate[0], emplID));

                    call.enqueue(new Callback<List<Incoming>>() {
                        @Override
                        public void onResponse(Call<List<Incoming>> call, final Response<List<Incoming>> response) {
                            if (Utility.checkResponseFromServer(response)) {
                                pushToFirebase(response, allIncomings);

                            } else {
                                //sa servera nije dobio odgovor dobar
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Incoming>> call, Throwable t) {
                            Utility.writeErrorToFile(new Exception(t.getMessage()));
                            //nije uopste uspostavio vezu sa serverom
                        }
                    });
                }
            }
        });
    }


    public void syncOutgoing() {
        int emplID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (emplID == -1) {
            throw new RuntimeException(resources.getString(R.string.employee_id_invalid));
        }

        final String[] modifiedDate = {"1900-01-01"};

        final CollectionReference allOutgoings = firebaseFirestore.collection("outgoings");

        Query query = allOutgoings.orderBy("modifiedDate").limitToLast(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Date modDate = null;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        modDate = document.getDate("modifiedDate");
                    }
                    if (modDate != null) {
                        modifiedDate[0] = Utility.getStringFromDateForServer(modDate);
                    }

                    Call<List<Outgoing>> call = ApiClient.getApiClient().create(Api.class).getOutgoingFirebase(
                            new PostRequest(modifiedDate[0] == null ? "1900-01-01" : modifiedDate[0], emplID));

                    call.enqueue(new Callback<List<Outgoing>>() {
                        @Override
                        public void onResponse(Call<List<Outgoing>> call, final Response<List<Outgoing>> response) {
                            if (Utility.checkResponseFromServer(response)) {
                                pushToFirebaseOutgoing(response, allOutgoings);
//                                Call<String> updateCall = ApiClient.getApiClient().create(Api.class).updatePdaArriveOutgoing(
//                                        new PostRequest.PostRequestDate(modifiedDate[0] == null ? "1900-01-01" : modifiedDate[0], Utility.getStringFromDateForServer(new Date())));
//                                updateCall.enqueue(new Callback<String>() {
//                                    @Override
//                                    public void onResponse(Call<String> call, Response<String> response) {
//                                        if (response.body().equals("OK")) {
//
//                                            //uspesno je poslat na server pda ARRIVED
//                                        } else {
//                                            // desila se neka greska pri slanju pdaARRIVED na server
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<String> call, Throwable t) {
//                                        //nije uopste poslao zahtev na server
//                                    }
//                                });
                            } else {
                                //sa servera nije dobio odgovor dobar
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Outgoing>> call, Throwable t) {
                            Utility.writeErrorToFile(new Exception(t.getMessage()));
                            //nije uopste uspostavio vezu sa serverom
                        }
                    });
                }
            }
        });
    }

    private void pushToFirebaseOutgoing(Response<List<Outgoing>> response, CollectionReference allOutgoings) {
        List<Outgoing> outgoings = response.body();
        for (Outgoing outgoing : outgoings) {
            allOutgoings.document(outgoing.getOutgoingID()).set(outgoing);
        }
    }


    private void pushToFirebase(Response<List<Incoming>> response, CollectionReference allIncomings) {
        List<Incoming> incomings = response.body();
        for (Incoming incoming : incomings) {
            allIncomings.document(incoming.getIncomingID()).set(incoming);
        }
    }


    public void logoutEmployeeFromFirebase(int employeeId) {
        firebaseFirestore.collection("employee").document(String.valueOf(employeeId)).delete();
    }
}
