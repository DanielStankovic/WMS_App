package com.example.wms_app.repository.outgoing.phasetwo;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.model.OutgoingForServerWrapper;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.model.OutgoingTruckResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.model.WarehouseStatusPosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.model.WrapperOutgoingDetailsResult;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Partition;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingPhaseTwoRepository {

    private final Resources resources; //resursi za stringove

    private final FirebaseFirestore firebaseFirestore; //objekat za komunikaciju sa firebaseom

    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija

    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu

    private MutableLiveData<Outgoing> selectedOutgoingMutLiveData; //Objekat koji predstavlja trenutno odabranu otpremu

    private ListenerRegistration listenerRegistrationDetails; //Varijabla za realTimeFirebase osluskivanje detailsResulta

    private ListenerRegistration listenerRegistrationTruck; //Varijabla za realTimeFirebase osluskivanje za kamione

    private ListenerRegistration listenerRegistrationOutgoing; //Varijabla za realTimeFirebase osluskivanje za header otpreme

    private MutableLiveData<List<OutgoingDetailsResult>> outgoingDetailsResultFromFbMutLiveData; //Lista OutgoingDetailsResult-a na FB za trenutno odabranu otpremu

    private MutableLiveData<List<OutgoingTruckResult>> mOutgoingTruckResultMutLiveData; //Lista koja sadrzi kamione koji su na firebaseu za otpremu

    private MutableLiveData<ViewEnableHelper> viewEnableHelperMutableLiveData; //Objekat koji sluzi da se refreshuju ili promene viewovi na fragmentu

    private MutableLiveData<ProductBox> currentProductBox; //Trenutno selektovana kutija tj proizvod

    private MutableLiveData<ProductBox> scannedProductBoxMutLiveData; //Kutija koja je skenirana

    private MutableLiveData<WarehousePosition> currentWarehousePositionMutLiveData; //Objekat koji predstavlja trenutno odabranu poziciju

    private MutableLiveData<String> wPositionBarcodeMutLiveData; //String koji je postavlja u polje za skeniranu poziciju. Njime se kontrolise i dugme.

    private MutableLiveData<List<OutgoingDetailsResult>> mOutgoingDetailsResultMutLiveData; //Lista u koju se dodaju skenirani/dodati artikli

    private MutableLiveData<List<WarehouseStatusPositionDetails>> mWarehouseStatusPositionWithArticlesDetails; //Lista koja sadrzi artikle koji su na odredjenoj poziciji sa artiklima isfiltrirana prema trenutnoj otpremi

    private MutableLiveData<Boolean> isOutgoingFinishedMutableLiveData; //Vrednost koja oznacava da li je otprema zavrsena.

    private MutableLiveData<Map<String, List<WarehouseStatusPositionDetails>>> wrapperOutgoingDetResMutableLiveData; /*Mapa koja
                                                                                                            sadrzi barkod pozicije i detalje pozicije koja je skenirana, tako imam detalje
                                                                                                            pozicije i znam sta mogu da skidam odatle*/


    private final List<ListenerRegistration> outgoingGroupedDetailsListenerRegList = new ArrayList<>(); //Lista u koju se stavljaju sv osluskivaci za sve otpremnice kod grupne otpreme

    private MutableLiveData<HashMap<String, List<OutgoingDetailsResult>>> hashMapDetailsResultMutableLiveData; //Ovo je mapa koja cuva liste svih
    // OutgoingDetailsResulta koji stizu preko realtime osluskivaca sa firebase-a

    private MutableLiveData<EnumOutgoingStyle> enumOutgoingStyleMutableLiveData; //Ovaj objekat sluzi kako bih uvek imao informaciju da li se radi o pojedinacnoj ili grupnoj otpremi

    private MutableLiveData<List<Outgoing>> selectedOutgoingListMutLiveData; //Ovo je lista koja sadrzi sve otpreme koje su prosledjene za grupnu otpremu

    public OutgoingPhaseTwoRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //   apiReference = ApiClient.getApiClient().create(Api.class);
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
    }

    public MutableLiveData<Outgoing> getSelectedOutgoingMutLiveData() {
        if (selectedOutgoingMutLiveData == null)
            selectedOutgoingMutLiveData = new MutableLiveData<>();
        return selectedOutgoingMutLiveData;
    }

    public MutableLiveData<List<OutgoingDetailsResult>> getOutgoingDetailsResultFromFbMutLiveData() {
        if (outgoingDetailsResultFromFbMutLiveData == null) {
            outgoingDetailsResultFromFbMutLiveData = new MutableLiveData<>();
            List<OutgoingDetailsResult> list = new ArrayList<>();
            outgoingDetailsResultFromFbMutLiveData.setValue(list);
        }
        return outgoingDetailsResultFromFbMutLiveData;
    }

    public MutableLiveData<List<OutgoingTruckResult>> getOutgoingTruckResultMutLiveData() {
        if (mOutgoingTruckResultMutLiveData == null) {
            mOutgoingTruckResultMutLiveData = new MutableLiveData<>();
            List<OutgoingTruckResult> list = new ArrayList<>();
            mOutgoingTruckResultMutLiveData.setValue(list);
        }
        return mOutgoingTruckResultMutLiveData;
    }

    public MutableLiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        if (viewEnableHelperMutableLiveData == null)
            viewEnableHelperMutableLiveData = new MutableLiveData<>();
        return viewEnableHelperMutableLiveData;
    }

    public MutableLiveData<ProductBox> getCurrentProductBox() {
        if (currentProductBox == null)
            currentProductBox = new MutableLiveData<>();
        return currentProductBox;
    }

    public MutableLiveData<WarehousePosition> getCurrentWarehousePositionMutLiveData() {
        if (currentWarehousePositionMutLiveData == null)
            currentWarehousePositionMutLiveData = new MutableLiveData<>();
        return currentWarehousePositionMutLiveData;
    }

    public MutableLiveData<String> getPositionBarcodeMutLiveData() {
        if (wPositionBarcodeMutLiveData == null)
            wPositionBarcodeMutLiveData = new MutableLiveData<>();

        return wPositionBarcodeMutLiveData;
    }

    public MutableLiveData<List<WarehouseStatusPositionDetails>> getWarehouseStatusPositionWithArticlesDetails() {
        if (mWarehouseStatusPositionWithArticlesDetails == null)
            mWarehouseStatusPositionWithArticlesDetails = new MutableLiveData<>();
        return mWarehouseStatusPositionWithArticlesDetails;
    }

    public MutableLiveData<Map<String, List<WarehouseStatusPositionDetails>>> getWrapperOutgoingDetResMutableLiveData() {
        if (wrapperOutgoingDetResMutableLiveData == null) {
            wrapperOutgoingDetResMutableLiveData = new MutableLiveData<>();
            Map<String, List<WarehouseStatusPositionDetails>> map = new HashMap<>();
            wrapperOutgoingDetResMutableLiveData.setValue(map);
        }
        return wrapperOutgoingDetResMutableLiveData;
    }

    public MutableLiveData<List<OutgoingDetailsResult>> getOutgoingDetailsResultMutLiveData() {
        if (mOutgoingDetailsResultMutLiveData == null) {
            mOutgoingDetailsResultMutLiveData = new MutableLiveData<>();
            List<OutgoingDetailsResult> list = new ArrayList<>();
            mOutgoingDetailsResultMutLiveData.setValue(list);
        }
        return mOutgoingDetailsResultMutLiveData;
    }

    public MutableLiveData<ProductBox> getScannedProductBoxMutLiveData() {
        if (scannedProductBoxMutLiveData == null)
            scannedProductBoxMutLiveData = new MutableLiveData<>();
        return scannedProductBoxMutLiveData;
    }

    public MutableLiveData<Boolean> getIsOutgoingFinishedMutableLiveData() {
        if (isOutgoingFinishedMutableLiveData == null)
            isOutgoingFinishedMutableLiveData = new MutableLiveData<>();
        return isOutgoingFinishedMutableLiveData;
    }

    public MutableLiveData<HashMap<String, List<OutgoingDetailsResult>>> getHashMapDetailsResultMutableLiveData() {
        if (hashMapDetailsResultMutableLiveData == null) {
            hashMapDetailsResultMutableLiveData = new MutableLiveData<>();
            HashMap<String, List<OutgoingDetailsResult>> map = new HashMap<>();
            hashMapDetailsResultMutableLiveData.setValue(map);
        }
        return hashMapDetailsResultMutableLiveData;
    }

    public MutableLiveData<EnumOutgoingStyle> getEnumOutgoingStyleMutableLiveData() {
        if (enumOutgoingStyleMutableLiveData == null)
            enumOutgoingStyleMutableLiveData = new MutableLiveData<>();
        return enumOutgoingStyleMutableLiveData;
    }

    public MutableLiveData<List<Outgoing>> getSelectedOutgoingListMutLiveData() {
        if (selectedOutgoingListMutLiveData == null)
            selectedOutgoingListMutLiveData = new MutableLiveData<>();
        return selectedOutgoingListMutLiveData;
    }

    public void registerRealTimeUpdatesResultDetails() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        CollectionReference outgoingDetailsResultReference = firebaseFirestore
                .collection("outgoings")
                .document(this.selectedOutgoingMutLiveData.getValue().getOutgoingID())
                .collection("OutgoingDetailsResult");
        listenerRegistrationDetails = outgoingDetailsResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    List<OutgoingDetailsResult> outgoingDetailsResultList = value.toObjects(OutgoingDetailsResult.class);
                    getOutgoingDetailsResultFromFbMutLiveData().setValue(outgoingDetailsResultList);

                    if (isFirstSync) {
                        getResponseMutableLiveData().setValue(ApiResponse.success());
                        isFirstSync = false;
                    }

                    //Provera da li je nalog otkazan
                    if (!getSelectedOutgoingMutLiveData().getValue().getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED))) {
                        //Ovde se menja status otpremi
                        changeOutgoingStatus(outgoingDetailsResultList);
                    }
                }
            }
        });
    }

    public void registerRealTimeUpdatesTruckResult() {
        CollectionReference outgoingTruckResultReference = firebaseFirestore
                .collection("outgoings")
                .document(this.selectedOutgoingMutLiveData.getValue().getOutgoingID())
                .collection("OutgoingTruckResult");
        listenerRegistrationTruck = outgoingTruckResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    List<OutgoingTruckResult> outgoingTruckResultList = value.toObjects(OutgoingTruckResult.class);
                    getOutgoingTruckResultMutLiveData().setValue(outgoingTruckResultList);
                }
            }
        });
    }

    public void registerRealTimeUpdatesOutgoing(Outgoing currentOutgoing) {
        DocumentReference outgoingReference = firebaseFirestore.collection("outgoings").document(currentOutgoing.getOutgoingID());
        listenerRegistrationOutgoing = outgoingReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    if (value != null) {
                        if (value.getData() == null) {
                            OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.outgoing_deleted_error)));
                        } else {
                            Outgoing outgoing = value.toObject(Outgoing.class);
                            if (outgoing != null && outgoing.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED)) &&
                                    !(currentOutgoing.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED)))) {
                                getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.outgoing_canceled_error)));
                            }
                        }
                    }
                }
            }
        });
    }

    public void removeFirebaseRealTimeListener() {
        if (listenerRegistrationDetails != null)
            listenerRegistrationDetails.remove();

        if (listenerRegistrationTruck != null) {
            listenerRegistrationTruck.remove();
        }

        if (listenerRegistrationOutgoing != null) {
            listenerRegistrationOutgoing.remove();
        }

        for (ListenerRegistration listener : outgoingGroupedDetailsListenerRegList) {
            if (listener != null)
                listener.remove();
        }
    }

    /**
     * Metoda koja postavlja value za {ViewEnablerHelper}
     *
     * @param viewID       ID Viewa koji je potrebno enable/disable
     * @param viewText     Text koji se postavlja na View
     * @param isEnabled    Da li ga treba enable/disable
     * @param enumViewType Kog tipa je View, sluzi kao Flag za postavljanje teksta ako je
     *                     rec o EditTextu
     */
    public void toggleViewEnabledAndText(int viewID, String viewText, boolean isEnabled,
                                         EnumViewType enumViewType, int viewVisibility) {
        getViewEnableHelperLiveData().setValue(new ViewEnableHelper(
                viewID,
                viewText,
                isEnabled,
                enumViewType,
                viewVisibility));
    }

    public void getProductBoxesOnPosition(String wPositionBarcode, int employeeID) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference warehouseStatusPos = firebaseFirestore.collection("WarehouseStatusPos");
        Query query = warehouseStatusPos
                .whereEqualTo("warehousePositionBarcode", wPositionBarcode);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {

                        /*Provera da li je ova pozicija zakljucana. Ako je zakljucana od strane istog magacionera provera prolazi
                         * */
                        WarehouseStatusPosition warehouseStatusPosition = querySnapshot.toObjects(WarehouseStatusPosition.class)
                                .get(0);

                        if (!warehouseStatusPosition.isLocked() || warehouseStatusPosition.getLockedEmployeeID() == employeeID) {
                            getProductBoxesFromScannedPosition(wPositionBarcode, warehouseStatusPosition);
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_locked_error)));
                            resetProductSpinner();
                        }
                        // getProductBoxesFromScannedPosition(wPositionBarcode, warehouseStatusPosition);

                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_article_on_pos)));
                        resetProductSpinner();
                    }
                } else {
                    getResponseMutableLiveData().setValue(ApiResponse.error(task.getException().getMessage()));
                    resetProductSpinner();
                    Utility.writeErrorToFile(task.getException());
                }
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            resetProductSpinner();
            Utility.writeErrorToFile(e);
        });

    }

    public void resetProductSpinner() {
        //ovde se resetuje spiner. Ako se posalje prazna lista onda kroz metodu za mappiranje se ubaci placeholder
        getWarehouseStatusPositionWithArticlesDetails().setValue(new ArrayList<>());
    }

    private void getProductBoxesFromScannedPosition(String wPositionBarcode,
                                                    WarehouseStatusPosition warehouseStatusPosition) {

        try {
            List<OutgoingDetails> outgoingDetailsList = getSelectedOutgoingMutLiveData().getValue().getOutgoingDetails();
            //Ovde se dobija cela lista svih artikla koji su na poziciji i filtrira se na osnovu artikla koji su prosledjeni za otpremu
            List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList = warehouseStatusPosition
                    .getWspDetails()
                    .stream()
                    .filter(wsp -> outgoingDetailsList.stream()
                            .anyMatch(outDet -> wsp.getProductBoxID() == outDet.getProductBoxID()))
                    .filter(wsp -> wsp.getQuantity() > 0)
                    .collect(Collectors.toList());

            if (warehouseStatusPositionDetailsList.isEmpty()) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_article_on_pos_on_current_outgoing)));
                resetProductSpinner();
                return;
            }

        /*Postavljanje vrednosti za listu warehouseStatusPosition.
            Ova lista sadrzi artikle koji su na odabranoj poziciji i izfiltrirani prema artiklima koji su definisani na otpremi */
            getWarehouseStatusPositionWithArticlesDetails().setValue(warehouseStatusPositionDetailsList);

            putDetailsListIntoWrapperMap(wPositionBarcode, warehouseStatusPosition.getWspDetails());

            getResponseMutableLiveData().setValue(ApiResponse.success());
        } catch (Exception e) {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            Utility.writeErrorToFile(e);
        }

    }

    private void putDetailsListIntoWrapperMap(String wPositionBarcode, List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList) {

        Map<String, List<WarehouseStatusPositionDetails>> tempMap = getWrapperOutgoingDetResMutableLiveData().getValue();
        if (tempMap != null) {
            tempMap.put(wPositionBarcode, warehouseStatusPositionDetailsList);
            getWrapperOutgoingDetResMutableLiveData().setValue(tempMap);
        } else {
            getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_creating_wrapper_map)));
        }
    }

    public void resetPositionBarcode() {
        getPositionBarcodeMutLiveData().setValue("");
        getCurrentWarehousePositionMutLiveData().setValue(null);
        getWarehouseStatusPositionWithArticlesDetails().setValue(null);
    }

    private void changeOutgoingStatus(List<OutgoingDetailsResult> outgoingDetailsResultList) {

        int status = Constants.OUTGOING_STATUS_ACTIVE;
        boolean isFinished = false;

        if (!outgoingDetailsResultList.isEmpty()) {

            for (OutgoingDetails outgoingDetails : getSelectedOutgoingMutLiveData().getValue().getOutgoingDetails()) {
                int quantityOnFb = (int) outgoingDetailsResultList.stream()
                        .filter(x -> x.getProductBoxID() == outgoingDetails.getProductBoxID())
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();

                if (outgoingDetails.getQuantity() > quantityOnFb) {
                    status = Constants.OUTGOING_STATUS_FINISHED_PARTIALLY;
                    break;
                } else {
                    status = Constants.OUTGOING_STATUS_FINISHED_COMPLETELY;
                }
            }

            if (status == Constants.OUTGOING_STATUS_FINISHED_COMPLETELY)
                isFinished = true;

        }

        //TODO Ovde proveriti logiku za ovo. Proveriti da li ce da radi kako treba.
        boolean test = getIsOutgoingFinishedMutableLiveData().getValue() == null ? false : getIsOutgoingFinishedMutableLiveData().getValue();
        if (isFinished && test)
            isFinished = false;

        getIsOutgoingFinishedMutableLiveData().setValue(isFinished);
        String statusCode = Constants.OUTGOING_STATUS_MAP.get(status);

        if (!(getSelectedOutgoingMutLiveData().getValue().getOutgoingStatusCode().equals(statusCode))) {
            firebaseFirestore.collection("outgoings").document(getSelectedOutgoingMutLiveData().getValue().getOutgoingID())
                    .update("outgoingStatusCode", statusCode)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Outgoing outgoing = OutgoingPhaseTwoRepository.this.getSelectedOutgoingMutLiveData().getValue();
                            outgoing.setOutgoingStatusCode(statusCode);
                            getSelectedOutgoingMutLiveData().setValue(outgoing);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND)
                                return;
                            OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                        }
                    });
        }
    }

    private void changeGroupedOutgoingStatus(String outgoingID, List<OutgoingDetailsResult> outgoingDetailsResultList) {
        int status = Constants.OUTGOING_STATUS_ACTIVE;

        Optional<Outgoing> optionalOutgoing = getSelectedOutgoingListMutLiveData().getValue()
                .stream()
                .filter(x -> x.getOutgoingID().equals(outgoingID))
                .findAny();

        //Ovo je outgoing iz liste koja se dobija sa firebase-a. Posto ova lista nije u realnom vremenu, mora rucno
        //da se promeni status tom jednom nalgu u toj listi. To se radi zato sto moram da imam sa cime da
        //uporedm novi status koji se dobija. Ako se ne updatuje u lsiti, uvek ce biti razlicit od dobijenog
        //i update ce uvek da prodje. Kod prijema iz proizvodnej ova lista je realtime pa je logika drugacija,
        //dok je ovde i kod grupnog prijema od dobavljaca ista.
        Outgoing outgoing = optionalOutgoing.orElse(null);


        if (outgoing != null) {
            if (!outgoingDetailsResultList.isEmpty()) {
                for (OutgoingDetails outgoingDetails : getSelectedOutgoingMutLiveData().getValue().getOutgoingDetails()) {
                    int quantityOnFb = (int) outgoingDetailsResultList.stream()
                            .filter(x -> x.getProductBoxID() == outgoingDetails.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

                    if (outgoingDetails.getQuantity() > quantityOnFb) {
                        status = Constants.OUTGOING_STATUS_FINISHED_PARTIALLY;
                        break;
                    } else {
                        status = Constants.OUTGOING_STATUS_FINISHED_COMPLETELY;
                    }
                }
            }

            String statusCode = Constants.OUTGOING_STATUS_MAP.get(status);

            if (!(outgoing.getOutgoingStatusCode().equals(statusCode))) {
                firebaseFirestore.collection("outgoings").document(outgoing.getOutgoingID())
                        .update("outgoingStatusCode", statusCode)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getSelectedOutgoingListMutLiveData().getValue().stream()
                                        .filter(x -> x.getOutgoingID().equals(outgoingID))
                                        .findAny()
                                        .get()
                                        .setOutgoingStatusCode(statusCode);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND)
                                    return;
                                OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                            }
                        });
            }
        }

    }

    public void pushTempListToFirebase() {

        List<OutgoingDetailsResult> tempList = getOutgoingDetailsResultMutLiveData().getValue();

        //Provera da li je temp lista prazna
        if (tempList.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
            return;
        }

        getResponseMutableLiveData().setValue(ApiResponse.loading());

        String outgoingID = this.selectedOutgoingMutLiveData.getValue().getOutgoingID();

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        CollectionReference currentOutgoing = firebaseFirestore.collection("outgoings").document(outgoingID).collection("OutgoingDetailsResult");

        //Postavlja se sve iz tempListe u batch da bi se pushovalo kroz batch na firebase
        for (OutgoingDetailsResult odr : tempList) {
            odr.setOutgoingID(outgoingID);
            DocumentReference outDetResultDocRef = currentOutgoing.document();
            odr.setOdrFirebaseID(outDetResultDocRef.getId());

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).set(outDetResultDocRef, odr);
            operationCounter++;
        }

          /*Dobijanje liste UNIQUE Barkodova pozicija skeniranih artikla.
                Ovi IDijevi se koriste posle da se vidi sa koje pozicije da se skida sa firebase-a*/
        List<String> uniquePositionBarcodeFromTemp = tempList.stream()
                .map(OutgoingDetailsResult::getwPositionBarcode)
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<WarehouseStatusPositionDetails>> wrapperMap = getWrapperOutgoingDetResMutableLiveData().getValue();

        if (wrapperMap == null) {
            getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_wrapper_list_not_exist_leave)));
            return;
        }

        for (String posBarcode : uniquePositionBarcodeFromTemp) {
              /*Dobijanje pozicije koja je skenirana tj liste sa svim artiklima sa te pozicije. Ovo je pozicija koja nije za predutovar nego
                normalna pozicija sa artiklima. Ova pozicija je dodata u wrappermapu bez obzira da li je nesto skinuto sa nje ili nije posto mora da se sakljuca
                * */
            List<WarehouseStatusPositionDetails> positionDetailsList = wrapperMap.get(posBarcode);
            if (positionDetailsList == null) {
                getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_list_in_wrapper_not_exist_leave)));
                return;
            }
            /*Ovde se prolazi kroz sve artikle iz temp liste koji imaju position barcode isti kao barcode liste iz wrapper Mape koja se trenutno proverava*/
            for (OutgoingDetailsResult odr : tempList.stream().filter(x -> x.getwPositionBarcode().equals(posBarcode)).collect(Collectors.toList())) {

                if (odr.getSerialNo().equals("")) {
                    //Radi se o isSerialMustScan = false tj o malom artiklu

                    //Ovde sa pozicije sa artiklima se nalazi artikal koji je dodat u temp listu. Tako se zna posle koj artikal sa
                    //ove pozicije treba da se skine, tj da mu se menja kolicina
                    WarehouseStatusPositionDetails productOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                            .findAny().orElse(null);
                    if (productOnPos == null) {
                        //Ovo ne bi smelo da se desi zato sto ako je sa ove pozicije dodat u temp listu taj artikal mora
                        //da postoji na toj poziciji, inace ne bi mogao da bude dodat.
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_prod_from_temp_not_exist_on_pos_leave)));
                        return;
                    }

                    /*ovde se smanjuje kolicina iz liste sa artiklima na poziciji
                     * */
                    positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                            .forEach(x ->
                            {
                                int quantityToSet = x.getQuantity() - (int) odr.getQuantity();
                                x.setQuantity(quantityToSet);
                                x.setModifiedDate(new Date());
                                if (odr.isReserveQtyPromptAsked()) {
                                    x.setReservedQuantity(quantityToSet);
                                }

                            });

                } else {
                    //Radi se o isSerialMustScan = true tj o velikom artiklu

                    //Ovde sa pozicije sa artiklima se nalazi artikal koji je dodat u temp listu. Tako se zna posle koj artikal sa
                    //ove pozicije treba da se skine, tj da mu se menja kolicina
                    WarehouseStatusPositionDetails productOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                            .findAny().orElse(null);

                    if (productOnPos == null) {
                        //Ovo ne bi smelo da se desi zato sto ako je sa ove pozicije dodat u temp listu taj artikal mora
                        //da postoji na toj poziciji, inace ne bi mogao da bude dodat.
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_prod_from_temp_not_exist_on_pos_leave)));
                        return;
                    }

                    /*ovde se smanjuje kolicina iz liste sa artiklima na poziciji
                     * */
                    positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                            .forEach(x ->
                            {
                                int quantityToSet = x.getQuantity() - (int) odr.getQuantity();
                                x.setQuantity(quantityToSet);
                                x.setModifiedDate(new Date());
                                if (odr.isReserveQtyPromptAsked()) {
                                    x.setReservedQuantity(quantityToSet);
                                }

                            });
                }
            }

            //Update pozicija na firebaseu. Updateuju se norlamne pozicije tako sto im se postavljaju nove liste.
            DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(posBarcode);

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(documentReference, "wspDetails", positionDetailsList);
            operationCounter++;
        }


        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;
        sendBatches(batchListSize, batchSendCounter, batchList, false, R.string.product_loaded_successfully);


