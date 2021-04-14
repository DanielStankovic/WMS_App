package com.example.wms_app.repository.incoming.standard;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.example.wms_app.R;
import com.example.wms_app.data.Api;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetailsResult;
import com.example.wms_app.model.IncomingForServerWrapper;
import com.example.wms_app.model.IncomingTruckResult;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Utility;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingActivityRepository {
    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu
    private final FirebaseFirestore firebaseFirestore;
    // private final Api apiReference; //Retrofit objekat za komunikaciju sa serverom
    private final Resources resources; //resursi za stringove
    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija prijema sa firebase-a
    private ListenerRegistration listenerRegistration; //Varijabla za realTimeFirebase osluskivanje
    private MutableLiveData<List<Incoming>> incomingListMutableLiveData;
    private MutableLiveData<Date> filterDateMutableLiveData;
    private MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData;
    private MutableLiveData<List<String>> citiesMutableLiveData;
    private MutableLiveData<List<String>> warehousesMutableLiveData;


    public IncomingActivityRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
        // apiReference = ApiClient.getApiClient().create(Api.class);
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
    }

    public MutableLiveData<List<Incoming>> getIncomingListMutableLiveData() {
        if (incomingListMutableLiveData == null)
            incomingListMutableLiveData = new MutableLiveData<>();

        return incomingListMutableLiveData;
    }

    public MutableLiveData<List<String>> getCitiesMutableLiveData() {
        if (citiesMutableLiveData == null)
            citiesMutableLiveData = new MutableLiveData<>();

        return citiesMutableLiveData;
    }

    public MutableLiveData<List<String>> getWarehousesMutableLiveData() {
        if (warehousesMutableLiveData == null)
            warehousesMutableLiveData = new MutableLiveData<>();

        return warehousesMutableLiveData;
    }

    public MutableLiveData<Date> getFilterDateMutableLiveData() {
        if (filterDateMutableLiveData == null) {
            filterDateMutableLiveData = new MutableLiveData<>();
            LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusDays(365).with(LocalTime.MAX);
            filterDateMutableLiveData.setValue(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }
        return filterDateMutableLiveData;
    }

    public MutableLiveData<FilterDataHolder> getFilterDataHolderMutableLiveData() {
        if (filterDataHolderMutableLiveData == null) {
            filterDataHolderMutableLiveData = new MutableLiveData<>();
            filterDataHolderMutableLiveData.setValue(FilterDataHolder.getFilterDataPlaceholder());
        }

        return filterDataHolderMutableLiveData;
    }

    public void registerRealTimeIncomings(Date dateTo) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        CollectionReference incomings = firebaseFirestore.collection("incomings");
        Query query = incomings
                .whereLessThanOrEqualTo("incomingDate", dateTo)
                .whereIn("incomingStatusCode", Arrays.asList("02", "03", "04", "05", "08"))
                .whereEqualTo("finished", false)
                .whereEqualTo("incomingTypeCode", "02");
        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                Utility.writeErrorToFile(error);
            } else {
                if (value != null) {

                    List<Incoming> incList = value.toObjects(Incoming.class);
                    //  incList.removeIf(x -> x.getIncomingTypeCode().equals("01"));
                    List<String> cityList = getListOfCities(incList);
                    getIncomingListMutableLiveData().setValue(incList);
                    getCitiesMutableLiveData().setValue(cityList);

                    if (isFirstSync) {
                        getResponseMutableLiveData().postValue(ApiResponse.success());
                        isFirstSync = false;
                    }
                } else {
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_sync_error)));
                }
            }
        });

    }

    private List<String> getListOfCities(List<Incoming> incList) {
        return incList
                .stream()
                .map(Incoming::getPartnerCity)
                .distinct()
                .collect(Collectors.toList());
    }

    public void unregisterRealTimeIncomings() {
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    public void sendIncomingToServer() {
        if (getIncomingListMutableLiveData().getValue() != null) {
            List<Incoming> incomingToBeSent = getIncomingListMutableLiveData().getValue().stream()
                    .filter(x -> (x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_COMPLETELY))
                            ||
                            x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_WITH_SURPLUS)))
                            && !x.isFinished())
                    .collect(Collectors.toList());

            List<String> incomingIDListToBeSent = incomingToBeSent.stream()
                    .map(Incoming::getIncomingID)
                    .distinct()
                    .collect(Collectors.toList());

            //Provera da li uopste postoje neposlati nalozi koji su zavrseni.
            if (incomingIDListToBeSent.isEmpty()) {
                getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_inc_to_be_sent)));
                return;
            }

            if (incomingIDListToBeSent.size() > Constants.FIRESTORE_IN_QUERY_LIMIT) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.firestore_in_limit_reached,
                        Constants.FIRESTORE_IN_QUERY_LIMIT, incomingIDListToBeSent.size())));
                return;
            }


            getResponseMutableLiveData().setValue(ApiResponse.loading());

            //Znaci da postoje neposlati zavrseni i ide dalje logika da se dobiju njihovi IncomingDetailsResult koji nisu poslati
            Query query = firebaseFirestore.collectionGroup("IncomingDetailsResult")
                    .whereIn("incomingId", incomingIDListToBeSent)
                    .whereEqualTo("sent", false);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null) {
                    List<IncomingDetailsResult> incomingDetailsResultList = queryDocumentSnapshots.toObjects(IncomingDetailsResult.class);
                    if (!incomingDetailsResultList.isEmpty()) {
                        getIncomingTruckResults(incomingToBeSent, incomingIDListToBeSent, incomingDetailsResultList);
                    }
                }
            }).addOnFailureListener(e -> {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            });


        }

    }

    private void getIncomingTruckResults(List<Incoming> incomingToBeSent, List<String> incomingIDListToBeSent,
                                         List<IncomingDetailsResult> incomingDetailsResultList) {

        //Ovde ide dovlacenje liste kamiona
        Query query = firebaseFirestore.collectionGroup("IncomingTruckResult")
                .whereIn("incomingID", incomingIDListToBeSent)
                .whereEqualTo("sent", false);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                List<IncomingTruckResult> incomingTruckList = queryDocumentSnapshots.toObjects(IncomingTruckResult.class);
                sendIncomingsToServer(incomingToBeSent, incomingDetailsResultList, incomingTruckList);
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    private void sendIncomingsToServer(List<Incoming> incomingToBeSent,
                                       List<IncomingDetailsResult> incomingDetailsResultList,
                                       List<IncomingTruckResult> incomingTruckList) {

        List<IncomingForServerWrapper> incomingForServerWrapperList = new ArrayList<>();
        for (Incoming incoming : incomingToBeSent) {
            incomingForServerWrapperList.add(
                    new IncomingForServerWrapper(
                            incoming.getIncomingID(),
                            incoming.getIncomingStatusCode(),
                            incomingDetailsResultList.stream()
                                    .filter(x -> x.getIncomingId().equals(incoming.getIncomingID()))
                                    .collect(Collectors.toList()),
                            incomingTruckList.stream()
                                    .filter(x -> x.getIncomingID().equals(incoming.getIncomingID()))
                                    .collect(Collectors.toList())
                    )
            );
        }

        Call<GenericResponse<String>> call = ApiClient.getApiClient().create(Api.class).sendAllIncomingDetailsResultToServer(incomingForServerWrapperList);
        call.enqueue(new Callback<GenericResponse<String>>() {
            @Override
            public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                try {
                    if (Utility.checkResponseFromServer(response)) {
                        GenericResponse<String> genericResponse = response.body();
                        if (genericResponse.isSuccess()) {
                            //Poslato je na server i sada ide update stanja magacina na firebaseu
                            updateIsSentOnIncomingDetailsResult(incomingToBeSent, incomingDetailsResultList, incomingTruckList);
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(genericResponse.getMessage()));
                        }
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_all_incoming, "")));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_all_incoming, t.getMessage())));
            }
        });

    }

