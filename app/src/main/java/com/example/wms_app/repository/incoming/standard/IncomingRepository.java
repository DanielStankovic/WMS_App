package com.example.wms_app.repository.incoming.standard;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
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
import com.example.wms_app.dao.WarehousePositionDao;
import com.example.wms_app.data.Api;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetails;
import com.example.wms_app.model.IncomingDetailsResult;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.model.IncomingForServerWrapper;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.IncomingTruckResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehouseStatusPosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.model.WrapperIncomingDetailsResult;
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingRepository {
    private Resources resources; //resursi za stringove
    // private Api apiReference; //Retrofit objekat za komunikaciju sa serverom
    private FirebaseFirestore firebaseFirestore; //objekat za komunikaciju sa firebaseom
    private WarehousePositionDao warehousePositionDao;
    private ListenerRegistration listenerRegistrationDetails; //Varijabla za realTimeFirebase osluskivanje
    private ListenerRegistration listenerRegistrationTruck; //Varijabla za realTimeFirebase osluskivanje za kamione
    private ListenerRegistration listenerRegistrationIncoming; //Varijabla za realTimeFirebase osluskivanje za header prijema
    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija artikla sa pozicije za predutovar
    private MutableLiveData<Incoming> selectedIncomingMutLiveData;
    private MutableLiveData<List<IncomingDetailsResult>> incomingDetailsResultFromFbMutLiveData;
    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu
    private MutableLiveData<List<IncomingDetailsResult>> mIncomingDetailsResultMutLiveData; //Lista u koju se dodaju skenirani/dodati artikli
    private MutableLiveData<List<IncomingTruckResult>> mIncomingTruckResultMutLiveData; //Lista koja sadrzi kamione koji su na firebaseu za prijem
    private MutableLiveData<ViewEnableHelper> viewEnableHelperMutableLiveData; //Objekat koji sluzi da se refreshuju ili promene viewovi na fragmentu
    private MutableLiveData<ProductBox> currentProductBox; //Trenutno selektovana kutija tj proizvod
    private MutableLiveData<ProductBox> scannedProductBoxMutLiveData; //Kutija koja je skenirana
    private MutableLiveData<Boolean> isIncomingFinishedMutableLiveData; //Vrednost koja oznacava da li je prijem zavrsen.
    private MutableLiveData<EnumIncomingStyle> enumIncomingStyleMutableLiveData;
    private MutableLiveData<List<Incoming>> selectedIncomingListMutLiveData; //Ovo je lista koja sadrzi sve prijeme koje su prosledjene za grupnu otpremu
    private List<ListenerRegistration> incomingDetailsListenerRegList = new ArrayList<>(); //Lista svih osluskivaca kod grupnog prijema
    private MutableLiveData<HashMap<String, List<IncomingDetailsResult>>> hashMapDetailsResultMutableLiveData; //Ovo je mapa koja cuva liste svih
    // IncomingDetailsResulta koji stizu preko realtime osluskivaca sa firebase-a

    public IncomingRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
        // apiReference = ApiClient.getApiClient().create(Api.class);
        warehousePositionDao = RoomDb.getDatabase(context).warehousePositionDao();
    }

    public MutableLiveData<Incoming> getSelectedIncomingMutLiveData() {
        if (selectedIncomingMutLiveData == null)
            selectedIncomingMutLiveData = new MutableLiveData<>();
        return selectedIncomingMutLiveData;
    }


    public MutableLiveData<EnumIncomingStyle> getEnumIncomingStyleMutableLiveData() {
        if (enumIncomingStyleMutableLiveData == null)
            enumIncomingStyleMutableLiveData = new MutableLiveData<>();
        return enumIncomingStyleMutableLiveData;
    }

    public MutableLiveData<List<IncomingDetailsResult>> getIncomingDetailsResultFromFbMutLiveData() {
        if (incomingDetailsResultFromFbMutLiveData == null) {
            incomingDetailsResultFromFbMutLiveData = new MutableLiveData<>();
            List<IncomingDetailsResult> list = new ArrayList<>();
            incomingDetailsResultFromFbMutLiveData.setValue(list);
        }
        return incomingDetailsResultFromFbMutLiveData;
    }

    public MutableLiveData<List<IncomingTruckResult>> getIncomingTruckResultMutLiveData() {
        if (mIncomingTruckResultMutLiveData == null) {
            mIncomingTruckResultMutLiveData = new MutableLiveData<>();
            List<IncomingTruckResult> list = new ArrayList<>();
            mIncomingTruckResultMutLiveData.setValue(list);
        }
        return mIncomingTruckResultMutLiveData;
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null) {
            responseMutableLiveData = new MutableLiveData<>();
            responseMutableLiveData.setValue(ApiResponse.idle());
        }
        return responseMutableLiveData;
    }

    public MutableLiveData<ProductBox> getScannedProductBoxMutLiveData() {
        if (scannedProductBoxMutLiveData == null)
            scannedProductBoxMutLiveData = new MutableLiveData<>();
        return scannedProductBoxMutLiveData;
    }

    public MutableLiveData<ProductBox> getCurrentProductBox() {
        if (currentProductBox == null)
            currentProductBox = new MutableLiveData<>();
        return currentProductBox;
    }

    public MutableLiveData<List<IncomingDetailsResult>> getIncomingDetailsResultMutLiveData() {
        if (mIncomingDetailsResultMutLiveData == null) {
            mIncomingDetailsResultMutLiveData = new MutableLiveData<>();
            List<IncomingDetailsResult> list = new ArrayList<>();
            mIncomingDetailsResultMutLiveData.setValue(list);
        }
        return mIncomingDetailsResultMutLiveData;
    }

    public MutableLiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        if (viewEnableHelperMutableLiveData == null)
            viewEnableHelperMutableLiveData = new MutableLiveData<>();
        return viewEnableHelperMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsIncomingFinishedMutableLiveData() {
        if (isIncomingFinishedMutableLiveData == null)
            isIncomingFinishedMutableLiveData = new MutableLiveData<>();
        return isIncomingFinishedMutableLiveData;
    }

    private MutableLiveData<List<Incoming>> getSelectedIncomingListMutLiveData() {
        if (selectedIncomingListMutLiveData == null)
            selectedIncomingListMutLiveData = new MutableLiveData<>();
        return selectedIncomingListMutLiveData;
    }

    public MutableLiveData<HashMap<String, List<IncomingDetailsResult>>> getHashMapDetailsResultMutableLiveData() {
        if (hashMapDetailsResultMutableLiveData == null) {
            hashMapDetailsResultMutableLiveData = new MutableLiveData<>();
            HashMap<String, List<IncomingDetailsResult>> map = new HashMap<>();
            hashMapDetailsResultMutableLiveData.setValue(map);
        }
        return hashMapDetailsResultMutableLiveData;
    }

    public void registerRealTimeUpdatesIncomingDetailsSingle(Incoming currentIncoming) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;

        if (listenerRegistrationDetails != null)
            listenerRegistrationDetails.remove();

        CollectionReference incomingDetailsResultReference = firebaseFirestore.collection("incomings").document(currentIncoming.getIncomingID()).collection("IncomingDetailsResult");
        listenerRegistrationDetails = incomingDetailsResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    if (Utility.isFirebaseSourceFromServer(value)) {
                        List<IncomingDetailsResult> incomingDetailsResultList = value.toObjects(IncomingDetailsResult.class);
                        getIncomingDetailsResultFromFbMutLiveData().setValue(incomingDetailsResultList);

                        if (isFirstSync) {
                            getResponseMutableLiveData().setValue(ApiResponse.success());
                            isFirstSync = false;
                        }

                        //Provera da li je nalog otkazan
                        if (!getSelectedIncomingMutLiveData().getValue().getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED))) {
                            //Ovde se menja status prijemu
                            changeIncomingStatus(incomingDetailsResultList);
                        }
                    }

                }
            }
        });

    }

    private void changeIncomingStatus(List<IncomingDetailsResult> incomingDetailsResultList) {


        int status = Constants.INCOMING_STATUS_ACTIVE;
        boolean isWithSurplus = false;
        boolean isFinished = false;

        if (!incomingDetailsResultList.isEmpty()) {

            for (IncomingDetails incomingDetails : getSelectedIncomingMutLiveData().getValue().getIncomingDetails()) {
                int quantityOnFb = (int) incomingDetailsResultList.stream()
                        .filter(x -> x.getProductBoxID() == incomingDetails.getProductBoxID())
                        .mapToDouble(IncomingDetailsResult::getQuantity)
                        .sum();

                if (incomingDetails.getQuantity() > quantityOnFb) {
                    status = Constants.INCOMING_STATUS_FINISHED_PARTIALLY;
                    break;
                } else {
                    if (quantityOnFb > incomingDetails.getQuantity()) {
                        isWithSurplus = true;
                    }
                    status = Constants.INCOMING_STATUS_FINISHED_COMPLETELY;
                }
            }

            if (status == Constants.INCOMING_STATUS_FINISHED_COMPLETELY && isWithSurplus)
                status = Constants.INCOMING_STATUS_FINISHED_WITH_SURPLUS;


            if (status == Constants.INCOMING_STATUS_FINISHED_COMPLETELY || status == Constants.INCOMING_STATUS_FINISHED_WITH_SURPLUS)
                isFinished = true;

        }

        //TODO Ovde proveriti logiku za ovo. Proveriti da li ce da radi kako treba.
        boolean test = getIsIncomingFinishedMutableLiveData().getValue() == null ? false : getIsIncomingFinishedMutableLiveData().getValue();
        if (isFinished && test)
            isFinished = false;

        getIsIncomingFinishedMutableLiveData().setValue(isFinished);
        String statusCode = Constants.STATUS_MAP.get(status);

        if (!(getSelectedIncomingMutLiveData().getValue().getIncomingStatusCode().equals(statusCode))) {
            firebaseFirestore.collection("incomings").document(getSelectedIncomingMutLiveData().getValue().getIncomingID())
                    .update("incomingStatusCode", statusCode)
                    .addOnSuccessListener(aVoid -> {
                        Incoming incoming = getSelectedIncomingMutLiveData().getValue();
                        incoming.setIncomingStatusCode(statusCode);
                        getSelectedIncomingMutLiveData().setValue(incoming);
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND)
                                return;
                            IncomingRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                        }
                    });

        }
    }

    private void changeIncomingStatusGrouped(String incomingID,
                                             List<IncomingDetailsResult> incomingDetailsResultList) {

        int status = Constants.INCOMING_STATUS_ACTIVE;
        boolean isWithSurplus = false;


        Optional<Incoming> optionalIncoming = getSelectedIncomingListMutLiveData().getValue().stream().filter(x -> x.getIncomingID().equals(incomingID)).findAny();

        //Ovo je incoming iz liste koja se dobija sa firebase-a. Posto ova lista nije u realnom vremenu, mora rucno
        //da se promeni status tom jednom nalgu u toj listi. To se radi zato sto moram da imam sa cime da
        //uporedm novi status koji se dobija. Ako se ne updatuje u lsiti, uvek ce biti razlicit od dobijenog
        //i update ce uvek da prodje. Kod prijema iz proizvodnej ova lista je realtime pa je logika drugacija.
        Incoming incoming = optionalIncoming.orElse(null);
        if (incoming != null) {
            if (!incomingDetailsResultList.isEmpty()) {

                for (IncomingDetails incomingDetails : incoming.getIncomingDetails()) {
                    int quantityOnFb = (int) incomingDetailsResultList.stream()
                            .filter(x -> x.getProductBoxID() == incomingDetails.getProductBoxID())
                            .mapToDouble(IncomingDetailsResult::getQuantity)
                            .sum();

                    if (incomingDetails.getQuantity() > quantityOnFb) {
                        status = Constants.INCOMING_STATUS_FINISHED_PARTIALLY;
                        break;
                    } else {
                        if (quantityOnFb > incomingDetails.getQuantity()) {
                            isWithSurplus = true;
                        }
                        status = Constants.INCOMING_STATUS_FINISHED_COMPLETELY;
                    }
                }

                if (status == Constants.INCOMING_STATUS_FINISHED_COMPLETELY && isWithSurplus)
                    status = Constants.INCOMING_STATUS_FINISHED_WITH_SURPLUS;

            }

            String statusCode = Constants.STATUS_MAP.get(status);
            if (!(incoming.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(status)))) {
                //TODO Srediti i proveriti logiku oko statusa
                firebaseFirestore.collection("incomings").document(incoming.getIncomingID())
                        .update("incomingStatusCode", Constants.STATUS_MAP.get(status))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getSelectedIncomingListMutLiveData().getValue().stream().filter(x -> x.getIncomingID()
                                        .equals(incomingID)).findAny().get().setIncomingStatusCode(statusCode);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND)
                                    return;
                                IncomingRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                            }
                        });
            }
        }
    }

    public void registerRealTimeUpdatesIncomingTruckSingle(Incoming currentIncoming) {
        CollectionReference incomingTruckResultReference = firebaseFirestore.collection("incomings").document(currentIncoming.getIncomingID()).collection("IncomingTruckResult");
        listenerRegistrationTruck = incomingTruckResultReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                    Utility.writeErrorToFile(error);
                } else {
                    List<IncomingTruckResult> incomingTruckResultList = value.toObjects(IncomingTruckResult.class);
                    getIncomingTruckResultMutLiveData().setValue(incomingTruckResultList);
                }
            }
        });

    }

    public void registerRealTimeUpdatesIncoming(Incoming currentIncoming) {
        DocumentReference incomingReference = firebaseFirestore.collection("incomings").document(currentIncoming.getIncomingID());
        listenerRegistrationIncoming = incomingReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                Utility.writeErrorToFile(error);
            } else {
                if (value != null) {
                    if (value.getData() == null) {
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.incoming_deleted_error)));
                    } else {
                        Incoming incoming = value.toObject(Incoming.class);

                        if (incoming != null &&
                                incoming.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED)) &&
                                !(currentIncoming.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED)))) {
                            getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.incoming_canceled_error)));
                        }
                    }
                }
            }
        });

    }

    public void removeFirebaseRealTimeListener() {
        if (listenerRegistrationDetails != null)
            listenerRegistrationDetails.remove();

        if (listenerRegistrationTruck != null)
            listenerRegistrationTruck.remove();

        if (listenerRegistrationIncoming != null)
            listenerRegistrationIncoming.remove();

        for (ListenerRegistration listener : incomingDetailsListenerRegList) {
            if (listener != null)
                listener.remove();
        }
    }


    public void pushTempListToFirebase(WarehouseStatusPosition warehouseStatusPosition, List<IncomingDetailsResult> tempList) {

        String incomingID = this.selectedIncomingMutLiveData.getValue().getIncomingID();

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        CollectionReference currentIncoming = firebaseFirestore.collection("incomings").document(incomingID).collection("IncomingDetailsResult");

        for (IncomingDetailsResult idr : tempList) {
            //Ide update positionID polja na svim artiklima u temp listi da bi se posle znalo na koju poziciju su postavljeni
            idr.setwPositionID(warehouseStatusPosition.getwPositionID());
            idr.setWarehousePositionBarcode(warehouseStatusPosition.getWarehousePositionBarcode());
            DocumentReference incDetResultDocRef = currentIncoming.document();
            idr.setIdrFirebaseID(incDetResultDocRef.getId());


            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).set(incDetResultDocRef, idr);
            operationCounter++;
        }

        ///////////////// DEO ZA POVECAVANJE KOLICINE NA POZICIJAMA, tj UPDATE STANJA MAGACINA NA FB///////////////
        List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList = getUpdatedWspDetails(warehouseStatusPosition.getWspDetails(), tempList);

        //Update pozicija na firebaseu.
        DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(warehouseStatusPosition.getWarehousePositionBarcode());
        batchList.get(commitCounter).update(documentReference, "wspDetails", warehouseStatusPositionDetailsList);

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;
        sendBatches(batchListSize, batchSendCounter, batchList, false, R.string.articles_added_on_pos);

