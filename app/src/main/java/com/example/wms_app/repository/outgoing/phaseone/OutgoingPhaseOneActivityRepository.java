package com.example.wms_app.repository.outgoing.phaseone;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.wms_app.R;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OutgoingPhaseOneActivityRepository {

    private final FirebaseFirestore firebaseFirestore;
    private final Resources resources; //resursi za stringove
    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu
    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija prijema sa firebase-a
    private ListenerRegistration listenerRegistration; //Varijabla za realTimeFirebase osluskivanje
    private MutableLiveData<Date> filterDateMutableLiveData;
    private MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData;
    private MutableLiveData<List<String>> citiesMutableLiveData;
    private MutableLiveData<List<Outgoing>> outgoingListMutableLiveData;


    public OutgoingPhaseOneActivityRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
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

    public MutableLiveData<List<Outgoing>> getOutgoingListMutableLiveData() {
        if (outgoingListMutableLiveData == null)
            outgoingListMutableLiveData = new MutableLiveData<>();

        return outgoingListMutableLiveData;
    }

    public MutableLiveData<FilterDataHolder> getFilterDataHolderMutableLiveData() {
        if (filterDataHolderMutableLiveData == null) {
            filterDataHolderMutableLiveData = new MutableLiveData<>();
            filterDataHolderMutableLiveData.setValue(FilterDataHolder.getFilterDataPlaceholder());
        }

        return filterDataHolderMutableLiveData;
    }

    public MutableLiveData<List<String>> getCitiesMutableLiveData() {
        if (citiesMutableLiveData == null)
            citiesMutableLiveData = new MutableLiveData<>();

        return citiesMutableLiveData;
    }

//    public void syncOutgoings(Date dateTo) {
//
//        getResponseMutableLiveData().setValue(ApiResponse.loading());
//        // isFirstSync = true;
//        DocumentReference employeeOutgoings = firebaseFirestore.collection("employee_outgoing").document(String.valueOf(Constants.EMPLOYEE_ID));
//        employeeOutgoings.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot documentSnapshot = task.getResult();
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    List<String> outgoingIDList = (List<String>) documentSnapshot.get("outgoingIDList");
//                    if (outgoingIDList != null && outgoingIDList.isEmpty()) {
//                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_outgoing_for_employee)));
//                        return;
//                    }
//                    List<List<String>> wrapperOutgoingIDList = Partition.ofSize(outgoingIDList, Constants.FIRESTORE_IN_QUERY_LIMIT);
//                    int wrapperSize = wrapperOutgoingIDList.size();
//                    int i = 0;
//                    List<Outgoing> outgoingList = new ArrayList<>();
//                    getOutgoingsFromFirebase(dateTo, wrapperOutgoingIDList, wrapperSize, i, outgoingList);
//                } else {
//                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_outgoing_for_employee)));
//                }
//            }
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//            Utility.writeErrorToFile(e);
//        });
//
//    }
//
//    private void getOutgoingsFromFirebase(Date dateTo, List<List<String>> wrapperOutgoingIDList,
//                                          int wrapperSize, int i, List<Outgoing> outgoingList) {
//
//        Query query = firebaseFirestore.collection("outgoings")
//                .whereLessThanOrEqualTo("outgoingDate", dateTo)
//                .whereIn("outgoingID", wrapperOutgoingIDList.get(i))
//                .whereEqualTo("finished", false)
//                .orderBy("outgoingDate", Query.Direction.ASCENDING);
//
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    QuerySnapshot querySnapshot = task.getResult();
//                    if (querySnapshot != null) {
//                        outgoingList.addAll(querySnapshot.toObjects(Outgoing.class));
//                        //Provera da li je ova inner lista poslednja u wrapper listi
//                        if (wrapperSize - 1 == i) {
//                            //znaci da je poslednja i ovde imam listu svih pozicija koje su skenirane u svim prijemima koji se salju
//
//                            outgoingList.removeIf(x ->
//                                    x.getOutgoingStatusCode().equals("01") ||
//                                            x.getOutgoingStatusCode().equals("04") ||
//                                            x.getOutgoingStatusCode().equals("06") ||
//                                            x.getOutgoingStatusCode().equals("07") ||
//                                            x.getOutgoingStatusCode().equals("08"));
//
//                            getOutgoingListMutableLiveData().setValue(outgoingList);
//
//
//                            getResponseMutableLiveData().postValue(ApiResponse.success());
//
//                        } else {
//                            int a = i + 1;
//                            getOutgoingsFromFirebase(dateTo, wrapperOutgoingIDList, wrapperSize, a, outgoingList);
//                        }
//                    }
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//                Utility.writeErrorToFile(e);
//            }
//        });
//
//    }

    public void unregisterRealTimeOutgoings() {
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    public void registerRealTimeOutgoings(Date dateTo, int employeeID) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        CollectionReference outgoings = firebaseFirestore.collection("outgoings");
        Query query = outgoings
                .whereLessThanOrEqualTo("outgoingDate", dateTo)
                .whereIn("outgoingStatusCode", Arrays.asList("02", "03", "04", "05"))
                .whereArrayContains("outgoingEmployee", employeeID)
                .whereEqualTo("finished", false)
                .orderBy("outgoingDate", Query.Direction.ASCENDING);
        ;

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    if (value != null) {
                        List<Outgoing> outgoingList = value.toObjects(Outgoing.class);
                        List<String> cityList = getListOfCities(outgoingList);
                        getOutgoingListMutableLiveData().setValue(outgoingList);
                        getCitiesMutableLiveData().setValue(cityList);

                        if (isFirstSync) {
                            getResponseMutableLiveData().postValue(ApiResponse.success());
                            isFirstSync = false;
                        }

                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.outgoing_sync_error)));
                    }
                }
            }
        });
    }

    private List<String> getListOfCities(List<Outgoing> outgoingList) {
        return outgoingList
                .stream()
                .map(Outgoing::getPartnerCity)
                .distinct()
                .collect(Collectors.toList());
    }
}