//    private void updateWarehouseStatusPosition(List<IncomingForServerWrapper> incomingForServerWrapperList) {
//        //Dobijanje liste unique barkodova svih pozicija na koje je nesto postavljano iz svih prijema
//        List<String> uniquePositionBarcodes = incomingForServerWrapperList.stream()
//                .flatMap(x -> x.getIncomingDetailsResultList().stream())
//                .map(IncomingDetailsResult::getWarehousePositionBarcode)
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<List<String>> wrapperUniquePosBarcodeList = Partition.ofSize(uniquePositionBarcodes, Constants.FIRESTORE_IN_QUERY_LIMIT);
//        int wrapperSize = wrapperUniquePosBarcodeList.size();
//        int i = 0;
//        List<WarehouseStatusPosition> wspList = new ArrayList<>();
//
//        getWarehouseStatusPositionsFromFirebase(incomingForServerWrapperList, wrapperSize, i, wrapperUniquePosBarcodeList, wspList);
//    }
//
//    private void getWarehouseStatusPositionsFromFirebase(List<IncomingForServerWrapper> wrapperIncomingDetailsResultList, int wrapperSize, int i, List<List<String>> wrapperUniquePosBarcodeList, List<WarehouseStatusPosition> wspList) {
//
//        Query positions = firebaseFirestore.collection("WarehouseStatusPos")
//                .whereIn("warehousePositionBarcode", wrapperUniquePosBarcodeList.get(i));
//        positions.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                QuerySnapshot querySnapshot = task.getResult();
//                if (querySnapshot != null) {
//                    wspList.addAll(querySnapshot.toObjects(WarehouseStatusPosition.class));
//                    //Provera da li je ova inner lista poslednja u wrapper listi
//                    if (wrapperSize - 1 == i) {
//                        //znaci da je poslednja i ovde imam listu svih pozicija koje su skenirane u svim prijemima koji se salju
//                        updateWarehousePositions(wrapperIncomingDetailsResultList, wspList);
//                    } else {
//                        int a = i + 1;
//                        getWarehouseStatusPositionsFromFirebase(wrapperIncomingDetailsResultList, wrapperSize, a, wrapperUniquePosBarcodeList, wspList);
//                    }
//                }
//            }
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//    }
//
//    private void updateWarehousePositions(List<IncomingForServerWrapper> wrapperIncomingDetailsResultList,
//                                          List<WarehouseStatusPosition> wspList) {
//
//        WriteBatch writeBatch = firebaseFirestore.batch();
//        //Prolazak kroz sve pozicije
//        for (WarehouseStatusPosition wsp : wspList) {
//
//            //wsp details za odredjenu poziciju
//            List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList = wsp.getWspDetails();
//            //Prolazak kroz sve artikle koji se nalaze na IncomingDetailsResult ali imaju barkod pozicije kao ova sto je trenutno filtrirana
//            for (IncomingDetailsResult idr : wrapperIncomingDetailsResultList.stream()
//                    .flatMap(x -> x.getIncomingDetailsResultList().stream())
//                    .filter(x -> x.getWarehousePositionBarcode().equals(wsp.getWarehousePositionBarcode())).collect(Collectors.toList())) {
//
//                  /*Posto u objektu OutgoingDetailsResult nemam isSerialMust scan polje,
//                        onda tu proveru vrsim preko serijskog broja koju imam. Inace bih za svaki morao da filtriram
//                        listu proizvoda.
//
//                        * */
//                //Radi se o isSerialMustScan = false
//                if (idr.getSerialNo().equals("")) {
//
//                    WarehouseStatusPositionDetails productBoxOnPos = warehouseStatusPositionDetailsList
//                            .stream()
//                            .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo() == null)
//                            .findAny().orElse(null);
//
//                    if (productBoxOnPos != null) {
//                        /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
//                         * */
//                        warehouseStatusPositionDetailsList.stream()
//                                .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo() == null)
//                                .forEach(x ->
//                                {
//                                    x.setQuantity(x.getQuantity() + (int) idr.getQuantity());
//                                    x.setModifiedDate(new Date());
//
//                                });
//
//                    } else {
//                        /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
//                         * */
//                        productBoxOnPos = new WarehouseStatusPositionDetails();
//                        productBoxOnPos.setProductBoxID(idr.getProductBoxID());
//                        productBoxOnPos.setSerialNo(null);
//                        productBoxOnPos.setModifiedDate(new Date());
//                        productBoxOnPos.setQuantity((int) idr.getQuantity());
//                        warehouseStatusPositionDetailsList.add(productBoxOnPos);
//                    }
//
//                } else {
//                    //Radi se o isSerialMustScan = true
//                    WarehouseStatusPositionDetails productBoxOnPos = warehouseStatusPositionDetailsList
//                            .stream()
//                            .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo().equals(idr.getSerialNo()))
//                            .findAny().orElse(null);
//
//                    if (productBoxOnPos == null) {
//
//                        /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
//                         * */
//                        productBoxOnPos = new WarehouseStatusPositionDetails();
//                        productBoxOnPos.setProductBoxID(idr.getProductBoxID());
//                        productBoxOnPos.setSerialNo(idr.getSerialNo());
//                        productBoxOnPos.setModifiedDate(new Date());
//                        productBoxOnPos.setQuantity((int) idr.getQuantity());
//                        warehouseStatusPositionDetailsList.add(productBoxOnPos);
//
//
//                    } else {
//                        /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
//                         * */
//                        warehouseStatusPositionDetailsList.stream()
//                                .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo().equals(idr.getSerialNo()))
//                                .forEach(x ->
//                                {
//                                    x.setQuantity(x.getQuantity() + (int) idr.getQuantity());
//                                    x.setModifiedDate(new Date());
//
//                                });
//
//                    }
//
//                }
//
//            }
//            //Update pozicija na firebaseu.
//            DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(wsp.getWarehousePositionBarcode());
//            writeBatch.update(documentReference, "wspDetails", warehouseStatusPositionDetailsList);
//
//        }
//
//        writeBatch.commit().addOnSuccessListener(aVoid -> {
//            //Ovde se sve poslalo na WarehouseStatusPos i updatovala se kolicina. Ovde onda sada ide update na stavkama na IncomingDetailsResult
//
//            updateIsSentOnIncomingDetailsResult(wrapperIncomingDetailsResultList);
//
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//    }

    private void updateIsSentOnIncomingDetailsResult(List<Incoming> incomingToBeSent,
                                                     List<IncomingDetailsResult> incomingDetailsResultList,
                                                     List<IncomingTruckResult> incomingTruckList) {

        //Ovde sada ide update rezltata na sent = true i za artikle i za kamione
        //Posle ide update i za nalog na finished = true
        WriteBatch writeBatch = firebaseFirestore.batch();
        List<String> incomingIDListToBeSent = incomingToBeSent.stream()
                .map(Incoming::getIncomingID)
                .distinct()
                .collect(Collectors.toList());

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        CollectionReference incomings = firebaseFirestore.collection("incomings");

        for (String incomingID : incomingIDListToBeSent) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomings.document(incomingID), "finished", true);
            operationCounter++;
        }

        for (IncomingDetailsResult idr : incomingDetailsResultList) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomings.document(idr.getIncomingId()).collection("IncomingDetailsResult").document(idr.getIdrFirebaseID()), "sent", true);
            operationCounter++;

        }

        for (IncomingTruckResult itr : incomingTruckList) {

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomings.document(itr.getIncomingID()).collection("IncomingTruckResult").document(itr.getItrFirebaseID()), "sent", true);
            operationCounter++;
        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;

        sendBatches(batchListSize, batchSendCounter, batchList, R.string.all_incoming_sent_successfully);