//        // batch.update(firebaseFirestore.collection("incomings").document(incomingID), "incomingStatusCode", "03");
//
//        batch.commit().addOnSuccessListener(aVoid -> {
//            resetTempList();
//            //Resetovanje viewova za poziciju i checkboksa za rezervisano
        toggleViewEnabledAndText(R.id.incomingPosEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
        setPositionBarcodeByWarehouseName(this.selectedIncomingMutLiveData.getValue().getPartnerWarehouseName());
        toggleViewEnabledAndText(R.id.reservedCb, "", false, EnumViewType.CHECK_BOX, View.VISIBLE);
//            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.articles_added_on_pos)));
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });

    }

    private List<WarehouseStatusPositionDetails> getUpdatedWspDetails(List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList, List<IncomingDetailsResult> tempList) {
        for (IncomingDetailsResult idr : tempList) {

            //Radi se o isSerialMustScan = false
            if (idr.getSerialNo().equals("")) {

                WarehouseStatusPositionDetails productBoxOnPos = warehouseStatusPositionDetailsList
                        .stream()
                        .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo() == null)
                        .findAny().orElse(null);

                if (productBoxOnPos != null) {
                    /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                     * */
                    warehouseStatusPositionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo() == null)
                            .forEach(x ->
                            {
                                x.setQuantity(x.getQuantity() + (int) idr.getQuantity());
                                x.setModifiedDate(new Date());
                                if (idr.isReserved()) {
                                    x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
                                }

                            });

                } else {
                    /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
                     * */
                    productBoxOnPos = new WarehouseStatusPositionDetails();
                    productBoxOnPos.setProductBoxID(idr.getProductBoxID());
                    productBoxOnPos.setSerialNo(null);
                    productBoxOnPos.setModifiedDate(new Date());
                    productBoxOnPos.setQuantity((int) idr.getQuantity());
                    if (idr.isReserved()) {
                        productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
                    }
                    warehouseStatusPositionDetailsList.add(productBoxOnPos);
                }

            } else {
                //Radi se o isSerialMustScan = true
                WarehouseStatusPositionDetails productBoxOnPos = warehouseStatusPositionDetailsList
                        .stream()
                        .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo().equals(idr.getSerialNo()))
                        .findAny().orElse(null);

                if (productBoxOnPos == null) {

                    /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
                     * */
                    productBoxOnPos = new WarehouseStatusPositionDetails();
                    productBoxOnPos.setProductBoxID(idr.getProductBoxID());
                    productBoxOnPos.setSerialNo(idr.getSerialNo());
                    productBoxOnPos.setModifiedDate(new Date());
                    productBoxOnPos.setQuantity((int) idr.getQuantity());
                    if (idr.isReserved()) {
                        productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
                    }
                    warehouseStatusPositionDetailsList.add(productBoxOnPos);


                } else {
                    /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                     * */
                    warehouseStatusPositionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == idr.getProductBoxID() && x.getSerialNo().equals(idr.getSerialNo()))
                            .forEach(x ->
                            {
                                x.setQuantity(x.getQuantity() + (int) idr.getQuantity());
                                x.setModifiedDate(new Date());
                                if (idr.isReserved()) {
                                    x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
                                }

                            });

                }

            }

        }
        return warehouseStatusPositionDetailsList;
    }


    private void sendBatches(int batchListSize, int batchSendCounter, List<WriteBatch> batchList, boolean shouldExitAfterSending, @StringRes int messageResourceID) {
        batchList.get(batchSendCounter).commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                //Provera da li je ovo poslednji batch
                if (batchListSize - 1 == batchSendCounter) {
                    //Znaci da je poslednji batch u listi
                    try {

                        resetTempList();
                        //Resetovanje viewova za poziciju i checkboksa za rezervisano
                        toggleViewEnabledAndText(R.id.incomingPosEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        if (getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE)
                            setPositionBarcodeByWarehouseName(getSelectedIncomingMutLiveData().getValue().getPartnerWarehouseName());
                        else
                            setPositionBarcodeByWarehouseName(getSelectedIncomingListMutLiveData().getValue() == null ? "" : getSelectedIncomingListMutLiveData().getValue().get(0).getPartnerWarehouseName());
                        toggleViewEnabledAndText(R.id.reservedCb, "", false, EnumViewType.CHECK_BOX, View.VISIBLE);
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

    public void pushTempListToFirebaseGrouped(WarehouseStatusPosition warehouseStatusPosition, List<IncomingDetailsResult> tempList) {


        /* Ovde ide prebacivanje jedne liste u drugu posto treba da se cepaju artikli, tj da im se cepa kolicina. Od jednog velikog se prave 2 objekta sa manjom kolicinom
          zato ide prebacivanje temp liste u drugu listu. Ovo mozda i ne mora da se radi, ali da bi temp lista ostala kao sto jeste. U principu ako se sve posalje
          temp lista se prazni tako da je nebitno. Testirati
         * */
        List<IncomingDetailsResult> splitTempList = new ArrayList<>(tempList);
//        WriteBatch batch = firebaseFirestore.batch();
        List<WrapperIncomingDetailsResult> wrapperList = new ArrayList<>();
        List<Incoming> incomingList = getSelectedIncomingListMutLiveData().getValue();
        //Sortiranje liste svih prijema koji su za slanje. Sortira se da bi mogle lepo da se dele kolicine posle
        if (incomingList != null) {
            incomingList.sort(Comparator.comparing(Incoming::getIncomingDate).thenComparing(Incoming::getIncomingID));
        }
        HashMap<String, List<IncomingDetailsResult>> incomingDetailsResultFromFb = getHashMapDetailsResultMutableLiveData().getValue();
        //Prolazak kroz sve dodate artikle u temp listi
        for (int i = 0; i < splitTempList.size(); i++) {
            //ID proizvoda koji je dodat u temp listu
            int productBoxIDInTemp = splitTempList.get(i).getProductBoxID();
            //kolicina objekata koji je dodat u temp listu
            int quantityOfCurrentObject = (int) splitTempList.get(i).getQuantity();

            //Postavljanje pozicije na svaki idr
            splitTempList.get(i).setwPositionID(warehouseStatusPosition.getwPositionID());
            splitTempList.get(i).setWarehousePositionBarcode(warehouseStatusPosition.getWarehousePositionBarcode());


            //Dobijanje liste svih prijema koji u details sadrze ovaj proizvod
            List<Incoming> filteredListOfIncoming = incomingList.stream()
                    .filter(x -> x.getIncomingDetails().stream().anyMatch(y -> y.getProductBoxID() == productBoxIDInTemp))
                    .collect(Collectors.toList());


            if (filteredListOfIncoming.isEmpty()) {
                //Znaci da se ovaj artikal ne nalazi ni na jednom prijemu. Ovo ne bi smelo da se desi posto vec postoji
                //logika kod skeniranja i dodavanja. Ovo je smao dodatna provera.
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_such_prod_on_current_inc)));
                return;
            }

            if (filteredListOfIncoming.size() == 1) {
                //Znaci da postoji samo jedan ovakav prijem i odmah ide njemu cela kolicina
                wrapperList.add(new WrapperIncomingDetailsResult(filteredListOfIncoming.get(0).getIncomingID(), splitTempList.get(i)));
            } else {

                //Sortiranje liste svih prijema koji su za slanje. Sortira se da bi mogle lepo da se dele kolicine posle
                filteredListOfIncoming.sort(Comparator.comparing(Incoming::getIncomingDate).thenComparing(Incoming::getIncomingID));

                //Prolazak kroz sve incominge koji su stigli sa firebase i provera da li se dodati artikal sadrzi. Sortirani su od najstarijeg ka najnovijem
                for (Incoming incoming : filteredListOfIncoming) {

                    //Kolicina koja vec postoji na prijemu na firebase u IncomingDetailsResult
                    int quantityOnCurrentIncOnFirebase = (int) incomingDetailsResultFromFb.get(incoming.getIncomingID()).stream()
                            .filter(x -> x.getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(IncomingDetailsResult::getQuantity)
                            .sum();

                    //Kolicina koja je vec dodata u wrapper listi
                    int quantityInWrapperList = (int) wrapperList.stream()
                            .filter(x -> x.getIncomingID().equals(incoming.getIncomingID()) && x.getIncomingDetailsResult().getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(x -> x.getIncomingDetailsResult().getQuantity())
                            .sum();

                    // Ocekivana kolicina za artikal na prijemu. Od ove kolicine se oduzima ona koaj je vec dodata na firebase-u da bi se videlo realno koliko se ocekuje jos na ovom prijemu
                    int expectedQuantityOnCurrentIncoming = (int) incoming.getIncomingDetails().stream().filter(x -> x.getProductBoxID() == productBoxIDInTemp)
                            .mapToDouble(IncomingDetails::getQuantity).sum() - quantityOnCurrentIncOnFirebase - quantityInWrapperList;


                    //Provera da li je trenutni prijem poslednji u lsiti
                    if (filteredListOfIncoming.indexOf(incoming) == filteredListOfIncoming.size() - 1) {
                        //Znaci da je poslednji u listi i onda mu se dodaje cela kolicina
                        wrapperList.add(new WrapperIncomingDetailsResult(incoming.getIncomingID(), splitTempList.get(i)));
                        break;
                    }

                    //Ako je ocekivana kolicina 0 znaci da na ovom prijemu nema sta vise da se dadaje pa se prelazi na sledeci prijem
                    if (expectedQuantityOnCurrentIncoming <= 0)
                        continue;

                    //Stara provera probati sa novom da li ce sve da radi ok
//                    if(expectedQuantityOnCurrentIncoming == 0)
//                        continue;

                    //Provera da li na firebase postoji ovaj prijem sa ovim idijem. Ovaj uslov bi uvek treba da prodje.
                    if (incomingDetailsResultFromFb.get(incoming.getIncomingID()) == null) {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_such_inc_in_hash)));
                        return;
                    }


                    if (quantityOfCurrentObject <= expectedQuantityOnCurrentIncoming) {
                        //Znaci da je kolicina koja je dodata u trenutnom artiklu u temp isti plus kolicina koja postoji vec na firebaseu
                        // plus kolicina iz wrapper liste manja ili jednaka od one koja je na trenutnom prijemu

                        wrapperList.add(new WrapperIncomingDetailsResult(incoming.getIncomingID(), splitTempList.get(i)));
                        break;

                    } else {
                        //Znaci da je kolicina koja je dodata u trenutnom artiklu u temp isti plus kolicina koja postoji vec na firebaseu
                        // plus kolicina iz wrapper liste veca od one koja je na trenutnom prijemu

                        //Znaci da nije poslednji u listi pa mora preracunavanje kolicina
                        //Posto treba da se dodata tacna kolicina ide ona koja je ocekivana
                        try {
                            //Klonirajne objekta
                            int originalIdrQty = (int) splitTempList.get(i).getQuantity();
                            IncomingDetailsResult idrSplit = (IncomingDetailsResult) splitTempList.get(i).clone();
                            idrSplit.setQuantity(originalIdrQty - expectedQuantityOnCurrentIncoming);
                            //Dodavanje izdeljenog objekta u tempListu. On ce se sada naci na kraju o onda ce se obraditi kada do njega dodje iteracija
                            splitTempList.add(idrSplit);
                            //Postavlja se ocekivana kolicina kao kolicina objekta koji se dodaje
                            splitTempList.get(i).setQuantity(expectedQuantityOnCurrentIncoming);
                            wrapperList.add(new WrapperIncomingDetailsResult(incoming.getIncomingID(), splitTempList.get(i)));
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

        CollectionReference currentIncoming = firebaseFirestore.collection("incomings");

        for (WrapperIncomingDetailsResult widr : wrapperList) {
            widr.getIncomingDetailsResult().setIncomingId(widr.getIncomingID());
            DocumentReference incDetResultDocRef = currentIncoming.document(widr.getIncomingID()).collection("IncomingDetailsResult").document();
            widr.getIncomingDetailsResult().setIdrFirebaseID(incDetResultDocRef.getId());

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).set(incDetResultDocRef, widr.getIncomingDetailsResult());
            operationCounter++;

        }

        ///////////////// DEO ZA POVECAVANJE KOLICINE NA POZICIJAMA, tj UPDATE STANJA MAGACINA NA FB///////////////

        List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList = getUpdatedWspDetails(warehouseStatusPosition.getWspDetails(), tempList);

        //Update pozicija na firebaseu.
        DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(warehouseStatusPosition.getWarehousePositionBarcode());
        batchList.get(commitCounter).update(documentReference, "wspDetails", warehouseStatusPositionDetailsList);
        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;
        sendBatches(batchListSize, batchSendCounter, batchList, false, R.string.articles_added_on_pos);

    }

    private void resetTempList() {
        List<IncomingDetailsResult> listToReset = getIncomingDetailsResultMutLiveData().getValue();
        listToReset.clear();
        getIncomingDetailsResultMutLiveData().setValue(listToReset);
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

    public void toggleViewEnabledAndTextAsync(int viewID, String viewText, boolean isEnabled,
                                              EnumViewType enumViewType, int viewVisibility) {
        getViewEnableHelperLiveData().postValue(new ViewEnableHelper(
                viewID,
                viewText,
                isEnabled,
                enumViewType,
                viewVisibility));
    }


    public void insertTruckToFirebase(String truckDriver, String licencePlate, int employeeId) {
        if (getIncomingTruckResultMutLiveData().getValue().stream().anyMatch(x -> x.getLicencePlate().equals(licencePlate))) {
            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.licence_plate_already_added)));
            return;
        }
        String incomingID = this.selectedIncomingMutLiveData.getValue().getIncomingID();
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference currentIncoming = firebaseFirestore.collection("incomings").document(incomingID).collection("IncomingTruckResult");
        IncomingTruckResult incomingTruckResult = new IncomingTruckResult(
                incomingID,
                truckDriver,
                licencePlate,
                false,
                employeeId,
                new Date()
        );
        DocumentReference documentReference = currentIncoming.document();
        incomingTruckResult.setItrFirebaseID(documentReference.getId());
        documentReference.set(incomingTruckResult).addOnSuccessListener(aVoid -> {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.truck_added)));
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    public void deleteTruckFromFirebase(IncomingTruckResult incomingTruckResult) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference incomingTruckResultReference = firebaseFirestore
                .collection("incomings")
                .document(this.selectedIncomingMutLiveData.getValue().getIncomingID())
                .collection("IncomingTruckResult");
        Query query = incomingTruckResultReference
                .whereEqualTo("truckDriver", incomingTruckResult.getTruckDriver())
                .whereEqualTo("licencePlate", incomingTruckResult.getLicencePlate())
                .whereEqualTo("employeeID", incomingTruckResult.getEmployeeID());
        query.get().continueWith(snapshot -> incomingTruckResultReference
                .document(snapshot.getResult().getDocuments().get(0).getId()).delete().addOnSuccessListener(aVoid -> getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.truck_removed))))
                .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()))));

    }

    public void deleteIncomingDetailsResultFromFirebase(IncomingDetailsResultLocal incomingDetailsResultLocal) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference incomingDetailsResultReference = firebaseFirestore
                .collection("incomings")
                .document(incomingDetailsResultLocal.getIncomingID())
                .collection("IncomingDetailsResult");
        Query query = incomingDetailsResultReference
                .whereEqualTo("createDate", incomingDetailsResultLocal.getCreatedDate());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String documentIDToBeDeleted = queryDocumentSnapshots.getDocuments().get(0).getId();
                if (documentIDToBeDeleted.isEmpty()) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_fb)));
                    return;
                }
                deleteIncomingDetailsResultAndUpdateWsp(incomingDetailsResultReference, documentIDToBeDeleted, incomingDetailsResultLocal);
            }
        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));

    }

    private void deleteIncomingDetailsResultAndUpdateWsp(CollectionReference incomingDetailsResultReference,
                                                         String documentIDToBeDeleted,
                                                         IncomingDetailsResultLocal incomingDetailsResultLocal) {

        WriteBatch batch = firebaseFirestore.batch();
        DocumentReference warehousePositionReference = firebaseFirestore
                .collection("WarehouseStatusPos")
                .document(incomingDetailsResultLocal.getPositionBarcode());
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

                if (incomingDetailsResultLocal.getSerialNumber().equals("")) {
                    //Radi se o isSerialMustScan = false tj o malom artiklu
                    WarehouseStatusPositionDetails productBoxOnPos = positionDetailsList.stream()
                            .filter(x -> x.getProductBoxID() == incomingDetailsResultLocal.getProductBoxID() && x.getSerialNo() == null)
                            .findAny().orElse(null);
                    if (productBoxOnPos != null) {
                        /*Znaci da ga vec ima na poziciji i samo ide update kolicine
                         * */
                        positionDetailsList.stream()
                                .filter(x -> x.getProductBoxID() == incomingDetailsResultLocal.getProductBoxID() && x.getSerialNo() == null)
                                .forEach(x ->
                                {
                                    x.setQuantity(x.getQuantity() - (int) incomingDetailsResultLocal.getQuantity());
                                    x.setModifiedDate(new Date());

                                });
                    }
                } else {
                    //Radi se o isSerialMustScan = true tj o velikom artiklu

                    WarehouseStatusPositionDetails productBoxOnPos = positionDetailsList
                            .stream()
                            .filter(x -> x.getProductBoxID() == incomingDetailsResultLocal.getProductBoxID() && x.getSerialNo().equals(incomingDetailsResultLocal.getSerialNumber()))
                            .findAny().orElse(null);

                    if (productBoxOnPos != null) {

                        /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                         * */
                        positionDetailsList.stream()
                                .filter(x -> x.getProductBoxID() == incomingDetailsResultLocal.getProductBoxID() && x.getSerialNo().equals(incomingDetailsResultLocal.getSerialNumber()))
                                .forEach(x ->
                                {
                                    x.setQuantity(x.getQuantity() - (int) incomingDetailsResultLocal.getQuantity());
                                    x.setModifiedDate(new Date());

                                });

                    }
                }

                //Update pozicija na firebaseu.
                DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(warehouseStatusPosition.getWarehousePositionBarcode());
                batch.update(documentReference, "wspDetails", positionDetailsList);
                batch.delete(incomingDetailsResultReference.document(documentIDToBeDeleted));

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

    public void sendIncomingToServerAndFirebase() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        //Lista rezultata koji treba da se posalji na server. Ovde se filtrira lista koja je dosla sa firebase za ovaj prijem ali se uzima samo ono gde je isSent = false
        String incomingID = getSelectedIncomingMutLiveData().getValue().getIncomingID();
        String incomingStatusCode = getSelectedIncomingMutLiveData().getValue().getIncomingStatusCode();
        List<IncomingDetailsResult> listToBeSentToServer = getIncomingDetailsResultFromFbMutLiveData().getValue().stream()
                .filter(x -> !x.isSent())
                .distinct()
                .collect(Collectors.toList());

        //Provera da li postoje uopste neke stavke koje nisu poslate
        if (listToBeSentToServer.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_inc_to_be_sent)));
            return;
        }
        //Brisanje dupliakta iz liste

        Set<IncomingDetailsResult> incomingDetailsResultListFiltered = listToBeSentToServer.stream()
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(IncomingDetailsResult::getIncomingId)
                                .thenComparing(IncomingDetailsResult::getProductBoxID)
                                .thenComparing(IncomingDetailsResult::getQuantity)
                                .thenComparing(IncomingDetailsResult::getSerialNo)
                                .thenComparing(IncomingDetailsResult::getwPositionID)
                                .thenComparing(IncomingDetailsResult::getwSubPositionID)
                                .thenComparing(IncomingDetailsResult::getEmployeeID)
                                .thenComparing(IncomingDetailsResult::getCreateDate)
                                .thenComparing(IncomingDetailsResult::isScanned)
                                .thenComparing(IncomingDetailsResult::isReserved)

                        )));

        List<IncomingTruckResult> listOfTrucksToBeSentToServer = getIncomingTruckResultMutLiveData().getValue().stream().filter(x -> !x.isSent()).collect(Collectors.toList());


        IncomingForServerWrapper incomingForServerWrapper = new IncomingForServerWrapper(incomingID, incomingStatusCode, listToBeSentToServer, listOfTrucksToBeSentToServer);
        Call<GenericResponse<String>> call = ApiClient.getApiClient().create(Api.class).sendIncomingDetailsResultToServer(incomingForServerWrapper);
        call.enqueue(new Callback<GenericResponse<String>>() {
            @Override
            public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                try {
                    if (Utility.checkResponseFromServer(response)) {
                        GenericResponse<String> genericResponse = response.body();
                        if (genericResponse.isSuccess()) {
                            //Poslato je na server i sada ide update stanja magacina na firebaseu
                            updateIsSentOnIncomingDetailsResult(incomingDetailsResultListFiltered, listOfTrucksToBeSentToServer);
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(genericResponse.getMessage()));
                        }
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_single_incoming, "")));
                    }
                } catch (Exception ex) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(ex.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_sending_single_incoming, t.getMessage())));
            }
        });
    }