//        batch.commit().addOnSuccessListener(aVoid -> {
//            //Znaci da se sve upisalo na firebase uspesno. I OutgoingDetailsResult i skinule su se kolicine sa pozicija
//
//            resetListsAndViewsAfterSendingData(tempList, wrapperMap);
//            getResponseMutableLiveData().postValue(ApiResponse.successWithAction(resources.getString(R.string.product_loaded_successfully)));
//
//
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
    }

    private void sendBatches(int batchListSize, int batchSendCounter, List<WriteBatch> batchList, boolean shouldExitAfterSending, @StringRes int messageResourceID) {
        batchList.get(batchSendCounter).commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                //Provera da li je ovo poslednji batch
                if (batchListSize - 1 == batchSendCounter) {
                    //Znaci da je poslednji batch u listi
                    try {
                        resetListsAndViewsAfterSendingData();
                        if (shouldExitAfterSending)
                            getResponseMutableLiveData().setValue(ApiResponse.successWithExitAction(resources.getString(messageResourceID)));
                        else
                            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(messageResourceID)));
                    } catch (Exception e) {
                        getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                    }
                } else {

                    //Znaci da ima jos batcheva za slanje
                    int increasedBatchCounter = batchSendCounter + 1;
                    sendBatches(batchListSize, increasedBatchCounter, batchList, shouldExitAfterSending, messageResourceID);
                }
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    public void pushTempListToFirebaseGrouped() {

        List<OutgoingDetailsResult> tempList = getOutgoingDetailsResultMutLiveData().getValue();

        //Provera da li je temp lista prazna
        if (tempList.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
            return;
        }

        getResponseMutableLiveData().setValue(ApiResponse.loading());

         /* Ovde ide prebacivanje jedne liste u drugu posto treba da se cepaju artikli, tj da im se cepa kolicina. Od jednog velikog se prave 2 objekta sa manjom kolicinom
          zato ide prebacivanje temp liste u drugu listu. Ovo mozda i ne mora da se radi, ali da bi temp lista ostala kao sto jeste. U principu ako se sve posalje
          temp lista se prazni tako da je nebitno. Testirati
         * */
        List<OutgoingDetailsResult> splitTempList = new ArrayList<>(tempList);

        //Lista svih otprema koje su usle u grupnu otpremu. Ova lista je dobijena sa firebase-a.
        List<Outgoing> outgoingList = getSelectedOutgoingListMutLiveData().getValue();
        //Sortiranje liste otpremnica
        if (outgoingList != null) {
            outgoingList.sort(Comparator.comparing(Outgoing::getOutgoingDate).thenComparing(Outgoing::getOutgoingID));
        }

        //Ovo je lista objekata koji ce se pushovati na firebase. Napravljen je wrapper da bi se lakse
        //postavio outgoingID za svaki od outgoingDetailsResulta koji se pushuju.
        List<WrapperOutgoingDetailsResult> wrapperList = new ArrayList<>();

        //Mapa koja sadrzi sve outgoingDetailsResulte sa firebase-a. Ova mapa se automatski updatuje zbog realtimeListenera
        //Kljuc je outgoingID a vrednost je lista OutgoingDetailsResulta za odredjenu otpremu
        HashMap<String, List<OutgoingDetailsResult>> outgoingDetailsResultFromFb = getHashMapDetailsResultMutableLiveData().getValue();

        //Prolazak kroz sve dodate artikle u temp listi
        for (int i = 0; i < splitTempList.size(); i++) {
            //ID proizvoda koji je dodat u temp listu
            int productBoxIDInTemp = splitTempList.get(i).getProductBoxID();
            //kolicina objekata koji je dodat u temp listu
            int quantityOfCurrentObject = (int) splitTempList.get(i).getQuantity();

            //Dobijanje liste svih otprema koje u details sadrze ovaj proizvod
            List<Outgoing> filteredListOfOutgoing = outgoingList.stream()
                    .filter(x -> x.getOutgoingDetails().stream().anyMatch(y -> y.getProductBoxID() == productBoxIDInTemp))
                    .collect(Collectors.toList());

            if (filteredListOfOutgoing.isEmpty()) {
                //Znaci da se ovaj artikal ne nalazi ni na jednoj otpremi. Ovo ne bi smelo da se desi posto vec postoji
                //logika kod skeniranja i dodavanja. Ovo je smao dodatna provera.
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_such_prod_on_current_out)));
                return;
            }

            if (filteredListOfOutgoing.size() == 1) {
                //Znaci da postoji samo jedna ovakba otprema i odmah ide njoj cela kolicina
                wrapperList.add(new WrapperOutgoingDetailsResult(filteredListOfOutgoing.get(0).getOutgoingID(), splitTempList.get(i)));
            } else {
                //Prolazak kroz sve outgoinge koji su stigli sa firebase i provera da li se dodati artikal sadrzi. Sortirani su od najstarijeg ka najnovijem
                for (Outgoing outgoing : filteredListOfOutgoing) {

                    //Kolicina koja vec postoji na otpremi na firebase u OutgoingDetailsResult
                    int quantityOnCurrentOutOnFirebase = (int) outgoingDetailsResultFromFb.get(outgoing.getOutgoingID()).stream()
                            .filter(x -> x.getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

                    //Kolicina koja je vec dodata u wrapper listi
                    int quantityInWrapperList = (int) wrapperList.stream()
                            .filter(x -> x.getOutgoingID().equals(outgoing.getOutgoingID()) && x.getOutgoingDetailsResult().getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(x -> x.getOutgoingDetailsResult().getQuantity())
                            .sum();

                    // Ocekivana kolicina za artikal na otpremi. Od ove kolicine se oduzima ona koaj je vec dodata na firebase-u da bi se videlo realno koliko se ocekuje jos na ovom otpremi
                    int expectedQuantityOnCurrentOutgoing = (int) outgoing.getOutgoingDetails().stream().filter(x -> x.getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(OutgoingDetails::getQuantity).sum() - quantityOnCurrentOutOnFirebase - quantityInWrapperList;

                    //Provera da li je trenutna otprema poslednja u lsiti
                    if (filteredListOfOutgoing.indexOf(outgoing) == filteredListOfOutgoing.size() - 1) {
                        //Znaci da je poslednji u listi i onda mu se dodaje cela kolicina
                        wrapperList.add(new WrapperOutgoingDetailsResult(outgoing.getOutgoingID(), splitTempList.get(i)));
                        break;
                    }

                    //Ako je ocekivana kolicina 0 znaci da na ovom prijemu nema sta vise da se dadaje pa se prelazi na sledeci prijem
                    if (expectedQuantityOnCurrentOutgoing <= 0)
                        continue;

                    //Provera da li na firebase postoji ova otprema sa ovim idijem. Ovaj uslov bi uvek treba da prodje.
                    if (outgoingDetailsResultFromFb.get(outgoing.getOutgoingID()) == null) {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_such_out_in_hash)));
                        return;
                    }

                    if (quantityOfCurrentObject <= expectedQuantityOnCurrentOutgoing) {
                        //Znaci da je kolicina koja je dodata u trenutnom artiklu u temp isti plus kolicina koja postoji vec na firebaseu
                        // plus kolicina iz wrapper liste manja ili jednaka od one koja je na trenutnoj otpremi

                        wrapperList.add(new WrapperOutgoingDetailsResult(outgoing.getOutgoingID(), splitTempList.get(i)));
                        break;

                    } else {
                        //Znaci da je kolicina koja je dodata u trenutnom artiklu u temp isti plus kolicina koja postoji vec na firebaseu
                        // plus kolicina iz wrapper liste veca od one koja je na trenutnoj otpremi

                        //Znaci da nije poslednji u listi pa mora preracunavanje kolicina
                        //Posto treba da se dodata tacna kolicina ide ona koja je ocekivana
                        try {
                            //Klonirajne objekta
                            int originalOdrQty = (int) splitTempList.get(i).getQuantity();
                            OutgoingDetailsResult odrSplit = (OutgoingDetailsResult) splitTempList.get(i).clone();
                            odrSplit.setQuantity(originalOdrQty - expectedQuantityOnCurrentOutgoing);
                            //Dodavanje izdeljenog objekta u tempListu. On ce se sada naci na kraju o onda ce se obraditi kada do njega dodje iteracija
                            splitTempList.add(odrSplit);
                            //Postavlja se ocekivana kolicina kao kolicina objekta koji se dodaje
                            splitTempList.get(i).setQuantity(expectedQuantityOnCurrentOutgoing);
                            wrapperList.add(new WrapperOutgoingDetailsResult(outgoing.getOutgoingID(), splitTempList.get(i)));
                            break;
                        } catch (CloneNotSupportedException ex) {
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.clone_error)));
                            return;
                        }

                    }
                }
            }
        }

        //Postavljanje na firebase
        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        for (WrapperOutgoingDetailsResult wodr : wrapperList) {
            CollectionReference currentOutgoing = firebaseFirestore.collection("outgoings").document(wodr.getOutgoingID()).collection("OutgoingDetailsResult");
            wodr.getOutgoingDetailsResult().setOutgoingID(wodr.getOutgoingID());

            DocumentReference outDetResultDocRef = currentOutgoing.document();
            wodr.getOutgoingDetailsResult().setOdrFirebaseID(outDetResultDocRef.getId());

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).set(outDetResultDocRef, wodr.getOutgoingDetailsResult());
            operationCounter++;
        }

        ////////////////// DEO ZA SMANJIVANJE KOLICINE SA POZICIJA, tj UPDATE STANJA MAGACINA NA FB////////////////

          /*Dobijanje liste UNIQUE Barkodova pozicija skeniranih artikla.
                Ovi IDijevi se koriste posle da se vidi sa koje pozicije da se skida sa firebase-a*/
        List<String> uniquePositionBarcodeFromTemp = splitTempList.stream()
                .map(OutgoingDetailsResult::getwPositionBarcode)
                .distinct()
                .collect(Collectors.toList());

        //Ovo je mapa sa svim pozicijam akoje su skenirane tokom rada.
        Map<String, List<WarehouseStatusPositionDetails>> wrapperMap = getWrapperOutgoingDetResMutableLiveData().getValue();

        if (wrapperMap == null) {
            getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_wrapper_list_not_exist_leave)));
            return;
        }

        for (String posBarcode : uniquePositionBarcodeFromTemp) {
            Log.d("AAAA", "PositioBarcode = " + posBarcode);
              /*Dobijanje pozicije koja je skenirana tj liste sa svim artiklima sa te pozicije. Ovo je pozicija koja nije za predutovar nego
                normalna pozicija sa artiklima. Ova pozicija je dodata u wrappermapu bez obzira da li je nesto skinuto sa nje ili nije posto mora da se sakljuca
                * */
            List<WarehouseStatusPositionDetails> positionDetailsList = wrapperMap.get(posBarcode);
            if (positionDetailsList == null) {
                getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_list_in_wrapper_not_exist_leave)));
                return;
            }
            /*Ovde se prolazi kroz sve artikle iz temp liste koji imaju position barcode isti kao barcode liste iz wrapper Mape koja se trenutno proverava*/
            for (OutgoingDetailsResult odr : splitTempList.stream().filter(x -> x.getwPositionBarcode().equals(posBarcode)).collect(Collectors.toList())) {

                if (odr.getSerialNo().equals("")) {
                    //Radi se o isSerialMustScan = false tj o malom artiklu

                    //Ovde sa pozicije sa artiklima se nalazi artikal koji je dodat u temp listu. Tako se zna posle koj artikal sa
                    //ove pozicije treba da se skine, tj da mu se menja kolicina
                    WarehouseStatusPositionDetails productOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                            .findAny().orElse(null);
                    if (productOnPos == null) {
                        //Ovo ne bi smelo da se desi zato sto ako je sa ove pozicije dodat u temp listu taj artikal mora
                        //da postoji na toj poziciji, inace ne bi mogao da bude dodat.
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_prod_from_temp_not_exist_on_pos_leave)));
                        return;
                    }

                    /*ovde se smanjuje kolicina iz liste sa artiklima na poziciji
                     * */
                    positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                            .forEach(x ->
                            {
                                int quantityToSet = x.getQuantity() - (int) odr.getQuantity();
                                Log.d("AAAAAAAA", "quantityToSet = " + quantityToSet + "  GetQuantity = " + x.getQuantity() + " ODR.GetQUantity = " + odr.getQuantity());
                                x.setQuantity(quantityToSet);
                                x.setModifiedDate(new Date());
                                if (odr.isReserveQtyPromptAsked()) {
                                    x.setReservedQuantity(quantityToSet);
                                }

                            });

                } else {
                    //Radi se o isSerialMustScan = true tj o velikom artiklu

                    //Ovde sa pozicije sa artiklima se nalazi artikal koji je dodat u temp listu. Tako se zna posle koj artikal sa
                    //ove pozicije treba da se skine, tj da mu se menja kolicina
                    WarehouseStatusPositionDetails productOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                            .findAny().orElse(null);

                    if (productOnPos == null) {
                        //Ovo ne bi smelo da se desi zato sto ako je sa ove pozicije dodat u temp listu taj artikal mora
                        //da postoji na toj poziciji, inace ne bi mogao da bude dodat.
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_prod_from_temp_not_exist_on_pos_leave)));
                        return;
                    }

                    /*ovde se smanjuje kolicina iz liste sa artiklima na poziciji
                     * */
                    positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                            .forEach(x ->
                            {
                                int quantityToSet = x.getQuantity() - (int) odr.getQuantity();
                                x.setQuantity(quantityToSet);
                                x.setModifiedDate(new Date());
                                if (odr.isReserveQtyPromptAsked()) {
                                    x.setReservedQuantity(quantityToSet);
                                }

                            });
                }
            }

            //Update pozicija na firebaseu. Updateuju se norlamne pozicije tako sto im se postavljaju nove liste.
            DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(posBarcode);
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(documentReference, "wspDetails", positionDetailsList);
            operationCounter++;


        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;
        sendBatches(batchListSize, batchSendCounter, batchList, false, R.string.product_loaded_successfully);

    }

    private void resetListsAndViewsAfterSendingData() {

        /*Brisanje svega iz temp liste
         * */
        List<OutgoingDetailsResult> listToReset = getOutgoingDetailsResultMutLiveData().getValue();
        listToReset.clear();
        getOutgoingDetailsResultMutLiveData().setValue(listToReset);

        /*Brisanje svega iz wrapper mape
         * */
        Map<String, List<WarehouseStatusPositionDetails>> mapToReset = getWrapperOutgoingDetResMutableLiveData().getValue();
        mapToReset.clear();
        getWrapperOutgoingDetResMutableLiveData().setValue(mapToReset);

        //Brisanje teksta iz editteksta gde je uneta pozicija
        resetPositionBarcode();
    }

    public void sendOutgoingToServerAndFirebase() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        //Lista rezultata koji treba da se posalji na server. Ovde se filtrira lista koja je dosla sa firebase za ovaj prijem ali se uzima samo ono gde je isSent = false
        String outgoingID = getSelectedOutgoingMutLiveData().getValue().getOutgoingID();
        String outgoingStatusCode = getSelectedOutgoingMutLiveData().getValue().getOutgoingStatusCode();
        List<OutgoingDetailsResult> listToBeSentToServer = getOutgoingDetailsResultFromFbMutLiveData().getValue()
                .stream()
                .distinct()
                .filter(x -> !x.isSent())
                .collect(Collectors.toList());

        Set<OutgoingDetailsResult> outgoingDetailsResultListFiltered = listToBeSentToServer.stream()
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(OutgoingDetailsResult::getOutgoingID)
                                .thenComparing(OutgoingDetailsResult::getProductBoxID)
                                .thenComparing(OutgoingDetailsResult::getQuantity)
                                .thenComparing(OutgoingDetailsResult::getSerialNo)
                                .thenComparing(OutgoingDetailsResult::getwPositionBarcode)
                                .thenComparing(OutgoingDetailsResult::getEmployeeID)
                                .thenComparing(OutgoingDetailsResult::getCreateDate)
                                .thenComparing(OutgoingDetailsResult::isScanned)
                                .thenComparing(OutgoingDetailsResult::getOdrFirebaseID)
                        )));

        //Provera da li postoje uopste neke stavke koje nisu poslate
        if (listToBeSentToServer.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_out_to_be_sent)));
            return;
        }

        List<OutgoingTruckResult> listOfTrucksToBeSentToServer = getOutgoingTruckResultMutLiveData().getValue().stream().filter(x -> !x.isSent()).collect(Collectors.toList());
        OutgoingForServerWrapper outgoingForServerWrapper = new OutgoingForServerWrapper(
                outgoingID,
                outgoingStatusCode,
                listToBeSentToServer,
                listOfTrucksToBeSentToServer);
        Call<GenericResponse<String>> call = ApiClient.getApiClient().create(Api.class).sendOutgoingDetailsResultToServer(outgoingForServerWrapper);
        call.enqueue(new Callback<GenericResponse<String>>() {
            @Override
            public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                try {
                    if (Utility.checkResponseFromServer(response)) {
                        GenericResponse<String> genericResponse = response.body();
                        if (genericResponse.isSuccess()) {
                            //Poslato je na server i sada ide update isSent = true na firebaseu
                            updateIsSentOnOutgoingDetailsResult(outgoingDetailsResultListFiltered, listOfTrucksToBeSentToServer);
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(genericResponse.getMessage()));
                        }
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_single_outgoing, "")));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_single_outgoing, t.getMessage())));

            }
        });
    }

    private void updateIsSentOnOutgoingDetailsResult(Set<OutgoingDetailsResult> outgoingDetailsResultList,
                                                     List<OutgoingTruckResult> listOfTrucksToBeSentToServer) {

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        String outgoingID = this.selectedOutgoingMutLiveData.getValue().getOutgoingID();

        //Prvo se postavlja da je ceo prijem finished da bi se to prvo trigerovalo i da ne bi iskako dijalog 2 puta kada dodje do update-a detalja
        if (getIsOutgoingFinishedMutableLiveData().getValue()) {
            batchList.get(commitCounter).update(firebaseFirestore.collection("outgoings").document(outgoingID), "finished", true);
            operationCounter++;
        }


        CollectionReference outgoingDetailsResultReference = firebaseFirestore.collection("outgoings").document(outgoingID).collection("OutgoingDetailsResult");
        CollectionReference outgoingTruckResultReference = firebaseFirestore.collection("outgoings").document(outgoingID).collection("OutgoingTruckResult");

        for (OutgoingDetailsResult odr : outgoingDetailsResultList) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(outgoingDetailsResultReference.document(odr.getOdrFirebaseID()), "sent", true);
            operationCounter++;
        }

        for (OutgoingTruckResult otr : listOfTrucksToBeSentToServer) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(outgoingTruckResultReference.document(otr.getOtrFirebaseID()), "sent", true);
            operationCounter++;
        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;

        sendBatches(batchListSize, batchSendCounter, batchList, true, R.string.outgoing_sent_successfully);