//        Query query = firebaseFirestore.collectionGroup("IncomingDetailsResult")
//                .whereIn("incomingId", incomingIDListToBeSent)
//                .whereEqualTo("sent", false);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//                queryDocumentSnapshots.getDocuments().forEach(x -> writeBatch.update(x.getReference(), "sent", true));
//                CollectionReference incomings = firebaseFirestore.collection("incomings");
//                for (String incomingID : incomingIDListToBeSent) {
//                    writeBatch.update(incomings.document(incomingID), "finished", true);
//                }
//                writeBatch.commit().addOnSuccessListener(aVoid -> updateIsSentOnIncomingTruckResult(incomingIDListToBeSent)).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));

    }

//    private void updateIsSentOnIncomingTruckResult(List<String> incomingIDListToBeSent) {
//        WriteBatch writeBatch = firebaseFirestore.batch();
//        Query query = firebaseFirestore.collectionGroup("IncomingTruckResult")
//                .whereIn("incomingID", incomingIDListToBeSent)
//                .whereEqualTo("sent", false);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//                queryDocumentSnapshots.getDocuments().forEach(x -> writeBatch.update(x.getReference(), "sent", true));
//                writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.all_incoming_sent_successfully)));
//                    }
//                }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//    }

    private void sendBatches(int batchListSize, int batchSendCounter, List<WriteBatch> batchList, @StringRes int messageResourceID) {
        batchList.get(batchSendCounter).commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                //Provera da li je ovo poslednji batch
                if (batchListSize - 1 == batchSendCounter) {
                    //Znaci da je poslednji batch u listi
                    getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(messageResourceID)));
                } else {

                    //Znaci da ima jos batcheva za slanje
                    int increasedBatchCounter = batchSendCounter + 1;
                    sendBatches(batchListSize, increasedBatchCounter, batchList, messageResourceID);
                }
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }
}
