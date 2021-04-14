package com.example.wms_app.repository.outgoing.phasetwo;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.example.wms_app.R;
import com.example.wms_app.data.Api;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingForServerWrapper;
import com.example.wms_app.model.OutgoingTruckResult;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Partition;
import com.example.wms_app.utilities.Utility;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingPhaseTwoActivityRepository {

    private final FirebaseFirestore firebaseFirestore;
    private final Resources resources; //resursi za stringove
  //  private final Api apiReference; //Retrofit objekat za komunikaciju sa serverom
    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija prijema sa firebase-a
    private ListenerRegistration listenerRegistration; //Varijabla za realTimeFirebase osluskivanje
    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu
    private MutableLiveData<Date> filterDateMutableLiveData;
    private MutableLiveData<FilterDataHolder> filterDataHolderMutableLiveData;
    private MutableLiveData<List<String>> citiesMutableLiveData;
    private MutableLiveData<List<Outgoing>> outgoingListMutableLiveData;
    private List<DocumentSnapshot> outgoingDetailsResultDocumentList;
    private List<OutgoingDetailsResult> outgoingDetailsResultList;
    private List<DocumentSnapshot> outgoingTruckResultDocumentList;
    private List<OutgoingTruckResult> outgoingTruckResultList;
    private List<Outgoing> outgoingsToBeSentList;
    private List<String> outgoingIDToBeSentList;

    public OutgoingPhaseTwoActivityRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
        // apiReference = ApiClient.getApiClient().create(Api.class);

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

    public MutableLiveData<List<Outgoing>> getOutgoingListMutableLiveData() {
        if (outgoingListMutableLiveData == null)
            outgoingListMutableLiveData = new MutableLiveData<>();

        return outgoingListMutableLiveData;
    }

    private List<DocumentSnapshot> getOutgoingDetailsResultDocumentList() {
        if (outgoingDetailsResultDocumentList == null)
            outgoingDetailsResultDocumentList = new ArrayList<>();
        return outgoingDetailsResultDocumentList;
    }

    private List<OutgoingDetailsResult> getOutgoingDetailsResultList() {
        if (outgoingDetailsResultList == null)
            outgoingDetailsResultList = new ArrayList<>();
        return outgoingDetailsResultList;
    }

    private List<DocumentSnapshot> getOutgoingTruckResultDocumentList() {
        if (outgoingTruckResultDocumentList == null)
            outgoingTruckResultDocumentList = new ArrayList<>();
        return outgoingTruckResultDocumentList;
    }

    private List<OutgoingTruckResult> getOutgoingTruckResultList() {
        if (outgoingTruckResultList == null)
            outgoingTruckResultList = new ArrayList<>();
        return outgoingTruckResultList;
    }

    private List<Outgoing> getOutgoingsToBeSentList() {
        if (outgoingsToBeSentList == null)
            outgoingsToBeSentList = new ArrayList<>();
        return outgoingsToBeSentList;
    }

    private List<String> getOutgoingIDToBeSentList() {
        if (outgoingIDToBeSentList == null)
            outgoingIDToBeSentList = new ArrayList<>();
        return outgoingIDToBeSentList;
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

    //Ovo je stari deo kada se vezivanje vrsilo kroz posebnu kolekciju na firebase-u.
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


    private List<String> getListOfCities(List<Outgoing> outgoingList) {
        return outgoingList
                .stream()
                .map(Outgoing::getPartnerCity)
                .distinct()
                .collect(Collectors.toList());
    }

    public void unregisterRealTimeOutgoings() {
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }


    private void getOutgoingsFromFirebase(Date dateTo, List<List<String>> wrapperOutgoingIDList,
                                          int wrapperSize, int i, List<Outgoing> outgoingList) {

        Query query = firebaseFirestore.collection("outgoings")
                .whereLessThanOrEqualTo("outgoingDate", dateTo)
                .whereIn("outgoingID", wrapperOutgoingIDList.get(i))
                .whereEqualTo("finished", false)
                .orderBy("outgoingDate", Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        outgoingList.addAll(querySnapshot.toObjects(Outgoing.class));
                        //Provera da li je ova inner lista poslednja u wrapper listi
                        if (wrapperSize - 1 == i) {
                            //znaci da je poslednja i ovde imam listu svih pozicija koje su skenirane u svim prijemima koji se salju

                            outgoingList.removeIf(x ->
                                    x.getOutgoingStatusCode().equals("01") ||
                                            x.getOutgoingStatusCode().equals("06") ||
                                            x.getOutgoingStatusCode().equals("07") ||
                                            x.getOutgoingStatusCode().equals("08") ||
                                            x.isFinished());

                            getOutgoingListMutableLiveData().setValue(outgoingList);
                            //TODO Ovde treba logika za filtriranje gradova

                            getResponseMutableLiveData().postValue(ApiResponse.success());

                        } else {
                            int a = i + 1;
                            getOutgoingsFromFirebase(dateTo, wrapperOutgoingIDList, wrapperSize, a, outgoingList);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                Utility.writeErrorToFile(e);
            }
        });

    }

    public void sendAllOutgoing() {
        if (getOutgoingListMutableLiveData().getValue() != null) {
            getResponseMutableLiveData().setValue(ApiResponse.loading());

            //Ciscenje liste naloga pre pozivanja bilo cega
            getOutgoingsToBeSentList().clear();
            getOutgoingIDToBeSentList().clear();

            getOutgoingsToBeSentList().addAll(
                    getOutgoingListMutableLiveData().getValue()
                            .stream()
                            .filter(x -> (x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_FINISHED_COMPLETELY)))
                                    && !x.isFinished())
                            .collect(Collectors.toList())
            );

            getOutgoingIDToBeSentList().addAll(
                    getOutgoingsToBeSentList().stream()
                            .map(Outgoing::getOutgoingID)
                            .distinct()
                            .collect(Collectors.toList())
            );

//            //Lista naloga koji se vide u pregledu
//            List<String> outgoingIDListToBeSent = getOutgoingListMutableLiveData().getValue().stream()
//                    .filter(x -> (x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_FINISHED_COMPLETELY)))
//                            && !x.isFinished())
//                    .map(Outgoing::getOutgoingID)
//                    .distinct()
//                    .collect(Collectors.toList());

            //Provera da li uopste postoje neposlati nalozi koji su za slanje.
            if (getOutgoingIDToBeSentList().isEmpty()) {
                getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_out_to_be_sent)));
                return;
            }

            List<List<String>> wrapperOutgoingIDsList = Partition.ofSize(getOutgoingIDToBeSentList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
            int wrapperSize = wrapperOutgoingIDsList.size();
            int i = 0;

            //ciscenje listi pre pozivanja firebase-a
            getOutgoingDetailsResultDocumentList().clear();
            getOutgoingDetailsResultList().clear();

            getOutgoingDetailsResultToBeSent(wrapperSize, i, wrapperOutgoingIDsList);
        }
    }

    private void getOutgoingDetailsResultToBeSent(int wrapperSize, int i,
                                                  List<List<String>> wrapperOutgoingIDsList) {

        Query query = firebaseFirestore.collectionGroup("OutgoingDetailsResult")
                .whereIn("outgoingID", wrapperOutgoingIDsList.get(i))
                .whereEqualTo("sent", false);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    //Ovde dodajem sva dokuemnta u listu. Ova lista ce mi trebati kasnije kada treba da ih updatujem
                    //sent na true
                    getOutgoingDetailsResultDocumentList().addAll(queryDocumentSnapshots.getDocuments());
                    //Dobijanje liste OurgoingDetailsResulta koji se nalaze na otpremama za slanje, a imaju sent na false
                    getOutgoingDetailsResultList().addAll(queryDocumentSnapshots.toObjects(OutgoingDetailsResult.class));
                    //Provera da li je ova inner lista poslednja u wrapper listi
                    if (wrapperSize - 1 == i) {
                        //provera da li postoji bilo koja stavka za slanje
                        if (getOutgoingDetailsResultList().isEmpty()) {
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_out_to_be_sent)));
                            return;
                        }
                        //znaci da je poslednja i ovde imam listu svih OutgoingDetailsResulta koji treba da se updatuju na sent = true
                        getOutgoingTruckResult();
                    } else {
                        int a = i + 1;
                        getOutgoingDetailsResultToBeSent(wrapperSize, a, wrapperOutgoingIDsList);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });
    }

    private void getOutgoingTruckResult() {

        List<List<String>> wrapperOutgoingIDsList = Partition.ofSize(getOutgoingIDToBeSentList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
        int wrapperSize = wrapperOutgoingIDsList.size();
        int i = 0;

        //ciscenje listi pre pozivanja firebase-a
        getOutgoingTruckResultDocumentList().clear();
        getOutgoingTruckResultList().clear();

        getOutgoingTruckResultToBeSent(wrapperSize, i, wrapperOutgoingIDsList);
    }

    private void getOutgoingTruckResultToBeSent(int wrapperSize,
                                                int i,
                                                List<List<String>> wrapperOutgoingIDsList) {

        Query query = firebaseFirestore.collectionGroup("OutgoingTruckResult")
                .whereIn("outgoingID", wrapperOutgoingIDsList.get(i))
                .whereEqualTo("sent", false);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    //Ovde dodajem sva dokuemnta u listu. Ova lista ce mi trebati kasnije kada treba da ih updatujem
                    //sent na true
                    getOutgoingTruckResultDocumentList().addAll(queryDocumentSnapshots.getDocuments());
                    //Dobijanje liste OurgoingDetailsResulta koji se nalaze na otpremama za slanje, a imaju sent na false
                    getOutgoingTruckResultList().addAll(queryDocumentSnapshots.toObjects(OutgoingTruckResult.class));
                    //Provera da li je ova inner lista poslednja u wrapper listi
                    if (wrapperSize - 1 == i) {
                        //znaci da je poslednja i ovde imam listu svih OutgoingDetailsResulta i OutgoingTruck resulta koji treba da se updatuju na sent = true
                        sendOutgoingAndDetailsAndTruckToServer();
                    } else {
                        int a = i + 1;
                        getOutgoingDetailsResultToBeSent(wrapperSize, a, wrapperOutgoingIDsList);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });
    }

    private void sendOutgoingAndDetailsAndTruckToServer() {

        List<OutgoingForServerWrapper> outgoingForServerWrapperModelList = new ArrayList<>();
        for (Outgoing outgoing : getOutgoingsToBeSentList()) {
            outgoingForServerWrapperModelList.add(
                    new OutgoingForServerWrapper(
                            outgoing.getOutgoingID(),
                            outgoing.getOutgoingStatusCode(),
                            getOutgoingDetailsResultList().stream()
                                    .filter(x -> x.getOutgoingID().equals(outgoing.getOutgoingID()))
                                    .collect(Collectors.toList()),
                            getOutgoingTruckResultList().stream()
                                    .filter(x -> x.getOutgoingID().equals(outgoing.getOutgoingID()))
                                    .collect(Collectors.toList())
                    )
            );
        }

        Call<GenericResponse<String>> call = ApiClient.getApiClient().create(Api.class).sendAllOutgoingDetailsResultToServer(outgoingForServerWrapperModelList);
        call.enqueue(new Callback<GenericResponse<String>>() {
            @Override
            public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                try {
                    if (Utility.checkResponseFromServer(response)) {

                        GenericResponse<String> genericResponse = response.body();
                        if (genericResponse.isSuccess()) {
                            //Poslato je na server i sada ide update na isSent na true
                            updateIsSentOnOutgoingDetailsResult();
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(genericResponse.getMessage()));
                        }

                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_all_outgoing, "")));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_all_outgoing, t.getMessage())));

            }
        });
    }

    private void updateIsSentOnOutgoingDetailsResult() {
        WriteBatch writeBatch = firebaseFirestore.batch();
        CollectionReference outgoings = firebaseFirestore.collection("outgoings");
        getOutgoingDetailsResultDocumentList().forEach(x -> writeBatch.update(x.getReference(), "sent", true));
        getOutgoingTruckResultDocumentList().forEach(x -> writeBatch.update(x.getReference(), "sent", true));
        getOutgoingIDToBeSentList().forEach(x -> writeBatch.update(outgoings.document(x), "finished", true));
        writeBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                OutgoingPhaseTwoActivityRepository.this.getResponseMutableLiveData()
                        .setValue(ApiResponse.successWithAction(resources.getString(R.string.outgoing_sent_successfully)));
                resetAllLists();
            }
        })
                .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
    }

    private void resetAllLists() {
        getOutgoingDetailsResultDocumentList().clear();
        getOutgoingDetailsResultList().clear();
        getOutgoingTruckResultDocumentList().clear();
        getOutgoingTruckResultList().clear();
        getOutgoingsToBeSentList().clear();
        getOutgoingIDToBeSentList().clear();
    }

}