//    private void updateWarehouseStatusPosition(List<IncomingDetailsResult> incomingDetailsResultList) {
//        //Dobijanje liste distinct barkodova pozicija na koje su postavljeni svi artikli iz trenutnog prijema
//        List<String> uniquePositionBarcodes = incomingDetailsResultList.stream().map(IncomingDetailsResult::getWarehousePositionBarcode).distinct().collect(Collectors.toList());
//
//        List<List<String>> wrapperUniquePosBarcodeList = Partition.ofSize(uniquePositionBarcodes, Constants.FIRESTORE_IN_QUERY_LIMIT);
//        int wrapperSize = wrapperUniquePosBarcodeList.size();
//        int i = 0;
//        List<WarehouseStatusPosition> wspList = new ArrayList<>();
//
//        getWarehouseStatusPositionsFromFirebase(incomingDetailsResultList, wrapperSize, i, wrapperUniquePosBarcodeList, wspList);
//    }
//
//    private void getWarehouseStatusPositionsFromFirebase(List<IncomingDetailsResult> incomingDetailsResultList,
//                                                         int wrapperSize, int i,
//                                                         List<List<String>> wrapperUniquePosBarcodeList,
//                                                         List<WarehouseStatusPosition> wspList) {
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
//                        updateWarehousePositions(incomingDetailsResultList, wspList);
//                    } else {
//                        int a = i + 1;
//                        getWarehouseStatusPositionsFromFirebase(incomingDetailsResultList, wrapperSize, a, wrapperUniquePosBarcodeList, wspList);
//                    }
//                }
//            }
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//    }
//
//    private void updateWarehousePositions(List<IncomingDetailsResult> incomingDetailsResultList, List<WarehouseStatusPosition> wspList) {
//
//        WriteBatch writeBatch = firebaseFirestore.batch();
//        //Prolazak kroz sve pozicije
//        for (WarehouseStatusPosition wsp : wspList) {
//            //wsp details za odredjenu poziciju
//            List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList = wsp.getWspDetails();
//
//            //Prolazak kroz sve artikle koji se nalaze na IncomingDetailsResult ali imaju barkod pozicije kao ova sto je trenutno filtrirana
//            for (IncomingDetailsResult idr : incomingDetailsResultList.stream().filter(x -> x.getWarehousePositionBarcode().equals(wsp.getWarehousePositionBarcode())).collect(Collectors.toList())) {
//
//                 /*Posto u objektu OutgoingDetailsResult nemam isSerialMust scan polje,
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
//                                    if (idr.isReserved()) {
//                                        x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
//                                    }
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
//                        if (idr.isReserved()) {
//                            productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
//                        }
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
//                        if (idr.isReserved()) {
//                            productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
//                        }
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
//                                    if (idr.isReserved()) {
//                                        x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
//                                    }
//
//                                });
//
//                    }
//
//                }
//            }
//
//            //Update pozicija na firebaseu.
//            DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(wsp.getWarehousePositionBarcode());
//            writeBatch.update(documentReference, "wspDetails", warehouseStatusPositionDetailsList);
//        }
//
//        writeBatch.commit().addOnSuccessListener(aVoid -> {
//            //Ovde se sve poslalo na WarehouseStatusPos i updatovala se kolicina. Ovde onda sada ide update na stavkama na IncomingDetailsResult
//
//            updateIsSentOnIncomingDetailsResult();
//
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//
//    }

    private void updateIsSentOnIncomingDetailsResult(Set<IncomingDetailsResult> incomingDetailsResultList,
                                                     List<IncomingTruckResult> listOfTrucksToBeSentToServer) {
        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());


        String incomingID = this.selectedIncomingMutLiveData.getValue().getIncomingID();

        if (getIsIncomingFinishedMutableLiveData().getValue()) {
            batchList.get(commitCounter).update(firebaseFirestore.collection("incomings").document(incomingID), "finished", true);
            operationCounter++;
        }

        CollectionReference incomingDetailsResultReference = firebaseFirestore.collection("incomings").document(incomingID).collection("IncomingDetailsResult");
        CollectionReference incomingTruckResultReference = firebaseFirestore.collection("incomings").document(incomingID).collection("IncomingTruckResult");


        for (IncomingDetailsResult idr : incomingDetailsResultList) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomingDetailsResultReference.document(idr.getIdrFirebaseID()), "sent", true);
            operationCounter++;

        }

        for (IncomingTruckResult itr : listOfTrucksToBeSentToServer) {

            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomingTruckResultReference.document(itr.getItrFirebaseID()), "sent", true);
            operationCounter++;
        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;

        sendBatches(batchListSize, batchSendCounter, batchList, true, R.string.incoming_sent_successfully);
    }