//        Query query = outgoingDetailsResultReference.whereEqualTo("sent", false);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//
//                //Prvo se postavlja da je ceo prijem finished da bi se to prvo trigerovalo i da ne bi iskako dijalog 2 puta kada dodje do update-a detalja
//                if (getIsOutgoingFinishedMutableLiveData().getValue()) {
//                    writeBatch.update(firebaseFirestore.collection("outgoings").document(outgoingID), "finished", true);
//                }
//                queryDocumentSnapshots.getDocuments().forEach(x -> writeBatch.update(outgoingDetailsResultReference.document(x.getId()), "sent", true));
//                writeBatch.commit().addOnSuccessListener(aVoid -> updateIsSentOnOutgoingTruckResult())
//                        .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//
    }

//    private void updateIsSentOnOutgoingTruckResult() {
//        WriteBatch writeBatch = firebaseFirestore.batch();
//        String outgoingID = this.selectedOutgoingMutLiveData.getValue().getOutgoingID();
//        CollectionReference outgoingTruckResultReference = firebaseFirestore.collection("outgoings").document(outgoingID).collection("OutgoingTruckResult");
//        Query query = outgoingTruckResultReference.whereEqualTo("sent", false);
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//
//                queryDocumentSnapshots.getDocuments().forEach(x -> writeBatch.update(outgoingTruckResultReference.document(x.getId()), "sent", true));
//                writeBatch.commit().addOnSuccessListener(aVoid -> getResponseMutableLiveData().setValue(ApiResponse.successWithExitAction(resources.getString(R.string.outgoing_sent_successfully))))
//                        .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//    }

    public void insertTruckToFirebase(String truckDriver, String licencePlate, int employeeId) {
        if (getOutgoingTruckResultMutLiveData().getValue().stream().anyMatch(x -> x.getLicencePlate().equals(licencePlate))) {
            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.licence_plate_already_added)));
            return;
        }
        String outgoingID = this.selectedOutgoingMutLiveData.getValue().getOutgoingID();
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference currentOutgoing = firebaseFirestore.collection("outgoings").document(outgoingID).collection("OutgoingTruckResult");
        OutgoingTruckResult outgoingTruckResult = new OutgoingTruckResult(
                outgoingID,
                truckDriver,
                licencePlate,
                false,
                employeeId,
                new Date()
        );
        DocumentReference documentReference = currentOutgoing.document();
        outgoingTruckResult.setOtrFirebaseID(documentReference.getId());
        currentOutgoing.document().set(outgoingTruckResult).addOnSuccessListener(aVoid -> {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.truck_added)));
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    public void deleteTruckFromFirebase(OutgoingTruckResult outgoingTruckResult) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference outgoingTruckResultReference = firebaseFirestore
                .collection("outgoings")
                .document(this.selectedOutgoingMutLiveData.getValue().getOutgoingID())
                .collection("OutgoingTruckResult");
        Query query = outgoingTruckResultReference
                .whereEqualTo("truckDriver", outgoingTruckResult.getTruckDriver())
                .whereEqualTo("licencePlate", outgoingTruckResult.getLicencePlate())
                .whereEqualTo("employeeID", outgoingTruckResult.getEmployeeID());
        query.get().continueWith(snapshot -> outgoingTruckResultReference
                .document(snapshot.getResult().getDocuments().get(0).getId()).delete().addOnSuccessListener(aVoid -> getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.truck_removed))))
                .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()))));

    }

    public void deleteOutgoingDetailsResultFromFirebase(OutgoingDetailsResultPreview outgoingDetailsResultPreview) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference outgoingDetailsResultReference = firebaseFirestore
                .collection("outgoings")
                .document(outgoingDetailsResultPreview.getOutgoingID())
                .collection("OutgoingDetailsResult");
        Query query = outgoingDetailsResultReference
                .whereEqualTo("createDate", outgoingDetailsResultPreview.getCreatedDate())
                .whereEqualTo("productBoxID", outgoingDetailsResultPreview.getProductBoxID())
                .whereEqualTo("wPositionBarcode", outgoingDetailsResultPreview.getPositionBarcode());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                String documentIDToBeDeleted = queryDocumentSnapshots.getDocuments().get(0).getId();
                if (documentIDToBeDeleted.isEmpty()) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_fb)));
                    return;
                }
                deleteOutgoingDetailsResultAndUpdateWsp(outgoingDetailsResultReference, documentIDToBeDeleted, outgoingDetailsResultPreview);

            }
        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));


    }

    private void deleteOutgoingDetailsResultAndUpdateWsp(CollectionReference outgoingDetailsResultReference,
                                                         String documentIDToBeDeleted,
                                                         OutgoingDetailsResultPreview outgoingDetailsResultPreview) {
        WriteBatch batch = firebaseFirestore.batch();
        DocumentReference warehousePositionReference = firebaseFirestore
                .collection("WarehouseStatusPos")
                .document(outgoingDetailsResultPreview.getPositionBarcode());
        warehousePositionReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(task.getException().getMessage()));
                    Utility.writeErrorToFile(task.getException());
                    return;
                }
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_fb)));
                    return;
                }

                //Dobijanje pozicije na koju je postavljen ovaj artikal koji se vraca na stanje
                WarehouseStatusPosition warehouseStatusPosition = documentSnapshot.toObject(WarehouseStatusPosition.class);

                //Dobijanje liste artikala koji se nalaze na toj poziciji
                List<WarehouseStatusPositionDetails> positionDetailsList = warehouseStatusPosition.getWspDetails();

                if (outgoingDetailsResultPreview.getSerialNumber().equals("")) {
                    //Radi se o isSerialMustScan = false tj o malom artiklu
                    WarehouseStatusPositionDetails productBoxOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID() && x.getSerialNo() == null)
                            .findAny().orElse(null);
                    if (productBoxOnPos != null) {
                        /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                         * */
                        positionDetailsList.stream()
                                .filter(x -> x.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID() && x.getSerialNo() == null)
                                .forEach(x ->
                                {
                                    x.setQuantity(x.getQuantity() + (int) outgoingDetailsResultPreview.getQuantity());
                                    x.setModifiedDate(new Date());

                                });
                    } else {
                       /*Znaci da ga nema na toj poziciji. Ova situacija retko moze da se desi. Na primer, ako je dodat na firebase, i na
//                        pozciji ovde (pozicija sa artiklima) ostala je kolicina 0, pa je prosla konzolna i pobrisala sve gde je kolicina 0 onda
//                        ga on nece biti u listi*/
                        productBoxOnPos = new WarehouseStatusPositionDetails();
                        productBoxOnPos.setProductBoxID(outgoingDetailsResultPreview.getProductBoxID());
                        productBoxOnPos.setSerialNo(null);
                        productBoxOnPos.setModifiedDate(new Date());
                        productBoxOnPos.setQuantity((int) outgoingDetailsResultPreview.getQuantity());
                        productBoxOnPos.setReservedQuantity(0);
                        positionDetailsList.add(productBoxOnPos);
                    }
                } else {
                    //Radi se o isSerialMustScan = true tj o velikom artiklu

                    //Radi se o isSerialMustScan = true
                    WarehouseStatusPositionDetails productBoxOnPos = positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID() && x.getSerialNo().equals(outgoingDetailsResultPreview.getSerialNumber()))
                            .findAny().orElse(null);

                    if (productBoxOnPos != null) {

                        /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                         * */
                        positionDetailsList.stream()
                                .filter(x -> x.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID() && x.getSerialNo().equals(outgoingDetailsResultPreview.getSerialNumber()))
                                .forEach(x ->
                                {
                                    x.setQuantity(x.getQuantity() + (int) outgoingDetailsResultPreview.getQuantity());
                                    x.setModifiedDate(new Date());

                                });

                    } else {
                        /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
                         * */
                        productBoxOnPos = new WarehouseStatusPositionDetails();
                        productBoxOnPos.setProductBoxID(outgoingDetailsResultPreview.getProductBoxID());
                        productBoxOnPos.setSerialNo(outgoingDetailsResultPreview.getSerialNumber());
                        productBoxOnPos.setModifiedDate(new Date());
                        productBoxOnPos.setQuantity((int) outgoingDetailsResultPreview.getQuantity());
                        productBoxOnPos.setReservedQuantity(0);
                        positionDetailsList.add(productBoxOnPos);
                    }
                }

                //Update pozicija na firebaseu.
                DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(warehouseStatusPosition.getWarehousePositionBarcode());
                batch.update(documentReference, "wspDetails", positionDetailsList);
                batch.delete(outgoingDetailsResultReference.document(documentIDToBeDeleted));

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getResponseMutableLiveData().postValue(ApiResponse.successWithAction(resources.getString(R.string.product_deleted_successfully)));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                    }
                });
            }
        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
    }

    public void registerRealTimeUpdatesOutgoingDetailsSingle(Outgoing currentOutgoing) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;

        if (listenerRegistrationDetails != null)
            listenerRegistrationDetails.remove();

        CollectionReference outgoingDetailsResultReference = firebaseFirestore
                .collection("outgoings")
                .document(currentOutgoing.getOutgoingID())
                .collection("OutgoingDetailsResult");
        listenerRegistrationDetails = outgoingDetailsResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    if (Utility.isFirebaseSourceFromServer(value)) {
                        List<OutgoingDetailsResult> outgoingDetailsResultList = value.toObjects(OutgoingDetailsResult.class);
                        getOutgoingDetailsResultFromFbMutLiveData().setValue(outgoingDetailsResultList);

                        if (isFirstSync) {
                            getResponseMutableLiveData().setValue(ApiResponse.success());
                            isFirstSync = false;
                        }

                        //Provera da li je nalog otkazan
                        if (!getSelectedOutgoingMutLiveData().getValue().getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED))) {
                            //Ovde se menja status otpremi
                            changeOutgoingStatus(outgoingDetailsResultList);
                        }
                    }
                }
            }
        });
    }

    public void registerRealTimeUpdatesOutgoingTruckSingle(Outgoing currentOutgoing) {
        CollectionReference outgoingTruckResultReference = firebaseFirestore
                .collection("outgoings")
                .document(currentOutgoing.getOutgoingID())
                .collection("OutgoingTruckResult");
        listenerRegistrationTruck = outgoingTruckResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    List<OutgoingTruckResult> outgoingTruckResultList = value.toObjects(OutgoingTruckResult.class);
                    getOutgoingTruckResultMutLiveData().setValue(outgoingTruckResultList);
                }
            }
        });

    }

    public void registerRealTimeUpdatesOutgoingDetailsGrouped(OutgoingGrouped currentOutgoingGrouped) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        List<List<String>> wrapperUniqueOutgoingIDList = Partition.ofSize(currentOutgoingGrouped.getOutgoingIDList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
        int wrapperSize = wrapperUniqueOutgoingIDList.size();
        int i = 0;
        List<Outgoing> outgoingList = new ArrayList<>();
        getOutgoingFromFirebase(wrapperSize, i, wrapperUniqueOutgoingIDList, outgoingList);
    }

    private void getOutgoingFromFirebase(int wrapperSize, int i, List<List<String>> wrapperUniqueOutgoingIDList, List<Outgoing> outgoingList) {
        Query outgoings = firebaseFirestore.collection("outgoings")
                .whereIn("outgoingID", wrapperUniqueOutgoingIDList.get(i));
        outgoings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        outgoingList.addAll(querySnapshot.toObjects(Outgoing.class));
                        //Provera da li je ova inner lista poslednja u wrapper listi
                        if (wrapperSize - 1 == i) {
                            //znaci da je poslednja i ovde imam listu svih naloga koji su obuhvaceni u grupnom nalogu

                            //Postavljame liveData vrednosti za listu svih naloga. Potrebno mi je to posle za grupnu otpremu kada se
                            //salje na Firebase
                            getSelectedOutgoingListMutLiveData().setValue(outgoingList);

                            //Postavljanje realtime osluskivaca za svaki od naloga
                            setupRealtimeListenersForOutgoings(outgoingList);

                            if (isFirstSync) {
                                getResponseMutableLiveData().setValue(ApiResponse.success());
                                isFirstSync = false;
                            }

                        } else {
                            int a = i + 1;
                            getOutgoingFromFirebase(wrapperSize, a, wrapperUniqueOutgoingIDList, outgoingList);
                        }
                    }
                }
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    private void setupRealtimeListenersForOutgoings(List<Outgoing> outgoingList) {

        for (ListenerRegistration listener : outgoingGroupedDetailsListenerRegList) {
            if (listener != null)
                listener.remove();
        }

        for (Outgoing outgoing : outgoingList) {
            ListenerRegistration listenerRegistration = firebaseFirestore
                    .collection("outgoings").document(outgoing.getOutgoingID()).collection("OutgoingDetailsResult").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.outgoing_grouped_fb_error, outgoing.getOutgoingID(), error.getMessage())));
                                Utility.writeErrorToFile(error);
                            } else {
                                if (Utility.isFirebaseSourceFromServer(value)) {
                                    List<OutgoingDetailsResult> outgoingDetailsResultList = value.toObjects(OutgoingDetailsResult.class);
                                    HashMap<String, List<OutgoingDetailsResult>> hashMap = getHashMapDetailsResultMutableLiveData().getValue();
                                    if (hashMap.containsKey(outgoing.getOutgoingID())) {
                                        //Znaci da vec postoji lista pod ovim kljucem i ide samo izmena
                                        hashMap.replace(outgoing.getOutgoingID(), outgoingDetailsResultList);
                                    } else {
                                        //Znaci da ne postoji lista pod ovim kljucem i ide dodavanje
                                        hashMap.put(outgoing.getOutgoingID(), outgoingDetailsResultList);
                                    }
                                    getHashMapDetailsResultMutableLiveData().setValue(hashMap);

                                    changeGroupedOutgoingStatus(outgoing.getOutgoingID(), outgoingDetailsResultList);
                                }
                            }
                        }
                    });
            outgoingGroupedDetailsListenerRegList.add(listenerRegistration);

        }
    }

    public void getOutgoingListToBeSent() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        List<List<String>> wrapperUniqueOutgoingIDList = Partition.ofSize(getSelectedOutgoingMutLiveData().getValue().getOutgoingIDList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
        int wrapperSize = wrapperUniqueOutgoingIDList.size();
        int i = 0;
        List<Outgoing> outgoingList = new ArrayList<>();
        getOutgoingListToBeSentFromFirebase(wrapperSize, i, wrapperUniqueOutgoingIDList, outgoingList);
    }

    private void getOutgoingListToBeSentFromFirebase(int wrapperSize, int i,
                                                     List<List<String>> wrapperUniqueOutgoingIDList,
                                                     List<Outgoing> outgoingList) {
        Query outgoings = firebaseFirestore.collection("outgoings")
                .whereIn("outgoingID", wrapperUniqueOutgoingIDList.get(i));
        outgoings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        outgoingList.addAll(querySnapshot.toObjects(Outgoing.class));
                        //Provera da li je ova inner lista poslednja u wrapper listi
                        if (wrapperSize - 1 == i) {
                            //znaci da je poslednja i ovde imam listu svih naloga koji su obuhvaceni u grupnom nalogu

                            //Postavljanje realtime osluskivaca za svaki od naloga
                            sendOutgoingListToServer(outgoingList);

//                            if (isFirstSync) {
//                                getResponseMutableLiveData().setValue(ApiResponse.success());
//                                isFirstSync = false;
//                            }

                        } else {
                            int a = i + 1;
                            getOutgoingListToBeSentFromFirebase(wrapperSize, a, wrapperUniqueOutgoingIDList, outgoingList);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });

    }

    private void sendOutgoingListToServer(List<Outgoing> outgoingList) {

        Set<Outgoing> outgoingsToBeSent = outgoingList.stream()
                .filter(x -> (x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_FINISHED_COMPLETELY))
                        ||
                        x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_FINISHED_PARTIALLY)))
                        && !x.isFinished())
                .collect(Collectors.toSet());

        List<String> outgoingIDListToBeSent = outgoingsToBeSent.stream()
                .map(Outgoing::getOutgoingID)
                .distinct()
                .collect(Collectors.toList());

        //Provera da li uopste postoje neposlati nalozi koji su za slanje.
        if (outgoingIDListToBeSent.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_out_to_be_sent)));
            return;
        }


        //Dobijanje celokupne liste svih OutgoingDetailsResult-a koji su sent = false i koji pripadaju ovim otpremama
        List<OutgoingDetailsResult> outgoingDetailsResultList = outgoingIDListToBeSent.stream()
                .map(x -> getHashMapDetailsResultMutableLiveData().getValue().get(x))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(x -> !x.isSent())
                .collect(Collectors.toList());

        //Provera da li postoje uopste neke stavke koje nisu poslate
        if (outgoingDetailsResultList.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_out_to_be_sent)));
            return;
        }

        Set<OutgoingDetailsResult> outgoingDetailsResultListFiltered = outgoingDetailsResultList.stream()
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(OutgoingDetailsResult::getOutgoingID)
                                .thenComparing(OutgoingDetailsResult::getProductBoxID)
                                .thenComparing(OutgoingDetailsResult::getQuantity)
                                .thenComparing(OutgoingDetailsResult::getSerialNo)
                                .thenComparing(OutgoingDetailsResult::getwPositionBarcode)
                                .thenComparing(OutgoingDetailsResult::getEmployeeID)
                                .thenComparing(OutgoingDetailsResult::getCreateDate)
                                .thenComparing(OutgoingDetailsResult::isScanned)
                                .thenComparing(OutgoingDetailsResult::getOdrFirebaseID)
                        )));

        sendOutgoingsToServer(outgoingsToBeSent, outgoingDetailsResultListFiltered);
    }

    private void sendOutgoingsToServer(Set<Outgoing> outgoingsToBeSent,
                                       Set<OutgoingDetailsResult> outgoingDetailsResultList) {

        List<OutgoingForServerWrapper> outgoingForServerWrapperModelList = new ArrayList<>();
        for (Outgoing outgoing : outgoingsToBeSent) {
            outgoingForServerWrapperModelList.add(
                    new OutgoingForServerWrapper(
                            outgoing.getOutgoingID(),
                            outgoing.getOutgoingStatusCode(),
                            outgoingDetailsResultList.stream()
                                    .filter(x -> x.getOutgoingID().equals(outgoing.getOutgoingID()))
                                    .collect(Collectors.toList()),
                            new ArrayList<>()
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
                            updateIsSentOnOutgoingDetailsResultGrouped(outgoingsToBeSent, outgoingDetailsResultList);
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

    private void updateIsSentOnOutgoingDetailsResultGrouped(
            Set<Outgoing> outgoingToBeSent,
            Set<OutgoingDetailsResult> outgoingDetailsResultList) {

        //Ovde sada ide update rezltata na sent = true  za artikle
        //Posle ide update i za nalog na finished = true
        List<String> outgoingIDListToBeSent = outgoingToBeSent.stream()
                .map(Outgoing::getOutgoingID)
                .distinct()
                .collect(Collectors.toList());

        //Ovo je lista IDjeva svih tprema koji su zavrseni potpuno. Kod njih je potrebno
        //da se update finished = true nakon update-a svih OutgoingDetailsResult-a, zato se prosledjuje.
        List<String> finishedListOfOutgoingIDs = outgoingToBeSent.stream()
                .filter(x -> x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_FINISHED_COMPLETELY)))
                .map(Outgoing::getOutgoingID)
                .distinct()
                .collect(Collectors.toList());


        updateOutgoingDetailsResult(finishedListOfOutgoingIDs, outgoingDetailsResultList);

//        List<List<String>> wrapperOutgoingIDsList = Partition.ofSize(outgoingIDListToBeSent, Constants.FIRESTORE_IN_QUERY_LIMIT);
//        int wrapperSize = wrapperOutgoingIDsList.size();
//        int i = 0;
//        List<DocumentSnapshot> documentSnapshotList = new ArrayList<>();
//
//        getOutgoingDetailsResultToBeUpdated(finishedListOfOutgoingIDs, wrapperSize, i, wrapperOutgoingIDsList, documentSnapshotList);

    }

//    private void getOutgoingDetailsResultToBeUpdated(List<String> finishedListOfOutgoingIDs,
//                                                     int wrapperSize,
//                                                     int i,
//                                                     List<List<String>> wrapperOutgoingIDsList,
//                                                     List<DocumentSnapshot> documentSnapshotList) {
//
//        Query query = firebaseFirestore.collectionGroup("OutgoingDetailsResult")
//                .whereIn("outgoingID", wrapperOutgoingIDsList.get(i))
//                .whereEqualTo("sent", false);
//
//        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if (queryDocumentSnapshots != null) {
//                    documentSnapshotList.addAll(queryDocumentSnapshots.getDocuments());
//
//                    //Provera da li je ova inner lista poslednja u wrapper listi
//                    if (wrapperSize - 1 == i) {
//                        //znaci da je poslednja i ovde imam listu svih OutgoingDetailsResulta koji treba da se updatuju na sent = true
//                        updateOutgoingDetailsResult(finishedListOfOutgoingIDs, documentSnapshotList);
//                    } else {
//                        int a = i + 1;
//                        getOutgoingDetailsResultToBeUpdated(finishedListOfOutgoingIDs, wrapperSize, a, wrapperOutgoingIDsList, documentSnapshotList);
//                    }
//
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                OutgoingPhaseTwoRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//            }
//        });
//    }

    private void updateOutgoingDetailsResult(List<String> finishedListOfOutgoingIDs,
                                             Set<OutgoingDetailsResult> outgoingDetailsResultList) {

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        CollectionReference outgoings = firebaseFirestore.collection("outgoings");
        for (OutgoingDetailsResult odr : outgoingDetailsResultList) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(outgoings.document(odr.getOutgoingID()).collection("OutgoingDetailsResult").document(odr.getOdrFirebaseID()), "sent", true);
            operationCounter++;
        }

        for (String outgoingID : finishedListOfOutgoingIDs) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(outgoings.document(outgoingID), "finished", true);
            operationCounter++;
        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;

        sendBatches(batchListSize, batchSendCounter, batchList, true, R.string.outgoing_sent_successfully);

    }
}
