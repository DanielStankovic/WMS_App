package com.example.wms_app.repository.outgoing.phaseone;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.example.wms_app.R;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.model.WarehouseStatusPosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OutgoingPhaseOneRepository {

    private Resources resources; //resursi za stringove

    private FirebaseFirestore firebaseFirestore; //objekat za komunikaciju sa firebaseom

    private ListenerRegistration listenerRegistrationOutgoingSingle; //Varijabla za realTimeFirebase osluskivanje za header pojedinacne otpreme

    private ListenerRegistration listenerRegistrationPreloadingPos; //Varijabla za realTimeFirebase osluskivanje za pozicije koje su za predutovar

    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu

    private MutableLiveData<Outgoing> currentOutgoing;

    private MutableLiveData<List<OutgoingDetailsResult>> mOutgoingDetailsResultMutLiveData; //Lista u koju se dodaju skenirani/dodati artikli

    private MutableLiveData<List<ProductBox>> mFilteredProductListForSpinner; //Lista koja sadrzi distinct proizvode kojima se puni spinner

    private boolean isFirstSync; //Varijabla koja kontrolise da li je prva sinhronizacija artikla sa pozicije za predutovar

    private MutableLiveData<List<WarehouseStatusPosition>> warehouseStatusPositionMutableLiveData; //Lista svih pozicija koje su za predutovar

    private MutableLiveData<List<WarehouseStatusPositionDetails>> preloadingStatusPosDetailsFilteredMutLive; /* Objekat koji sadrzi listu
                                                                   artikla na pozicijama za predutovar grupisanih kolicina i filtrirano prema trenutnoj otpremi*/

    private MutableLiveData<List<WarehouseStatusPositionDetails>> preloadingStatusPosDetailsMutLive; /* Objekat koji sadrzi listu
                                                                   artikla na pozicijama za predutovar grupisanih kolicina ali nije filtrirano prema trenutnoj otpremi*/

    private MutableLiveData<WarehousePosition> currentWarehousePositionMutLiveData; //Objekat koji predstavlja trenutno odabranu poziciju

    private MutableLiveData<List<WarehouseStatusPositionDetails>> mWarehouseStatusPositionWithArticlesDetails; //Lista koja sadrzi artikle koji su na odredjenoj poziciji koja nije za predutovar

    private MutableLiveData<Map<String, List<WarehouseStatusPositionDetails>>> wrapperOutgoingDetResMutableLiveData; /*Mapa koja
                                                                                                            sadrzi ID pozicije i detalje pozicije koja je skenirana, tako imam detalje
                                                                                                            pozicije i znam sta mogu da skidam odatle*/

    private MutableLiveData<ViewEnableHelper> viewEnableHelperMutableLiveData; //Objekat koji sluzi da se refreshuju ili promene viewovi na fragmentu

    private MutableLiveData<ProductBox> scannedProductBoxMutLiveData; //Kutija koja je skenirana

    private MutableLiveData<ProductBox> currentProductBox; //Trenutno selektovana kutija tj proizvod

    private MutableLiveData<String> wPositionBarcodeMutLiveData; //String koji je postavlja u polje za skeniranu poziciju. Njime se kontrolise i dugme.


    public OutgoingPhaseOneRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
    }

    public MutableLiveData<List<OutgoingDetailsResult>> getOutgoingDetailsResultMutLiveData() {
        if (mOutgoingDetailsResultMutLiveData == null) {
            mOutgoingDetailsResultMutLiveData = new MutableLiveData<>();
            List<OutgoingDetailsResult> list = new ArrayList<>();
            mOutgoingDetailsResultMutLiveData.setValue(list);
        }
        return mOutgoingDetailsResultMutLiveData;
    }

    public MutableLiveData<Outgoing> getCurrentOutgoing() {
        if (currentOutgoing == null)
            currentOutgoing = new MutableLiveData<>();
        return currentOutgoing;
    }


    public MutableLiveData<List<WarehouseStatusPosition>> getWarehouseStatusPositionMutableLiveData() {
        if (warehouseStatusPositionMutableLiveData == null)
            warehouseStatusPositionMutableLiveData = new MutableLiveData<>();
        return warehouseStatusPositionMutableLiveData;
    }

    public MutableLiveData<List<WarehouseStatusPositionDetails>> getPreloadingStatusPosDetailsFilteredMutLive() {
        if (preloadingStatusPosDetailsFilteredMutLive == null)
            preloadingStatusPosDetailsFilteredMutLive = new MutableLiveData<>();
        return preloadingStatusPosDetailsFilteredMutLive;
    }

    public MutableLiveData<List<WarehouseStatusPositionDetails>> getPreloadingStatusPosDetailsMutLive() {
        if (preloadingStatusPosDetailsMutLive == null)
            preloadingStatusPosDetailsMutLive = new MutableLiveData<>();
        return preloadingStatusPosDetailsMutLive;
    }

    public MutableLiveData<WarehousePosition> getCurrentWarehousePositionMutLiveData() {
        if (currentWarehousePositionMutLiveData == null)
            currentWarehousePositionMutLiveData = new MutableLiveData<>();
        return currentWarehousePositionMutLiveData;
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

    public MutableLiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        if (viewEnableHelperMutableLiveData == null)
            viewEnableHelperMutableLiveData = new MutableLiveData<>();
        return viewEnableHelperMutableLiveData;
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

    public MutableLiveData<String> getPositionBarcodeMutLiveData() {
        if (wPositionBarcodeMutLiveData == null)
            wPositionBarcodeMutLiveData = new MutableLiveData<>();

        return wPositionBarcodeMutLiveData;
    }
//    public MutableLiveData<List<ProductBox>> getFilteredProductListForSpinner() {
////        if (mFilteredProductListForSpinner == null) {
////            mFilteredProductListForSpinner = new MutableLiveData<>();
////            List<ProductBox> productBoxList = new ArrayList<>();
////            productBoxList.add(ProductBox.newPlaceHolderInstance());
////            mFilteredProductListForSpinner.setValue(productBoxList);
////        }
////        return mFilteredProductListForSpinner;
//
////            if(mFilteredProductListForSpinner == null) {
////                mFilteredProductListForSpinner = AsyncMapper.getProductBoxForSpinnerPhaseOne(
////                        getWarehouseStatusPositionWithArticlesDetails(),
////                        getOutgoingDetailsResultMutLiveData(),
////                        getPreloadingStatusPosDetailsFilteredMutLive(),
////                        getWarehouseStatusPositionWithArticlesDetails().getValue(),
////                        getOutgoingDetailsResultMutLiveData().getValue(),
////                        getPreloadingStatusPosDetailsFilteredMutLive().getValue(),
////                        getCurrentOutgoing().getValue().getOutgoingDetails(),
////                        mContext
////                );
//
//        if (mFilteredProductListForSpinner == null) {
//
//
//            mFilteredProductListForSpinner = AsyncMapper.getProductBoxForSpinnerPhaseOne(
//                    getWarehouseStatusPositionWithArticlesDetails(),
//                    getOutgoingDetailsResultMutLiveData(),
//                    getPreloadingStatusPosDetailsFilteredMutLive(),
//                    this::getProductBoxFromWPosDetails,
//                    this::changeColorAndQuantities
//            );
//
//            List<ProductBox> productBoxList = new ArrayList<>();
//            productBoxList.add(ProductBox.newPlaceHolderInstance());
//            mFilteredProductListForSpinner.setValue(productBoxList);
//        }
//        return mFilteredProductListForSpinner;
//    }


    public void registerRealTimeUpdatesOutgoingSingle(Outgoing outgoing) {
        DocumentReference currentOutgoingDoc = firebaseFirestore.collection("outgoings").document(outgoing.getOutgoingID());
        listenerRegistrationOutgoingSingle = currentOutgoingDoc.addSnapshotListener((value, error) -> {
            if (error != null) {
                getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                Utility.writeErrorToFile(error);
            } else {
                if (value != null) {
                    if (value.getData() == null) {
                        getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.outgoing_deleted_error)));
                    }
                }
            }
        });
    }

    public void removeFirebaseRealTimeListener() {
        if (listenerRegistrationOutgoingSingle != null)
            listenerRegistrationOutgoingSingle.remove();

        if (listenerRegistrationPreloadingPos != null)
            listenerRegistrationPreloadingPos.remove();
    }

    public void registerRealTimeUpdatesPreloadingPos() {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        isFirstSync = true;
        CollectionReference warehouseStatusPos = firebaseFirestore.collection("WarehouseStatusPos");
        Query query = warehouseStatusPos
                .whereEqualTo("forPreloading", true);

        listenerRegistrationPreloadingPos = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                Utility.writeErrorToFile(error);
            } else {
                if (Utility.isFirebaseSourceFromServer(value)) {
                    //Ovde se dobija cela lista svih pozicija za predutovar
                    List<WarehouseStatusPosition> warehouseStatusPositionList = value.toObjects(WarehouseStatusPosition.class);


                    /* Dobijanje liste artikala koji se nalaze na poziciji za predutovar sa grupisanim kolicinama
                     * */

                    List<WarehouseStatusPositionDetails> groupedListOfAllArticles = warehouseStatusPositionList.stream()
                            .flatMap(wspl -> wspl.getWspDetails()
                                    .stream())
                            .collect(Collectors.groupingBy(
                                    WarehouseStatusPositionDetails::getProductBoxID,
                                    Collectors.summarizingInt(WarehouseStatusPositionDetails::getQuantity)
                            ))
                            .entrySet()
                            .stream()
                            .map(k -> new WarehouseStatusPositionDetails(k.getKey(), (int) k.getValue().getSum()))
                            .collect(Collectors.toList());

                    //Ovde je lista sa svim artiklima sa grupisanim kolicinama na pozicijama za predutovar, filtrira se prema
                    //trenutnoj otpremi
                    List<WarehouseStatusPositionDetails> groupedListOfArticlesForCurrOutgoing = groupedListOfAllArticles
                            .stream().filter(
                                    wsp -> getCurrentOutgoing().getValue().getOutgoingDetails().stream()
                                            .anyMatch(outDet -> wsp.getProductBoxID() == outDet.getProductBoxID()))
                            .collect(Collectors.toList());

                    getWarehouseStatusPositionMutableLiveData().setValue(warehouseStatusPositionList);
                    getPreloadingStatusPosDetailsFilteredMutLive().setValue(groupedListOfArticlesForCurrOutgoing);
                    getPreloadingStatusPosDetailsMutLive().setValue(groupedListOfAllArticles);

                    if (isFirstSync) {
                        getResponseMutableLiveData().postValue(ApiResponse.success());
                        isFirstSync = false;
                    }
                }
            }

        });
    }

    public void resetProductSpinner() {
        //ovde se resetuje spiner. Ako se posalje prazna lista onda kroz metodu za mappiranje se ubaci placeholder
        getWarehouseStatusPositionWithArticlesDetails().setValue(new ArrayList<>());
    }

    public void getProductBoxesOnPosition(String wPositionBarcode, int employeeID) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        CollectionReference warehouseStatusPos = firebaseFirestore.collection("WarehouseStatusPos");
        Query query = warehouseStatusPos
                .whereEqualTo("forPreloading", false)
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
                            getProductBoxesFromScannedPosition(wPositionBarcode, warehouseStatusPosition, employeeID);
                        } else {
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_locked_error)));
                            resetProductSpinner();
                        }


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

    private void getProductBoxesFromScannedPosition(String wPositionBarcode,
                                                    WarehouseStatusPosition warehouseStatusPosition, int employeeID) {

        try {
            List<OutgoingDetails> outgoingDetailsList = getCurrentOutgoing().getValue().getOutgoingDetails();
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

            //Zakljucavanje pozicije
            lockPosition(wPositionBarcode, employeeID);

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

    private void lockPosition(String wPositionBarcode, int employeeID) {
        DocumentReference warehouseStatusPosLock = firebaseFirestore.collection("WarehouseStatusPos").document(wPositionBarcode);
        warehouseStatusPosLock
                .update("locked", true, "lockedEmployeeID", employeeID)
                .addOnFailureListener(Utility::writeErrorToFile);

    }

    public void unlockAllPositions(int employeeID) {
        WriteBatch batch = firebaseFirestore.batch();
        Query query = firebaseFirestore.collection("WarehouseStatusPos")
                .whereEqualTo("locked", true)
                .whereEqualTo("lockedEmployeeID", employeeID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<WarehouseStatusPosition> wsp = querySnapshot.toObjects(WarehouseStatusPosition.class);
                        for (WarehouseStatusPosition ws : wsp) {
                            DocumentReference warehouseStatusPosUnlock = firebaseFirestore.collection("WarehouseStatusPos").document(String.valueOf(ws.getwPositionID()));
                            batch.update(warehouseStatusPosUnlock, "locked", false, "lockedEmployeeID", -1);
                        }

                        batch.commit().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utility.writeErrorToFile(e);
                            }
                        });
                    }
                }

            }
        });

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

    public void updateWarehouseStatusForPreloading(int positionID, String barcode) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        List<WarehouseStatusPositionDetails> forPreloadingPositionDetails;

        if (getWarehouseStatusPositionMutableLiveData().getValue() == null) {
            getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_preloading_list_not_exist_leave)));
            return;
        }

           /* Ovo je provera da li ova pozicija za predutovar postoji na firebase, posto moze da se desi situacija da uopste ne postoji
            pa mora da se prvo kreira na firebase pa tek onda da se dodaju artikli itd.
            * */
        WarehouseStatusPosition warehousePositionForPrelaoding = getWarehouseStatusPositionMutableLiveData().getValue()
                .stream()
                .filter(x -> x.getWarehousePositionBarcode().equals(barcode))
                .findFirst().orElse(null);

        if (warehousePositionForPrelaoding == null) {
            //Znaci da ne postoji na firebase ova pozicija za predutovar
            createWarehousePrelaodPositionAndRecursion(positionID, barcode);

        } else {
            //Znaci da postoji na firebase ova pozicija za predutovar i ide dalja logika, tj dobijanje wspDetails
            forPreloadingPositionDetails = warehousePositionForPrelaoding.getWspDetails();
            WriteBatch batch = firebaseFirestore.batch();
            /* Ova lista nikada nije null zato sto se instancira odmah, tako da ne mora provera da li je null
             * */
            List<OutgoingDetailsResult> tempList = getOutgoingDetailsResultMutLiveData().getValue();
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

                        /*ovde se dodaje kolicina u listu sa artiklima za poziciju za predutovar
                         * */

                        //Ovo je artikal na poziciji za predutovar koji se proverava
                        WarehouseStatusPositionDetails productOnPosForPreloading = forPreloadingPositionDetails.stream()
                                .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                                .findAny().orElse(null);

                        if (productOnPosForPreloading != null) {
                            /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                             * */
                            forPreloadingPositionDetails.stream()
                                    .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo() == null)
                                    .forEach(x ->
                                    {
                                        x.setQuantity(x.getQuantity() + (int) odr.getQuantity());
                                        x.setModifiedDate(new Date());

                                    });

                        } else {
                            /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
                             * */
                            forPreloadingPositionDetails.add(getWarehouseStatusPositionDetailsObject(
                                    odr.getProductBoxID(),
                                    (int) odr.getQuantity(),
                                    null));
                        }


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
                        /*ovde se dodaje kolicina u listu sa artiklima za poziciju za predutovar
                         * */

                        //Ovo je artikal na poziciji za predutovar koji se proverava
                        WarehouseStatusPositionDetails productOnPosForPreloading = forPreloadingPositionDetails.stream()
                                .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                                .findAny().orElse(null);

                        if (productOnPosForPreloading != null) {

                            /*Znaci da ga vec ima na poziciji za predutovar i samo ide update kolicine
                             * */
                            forPreloadingPositionDetails.stream()
                                    .filter(x -> x.getProductBoxID() == odr.getProductBoxID() && x.getSerialNo().equals(odr.getSerialNo()))
                                    .forEach(x ->
                                    {
                                        x.setQuantity(x.getQuantity() + (int) odr.getQuantity());
                                        x.setModifiedDate(new Date());

                                    });

                        } else {
                            /*Znaci da ga nema na poziciji za predutovar pa mora kreiranje novog objekta i insert u listu
                             * */
                            forPreloadingPositionDetails.add(getWarehouseStatusPositionDetailsObject(
                                    odr.getProductBoxID(),
                                    (int) odr.getQuantity(),
                                    odr.getSerialNo()));
                        }

                    }
                }

                //Update pozicija na firebaseu. Updateuju se norlamne pozicije tako sto im se postavljaju nove liste.
                DocumentReference documentReference = firebaseFirestore.collection("WarehouseStatusPos").document(posBarcode);
                batch.update(documentReference, "wspDetails", positionDetailsList, "locked", false, "lockedEmployeeID", -1);
            }

            //Update pozicija na firebaseu. Updateuju se pozicija za predutovar koja je skenirana tako sto joj se postavlja nova lista.
            DocumentReference documentReferenceForPreloading = firebaseFirestore.collection("WarehouseStatusPos").document(barcode);
            batch.update(documentReferenceForPreloading, "wspDetails", forPreloadingPositionDetails);

            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    resetListsAndViewsAfterSendingData(tempList, wrapperMap);
                    getResponseMutableLiveData().postValue(ApiResponse.successWithAction(resources.getString(R.string.product_moved_successfully)));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //TODO Ovde smisliti logiku sta da se radi
                    getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                }
            });
        }

    }

    private void resetListsAndViewsAfterSendingData(List<OutgoingDetailsResult> tempList,
                                                    Map<String, List<WarehouseStatusPositionDetails>> wrapperMap) {

        /*Brisanje svega iz temp liste
         * */
        tempList.clear();
        getOutgoingDetailsResultMutLiveData().setValue(tempList);

        /*Brisanje svega iz wrapper mape
         * */
        wrapperMap.clear();
        getWrapperOutgoingDetResMutableLiveData().setValue(wrapperMap);

        //Brisanje teksta iz editteksta gde je uneta pozicija za predutovar
        toggleViewEnabledAndText(R.id.outgoingPhaseOnePosPreloadingEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
        resetPositionBarcode();
    }

    private WarehouseStatusPositionDetails getWarehouseStatusPositionDetailsObject(int productBoxID, int quantity, String serialNumber) {
        WarehouseStatusPositionDetails productOnPosForPreloading = new WarehouseStatusPositionDetails();
        productOnPosForPreloading.setProductBoxID(productBoxID);
        productOnPosForPreloading.setSerialNo(serialNumber);
        productOnPosForPreloading.setModifiedDate(new Date());
        productOnPosForPreloading.setQuantity(quantity);
        return productOnPosForPreloading;
    }

    private void createWarehousePrelaodPositionAndRecursion(int positionID, String barcode) {
        WarehouseStatusPosition warehousePositionForPreloading = new WarehouseStatusPosition();
        warehousePositionForPreloading.setLocked(false);
        warehousePositionForPreloading.setLockedEmployeeID(-1);
        warehousePositionForPreloading.setForPreloading(true);
        warehousePositionForPreloading.setwPositionID(positionID);
        warehousePositionForPreloading.setWarehouseCode(null); //TODO ovde izmeniti ako treba. Kod talijana se koristi ovde ne za sada
        warehousePositionForPreloading.setWarehousePositionBarcode(barcode);
        warehousePositionForPreloading.setwSubPositionID(0);
        warehousePositionForPreloading.setWspDetails(new ArrayList<>());

        //Insert nove pozicije za predutovar na firebase
        firebaseFirestore.collection("WarehouseStatusPos")
                .document(barcode)
                .set(warehousePositionForPreloading)
                .addOnSuccessListener(aVoid -> {

                    /* Dodatna provera da li se nakon inserta na firebase ta dodata pozicija vratila preko realtimeListenera*/
                    if (getWarehouseStatusPositionMutableLiveData().getValue()
                            .stream()
                            .anyMatch(x -> x.getWarehousePositionBarcode().equals(barcode))) {

                        /* Rekurzivno pizivanje ove metode posto sada postoji pozicija*/
                        updateWarehouseStatusForPreloading(positionID, barcode);

                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.preloading_pos_not_exist_on_fb, barcode)));
                    }

                }).addOnFailureListener(e -> getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage())));

    }

    public void resetPositionBarcode() {
        getPositionBarcodeMutLiveData().setValue("");
        getCurrentWarehousePositionMutLiveData().setValue(null);
        getWarehouseStatusPositionWithArticlesDetails().setValue(null);
    }
}