//    private void updateIsSentOnIncomingTruckResult() {
//        WriteBatch writeBatch = firebaseFirestore.batch();
//        String incomingID = this.selectedIncomingMutLiveData.getValue().getIncomingID();
//        CollectionReference incomingTruckResultReference = firebaseFirestore.collection("incomings").document(incomingID).collection("IncomingTruckResult");
//        Query query = incomingTruckResultReference.whereEqualTo("sent", false);
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//
//                queryDocumentSnapshots.getDocuments().forEach(x -> writeBatch.update(incomingTruckResultReference.document(x.getId()), "sent", true));
//                writeBatch.commit().addOnSuccessListener(aVoid -> getResponseMutableLiveData().setValue(ApiResponse.successWithExitAction(resources.getString(R.string.incoming_sent_successfully))))
//                        .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//    }

    public void setPositionBarcodeByWarehouseName(String partnerWarehouseName) {
        AsyncTask.execute(() -> {
            String barcode = warehousePositionDao.getPositionBarcodeByWarehouseName(partnerWarehouseName);
            toggleViewEnabledAndTextAsync(R.id.incomingPosEt, barcode == null ? "" : barcode, true, EnumViewType.EDIT_TEXT, View.VISIBLE);
        });
    }

    public void registerRealTimeUpdatesIncomingDetailsGrouped(IncomingGrouped currentIncomingGrouped) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        List<List<String>> wrapperUniqueIncomingIDList = Partition.ofSize(currentIncomingGrouped.getIncomingIDList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
        int wrapperSize = wrapperUniqueIncomingIDList.size();
        int i = 0;
        List<Incoming> incomingList = new ArrayList<>();
        getIncomingFromFirebase(wrapperSize, i, wrapperUniqueIncomingIDList, incomingList);
    }

    private void getIncomingFromFirebase(int wrapperSize,
                                         int i,
                                         List<List<String>> wrapperUniqueIncomingIDList,
                                         List<Incoming> incomingList) {
        Query incomings = firebaseFirestore.collection("incomings")
                .whereIn("incomingID", wrapperUniqueIncomingIDList.get(i));
        incomings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        incomingList.addAll(querySnapshot.toObjects(Incoming.class));
                        //Provera da li je ova inner lista poslednja u wrapper listi
                        if (wrapperSize - 1 == i) {
                            //znaci da je poslednja i ovde imam listu svih naloga koji su obuhvaceni u grupnom nalogu

                            //Postavljame liveData vrednosti za listu svih naloga. Potrebno mi je to posle za grupni prijem kada se
                            //salje na Firebase
                            getSelectedIncomingListMutLiveData().setValue(incomingList);

                            //Postavljanje realtime osluskivaca za svaki od naloga
                            setupRealtimeListenersForIncomings(incomingList);

//                            if (isFirstSync) {
//
//                            }

                        } else {
                            int a = i + 1;
                            getIncomingFromFirebase(wrapperSize, a, wrapperUniqueIncomingIDList, incomingList);
                        }
                    }
                }
            }
        }).addOnFailureListener(e -> {
            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
        });
    }

    private void setupRealtimeListenersForIncomings(List<Incoming> incomingList) {

        for (ListenerRegistration listener : incomingDetailsListenerRegList) {
            if (listener != null)
                listener.remove();
        }

        //Ovde ubacujem prvo prazne liste za svaki nalog. Posto mi treba ova provera kasnije kada
        //iz temp lsite saljem na FB. Inace bi pucalo. Ovo je dodato zbog izmene unutar ove dole
        //Callback metode, gde je stavljena provera ako lista  sa firebase-a nije prazna
        //Prolazim prvo ovde kroz petlju da bih bio siguran da ce prov ovo da se izvesi a ne neki callback
        HashMap<String, List<IncomingDetailsResult>> initialHashmap = getHashMapDetailsResultMutableLiveData().getValue();
        incomingList.forEach(x -> {
            initialHashmap.put(x.getIncomingID(), new ArrayList<>());
        });

        //velicina liste. Korisitm je posle da bih mogao da ugasim loadingDijalogTek na poslednjem nalogu kroz loop
        int incomingListSize = incomingList.size();
        for (int i = 0; i < incomingListSize; i++) {

            //Mora ovako zbog lambde dole, nece da prihvati i iz loopa.
            int finalI = i;
            ListenerRegistration listenerRegistration = firebaseFirestore
                    .collection("incomings").document(incomingList.get(i).getIncomingID()).collection("IncomingDetailsResult").addSnapshotListener((value, error) -> {
                        if (error != null) {
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_production_fb_error, error.getMessage())));
                            Utility.writeErrorToFile(error);
                        } else {
                            if (Utility.isFirebaseSourceFromServer(value)) {

                                List<IncomingDetailsResult> incomingDetailsResultList = value.toObjects(IncomingDetailsResult.class);
                                HashMap<String, List<IncomingDetailsResult>> hashMap = getHashMapDetailsResultMutableLiveData().getValue();
//                                if (hashMap.containsKey(incomingList.get(finalI).getIncomingID())) {
//                                    //Znaci da vec postoji lista pod ovim kljucem i ide samo izmena
//                                    hashMap.replace(incomingList.get(finalI).getIncomingID(), incomingDetailsResultList);
//                                } else {
//                                    //Znaci da ne postoji lista pod ovim kljucem i ide dodavanje
//                                    hashMap.put(incomingList.get(finalI).getIncomingID(), incomingDetailsResultList);
//                                }


                                if (isFirstSync) {
                                    //Ako se radi o prvom ulasku u formu. Ide ova logika gde se proverava
                                    //da li je lsita prazna i ako nije ide update hashmape.
                                    if (!incomingDetailsResultList.isEmpty()) {
                                        String incomingID = incomingDetailsResultList.get(0).getIncomingId();
                                        hashMap.put(incomingID, incomingDetailsResultList);
                                        getHashMapDetailsResultMutableLiveData().setValue(hashMap);

                                        changeIncomingStatusGrouped(incomingID, incomingDetailsResultList);
                                    }

                                    if ((incomingListSize - 1 == finalI)) {
                                        setupInitialPositionBarcode(incomingList);
                                        getResponseMutableLiveData().setValue(ApiResponse.success());
                                        isFirstSync = false;
                                    }
                                } else {
                                    //Ovo je deo koji ce se trigerovati kada dodje do promene na firebase-u u IncomingDetailsResultu
                                    //Lista promena koja su se izrsila na dokumentu na firebase-u.
                                    List<DocumentChange> documentChangeTypeList = value.getDocumentChanges();
                                    if (!documentChangeTypeList.isEmpty()) {
                                        //MOZDA OVDE TREBA DA SE PRODJE KROZ LOOP I DA SE ODRADI CELA OVA LOGIKA DOLE
                                        //ZA SVAKI DOCUMENT CHANGE. MADA KOLKO VIDIM UVEK IMA SAMO JEDAN DOCUMENT CHANGE

                                        String incomingID = documentChangeTypeList.get(0).getDocument().getReference().getPath().split("/")[1];
                                        hashMap.put(incomingID, incomingDetailsResultList);
                                        getHashMapDetailsResultMutableLiveData().setValue(hashMap);

                                        changeIncomingStatusGrouped(incomingID, incomingDetailsResultList);

                                    } else {
                                        //Ovde nikada ne bi trebalo da udje ja mislim
                                        if (!incomingDetailsResultList.isEmpty()) {
                                            String incomingID = incomingDetailsResultList.get(0).getIncomingId();
                                            hashMap.put(incomingID, incomingDetailsResultList);
                                            getHashMapDetailsResultMutableLiveData().setValue(hashMap);

                                            changeIncomingStatusGrouped(incomingID, incomingDetailsResultList);
                                        }
                                    }

                                }


//
//                                if (isFirstSync && (incomingListSize - 1 == finalI)) {
//                                    setupInitialPositionBarcode(incomingList);
//                                    getResponseMutableLiveData().setValue(ApiResponse.success());
//                                    isFirstSync = false;
//                                }
                            }
                        }
                    });
            incomingDetailsListenerRegList.add(listenerRegistration);

        }
    }

    private void setupInitialPositionBarcode(List<Incoming> productionIncomings) {
        if (productionIncomings.isEmpty())
            return;
        setPositionBarcodeByWarehouseName(productionIncomings.get(0).getPartnerWarehouseName());
    }

    public void getIncomingListToBeSent() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        List<List<String>> wrapperUniqueIncomingIDList = Partition.ofSize(getSelectedIncomingMutLiveData().getValue().getIncomingIDList(), Constants.FIRESTORE_IN_QUERY_LIMIT);
        int wrapperSize = wrapperUniqueIncomingIDList.size();
        int i = 0;
        List<Incoming> incomingList = new ArrayList<>();
        getIncomingListTobeSentFromFirebase(wrapperSize, i, wrapperUniqueIncomingIDList, incomingList);
    }

    private void getIncomingListTobeSentFromFirebase(int wrapperSize, int i,
                                                     List<List<String>> wrapperUniqueIncomingIDList,
                                                     List<Incoming> incomingList) {
        Query outgoings = firebaseFirestore.collection("incomings")
                .whereIn("incomingID", wrapperUniqueIncomingIDList.get(i));
        outgoings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        incomingList.addAll(querySnapshot.toObjects(Incoming.class));
                        //Provera da li je ova inner lista poslednja u wrapper listi
                        if (wrapperSize - 1 == i) {
                            //znaci da je poslednja i ovde imam listu svih naloga koji su obuhvaceni u grupnom nalogu

                            //Postavljanje realtime osluskivaca za svaki od naloga
                            sendIncomingToServerAndFirebaseGrouped(incomingList);
                        } else {
                            int a = i + 1;
                            getIncomingListTobeSentFromFirebase(wrapperSize, a, wrapperUniqueIncomingIDList, incomingList);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                IncomingRepository.this.getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });

    }

    private void sendIncomingToServerAndFirebaseGrouped(List<Incoming> incomingList) {
        Set<Incoming> incomingToBeSent = incomingList.stream()
                .filter(x -> (x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_COMPLETELY))
                        ||
                        x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_PARTIALLY)))
                        && !x.isFinished())
                .collect(Collectors.toSet());

        List<String> incomingIDListToBeSent = incomingToBeSent.stream()
                .map(Incoming::getIncomingID)
                .distinct()
                .collect(Collectors.toList());

        //Provera da li uopste postoje neposlati nalozi koji su za slanje.
        if (incomingIDListToBeSent.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_inc_to_be_sent)));
            return;
        }

        HashMap<String, List<IncomingDetailsResult>> hashMapToBeQueried = getHashMapDetailsResultMutableLiveData().getValue();

        //Dobijanje celokupne liste svih IncomingDetailsResult-a koji su sent = false i koji pripadaju ovim prijemima
        List<IncomingDetailsResult> incomingDetailsResultList = incomingIDListToBeSent.stream()
                .map(hashMapToBeQueried::get)
                .flatMap(Collection::stream)
                .filter(x -> !x.isSent())
                .distinct()
                .collect(Collectors.toList());

        //Provera da li postoje uopste neke stavke koje nisu poslate
        if (incomingDetailsResultList.isEmpty()) {
            getResponseMutableLiveData().setValue(ApiResponse.successWithAction(resources.getString(R.string.no_inc_to_be_sent)));
            return;
        }

        //Brisanje dupliakta iz liste
        Set<IncomingDetailsResult> incomingDetailsResultListFiltered = incomingDetailsResultList.stream()
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(IncomingDetailsResult::getIncomingId)
                                .thenComparing(IncomingDetailsResult::getProductBoxID)
                                .thenComparing(IncomingDetailsResult::getQuantity)
                                .thenComparing(IncomingDetailsResult::getSerialNo)
                                .thenComparing(IncomingDetailsResult::getwPositionID)
                                .thenComparing(IncomingDetailsResult::getwSubPositionID)
                                .thenComparing(IncomingDetailsResult::getEmployeeID)
                                .thenComparing(IncomingDetailsResult::getCreateDate)
                                .thenComparing(IncomingDetailsResult::isScanned)
                                .thenComparing(IncomingDetailsResult::isReserved)

                        )));

        sendIncomingsToServerGrouped(incomingToBeSent, incomingDetailsResultListFiltered);

    }

    private void sendIncomingsToServerGrouped(Set<Incoming> incomingToBeSent,
                                              Set<IncomingDetailsResult> incomingDetailsResultList) {

        List<IncomingForServerWrapper> incomingForServerWrapperList = new ArrayList<>();
        for (Incoming incoming : incomingToBeSent) {
            incomingForServerWrapperList.add(
                    new IncomingForServerWrapper(
                            incoming.getIncomingID(),
                            incoming.getIncomingStatusCode(),
                            incomingDetailsResultList.stream()
                                    .filter(x -> x.getIncomingId().equals(incoming.getIncomingID()))
                                    .collect(Collectors.toList()),
                            new ArrayList<>()
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
                            updateIsSentOnIncomingDetailsResultGrouped(incomingToBeSent, incomingDetailsResultList);
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

//    private void updateWarehouseStatusPositionGrouped(List<IncomingForServerWrapper> incomingForServerWrapperList) {
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
//        getWarehouseStatusPositionsFromFirebaseGrouped(incomingForServerWrapperList, wrapperSize, i, wrapperUniquePosBarcodeList, wspList);
//    }
//
//    private void getWarehouseStatusPositionsFromFirebaseGrouped(List<IncomingForServerWrapper> wrapperIncomingDetailsResultList,
//                                                                int wrapperSize,
//                                                                int i,
//                                                                List<List<String>> wrapperUniquePosBarcodeList,
//                                                                List<WarehouseStatusPosition> wspList) {
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
//                        updateWarehousePositionsGrouped(wrapperIncomingDetailsResultList, wspList);
//                    } else {
//                        int a = i + 1;
//                        getWarehouseStatusPositionsFromFirebaseGrouped(wrapperIncomingDetailsResultList, wrapperSize, a, wrapperUniquePosBarcodeList, wspList);
//                    }
//                }
//            }
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//    }
//
//    private void updateWarehousePositionsGrouped(List<IncomingForServerWrapper> wrapperIncomingDetailsResultList,
//                                                 List<WarehouseStatusPosition> wspList) {
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
//                        TODO Proveriti da ovo ne puca
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
//                                    if (idr.isReserved()) {
//                                        x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
//                                    }
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
//                        if (idr.isReserved()) {
//                            productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
//                        }
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
//                        if (idr.isReserved()) {
//                            productBoxOnPos.setReservedQuantity((int) idr.getQuantity());
//                        }
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
//                                    if (idr.isReserved()) {
//                                        x.setReservedQuantity(x.getReservedQuantity() + (int) idr.getQuantity());
//                                    }
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
//            updateIsSentOnIncomingDetailsResultGrouped(wrapperIncomingDetailsResultList);
//
//        }).addOnFailureListener(e -> {
//            getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
//        });
//    }

    private void updateIsSentOnIncomingDetailsResultGrouped(Set<Incoming> incomingToBeSent,
                                                            Set<IncomingDetailsResult> incomingDetailsResultList) {

        //Ovde sada ide update rezltata na sent = true  za artikle
        //Posle ide update i za nalog na finished = true
        List<String> incomingIDListToBeSent = incomingToBeSent.stream()
                .map(Incoming::getIncomingID)
                .distinct()
                .collect(Collectors.toList());

        //Ovo je lista IDjeva svih prijema koji su zavrseni potpuno ili su zavrseni sa viskom. Kod njih je potrebno
        //da se update finished = true nakon update-a svih IncomingDetailsResult-a, zato se prosledjuje.
        List<String> finishedListOfIncomingIDs = incomingToBeSent.stream()
                .filter(x -> x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_COMPLETELY))
                        ||
                        x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_FINISHED_WITH_SURPLUS)))
                .map(Incoming::getIncomingID)
                .distinct()
                .collect(Collectors.toList());

        updateIncomingDetailsResult(finishedListOfIncomingIDs, incomingDetailsResultList);
//        List<List<String>> wrapperIncomingIDsList = Partition.ofSize(incomingIDListToBeSent, Constants.FIRESTORE_IN_QUERY_LIMIT);
//        int wrapperSize = wrapperIncomingIDsList.size();
//        int i = 0;
//        List<DocumentSnapshot> documentSnapshotList = new ArrayList<>();
//
//        getIncomingDetailsResultToBeUpdated(finishedListOfIncomingIDs, wrapperSize, i, wrapperIncomingIDsList, documentSnapshotList);


    }

//    private void getIncomingDetailsResultToBeUpdated(List<String> finishedListOfIncomingIDs, int wrapperSize, int i,
//                                                     List<List<String>> wrapperIncomingIDsList,
//                                                     List<DocumentSnapshot> documentSnapshotList) {
//
//        Query query = firebaseFirestore.collectionGroup("IncomingDetailsResult")
//                .whereIn("incomingId", wrapperIncomingIDsList.get(i))
//                .whereEqualTo("sent", false);
//
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (queryDocumentSnapshots != null) {
//                documentSnapshotList.addAll(queryDocumentSnapshots.getDocuments());
//
//                //Provera da li je ova inner lista poslednja u wrapper listi
//                if (wrapperSize - 1 == i) {
//                    //znaci da je poslednja i ovde imam listu svih IncomingDetailsResulta koji treba da se updatuju na sent = true
//                    updateIncomingDetailsResult(finishedListOfIncomingIDs, documentSnapshotList);
//                } else {
//                    int a = i + 1;
//                    getIncomingDetailsResultToBeUpdated(finishedListOfIncomingIDs, wrapperSize, a, wrapperIncomingIDsList, documentSnapshotList);
//                }
//
//            }
//        }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
//    }

    private void updateIncomingDetailsResult(List<String> finishedListOfIncomingIDs,
                                             Set<IncomingDetailsResult> incomingDetailsResultList) {

        //Postavljanje na firebase

        int operationCounter = 0;
        int commitCounter = 0;
        List<WriteBatch> batchList = new ArrayList<>();
        batchList.add(firebaseFirestore.batch());

        CollectionReference incomings = firebaseFirestore.collection("incomings");

        for (IncomingDetailsResult idr : incomingDetailsResultList) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomings.document(idr.getIncomingId()).collection("IncomingDetailsResult").document(idr.getIdrFirebaseID()), "sent", true);
            operationCounter++;

        }
        for (String incomingID : finishedListOfIncomingIDs) {
            if (operationCounter > Constants.WRITE_BATCH_LIMIT) {
                operationCounter = 0;
                commitCounter++;
                batchList.add(firebaseFirestore.batch());
            }
            batchList.get(commitCounter).update(incomings.document(incomingID), "finished", true);
            operationCounter++;
        }

        //Ovoliko batcheva ima za slanje
        int batchListSize = batchList.size();
        int batchSendCounter = 0;

        sendBatches(batchListSize, batchSendCounter, batchList, true, R.string.all_incoming_sent_successfully);

    }

    public void getWarehousePositionFromFirebase(List<IncomingDetailsResult> tempList, String barcode) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        DocumentReference warehousePositionReference = firebaseFirestore
                .collection("WarehouseStatusPos")
                .document(barcode);
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
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_getting_pos_from_fb)));
                    return;
                }

                //Dobijanje pozicije sa firebase-a koja je skenirana
                WarehouseStatusPosition warehouseStatusPosition = documentSnapshot.toObject(WarehouseStatusPosition.class);
                //Dobijanje liste artikala koji se nalaze na toj poziciji
                List<WarehouseStatusPositionDetails> positionDetailsList = warehouseStatusPosition.getWspDetails();
                if (positionDetailsList == null) {
                    getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_wsp_details_dont_exist)));
                    return;
                }
                if (getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE)
                    pushTempListToFirebase(warehouseStatusPosition, tempList);
                else
                    pushTempListToFirebaseGrouped(warehouseStatusPosition, tempList);
            }
        })
                .addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));
    }
}
