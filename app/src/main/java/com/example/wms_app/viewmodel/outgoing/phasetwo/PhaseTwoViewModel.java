package com.example.wms_app.viewmodel.outgoing.phasetwo;

import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.wms_app.R;
import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.dao.ProductItemTypeDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumAdditionType;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.model.OutgoingTruckResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ProductItemType;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.repository.data.EmployeeRepository;
import com.example.wms_app.repository.outgoing.phasetwo.OutgoingPhaseTwoRepository;
import com.example.wms_app.repository.data.ProductBoxRepository;
import com.example.wms_app.repository.data.TruckRepository;
import com.example.wms_app.repository.data.WarehousePositionRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.utilities.mapper.AsyncMapperPhaseOne;
import com.example.wms_app.utilities.mapper.AsyncMapperPhaseTwo;
import com.example.wms_app.utilities.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PhaseTwoViewModel extends AndroidViewModel {

    private final Resources resources;
    private final OutgoingPhaseTwoRepository outgoingPhaseTwoRepository;
    private final WarehousePositionRepository warehousePositionRepository;
    private final ProductBoxRepository productBoxRepository;
    private final EmployeeRepository employeeRepository;
    private final TruckRepository truckRepository;
    private final ProductBoxDao productBoxDao;
    private final ProductItemTypeDao productItemTypeDao;
    private MutableLiveData<Integer> requestFocusViewID;
    private LiveData<List<ProductBox>> mFilteredProductBoxListForSpinner;
    private LiveData<List<OutgoingDetailsResultPreview>> mOutgoingDetailsResultPreviewList;
    private MediatorLiveData<Integer> totalNumberOfAddedProdMediatorLiveData;
    private final int employeeIDDb;


    public PhaseTwoViewModel(@NonNull Application application) {
        super(application);
        resources = application.getResources();
        outgoingPhaseTwoRepository = new OutgoingPhaseTwoRepository(application.getApplicationContext());
        warehousePositionRepository = new WarehousePositionRepository(application.getApplicationContext());
        productBoxRepository = new ProductBoxRepository(application.getApplicationContext());
        truckRepository = new TruckRepository(application.getApplicationContext());
        employeeRepository = new EmployeeRepository(application.getApplicationContext());
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();


        productBoxDao = RoomDb.getDatabase(application.getApplicationContext()).productBoxDao();
        productItemTypeDao = RoomDb.getDatabase(application.getApplicationContext()).productItemTypeDao();
    }

    public LiveData<ApiResponse> getApiResponseLiveData() {
        return outgoingPhaseTwoRepository.getResponseMutableLiveData();
    }

    public LiveData<Integer> getRequestFocusViewID() {
        if (requestFocusViewID == null)
            requestFocusViewID = new MutableLiveData<>();
        return requestFocusViewID;
    }

    /**
     * Ovaj objekat se koristi kako bi Enable/Disable polja za unos kolicine i serijskog broja
     *
     * @return viewEnableHelperMutableLiveData
     */
    public LiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        return outgoingPhaseTwoRepository.getViewEnableHelperLiveData();
    }

    public LiveData<ProductBox> getScannedProductBoxLiveData() {
        return outgoingPhaseTwoRepository.getScannedProductBoxMutLiveData();
    }

    public LiveData<String> getPositionBarcode() {
        return outgoingPhaseTwoRepository.getPositionBarcodeMutLiveData();
    }

    public LiveData<List<OutgoingTruckResult>> getOutgoingTruckResultLiveData() {
        return outgoingPhaseTwoRepository.getOutgoingTruckResultMutLiveData();
    }

    public LiveData<Boolean> getIsOutgoingFinishedLiveData() {
        return outgoingPhaseTwoRepository.getIsOutgoingFinishedMutableLiveData();

    }

    public LiveData<List<String>> getLicencePlateListLiveData() {
        return truckRepository.getLicencePlateListMutLiveData();
    }


    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeRepository.getEmployeeIDLiveData();
    }

    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewFromOutgoing() {
        //DANIEL IZMENA
        return mOutgoingDetailsResultPreviewList = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                AsyncMapperPhaseTwo.getOutgoingPhaseTwoLeftPreview(
                        outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData(),
                        outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData(),
                        this::getOutgoingDetailsResultPreviewFromOutgoingInput) :
                AsyncMapperPhaseTwo.getOutgoingPhaseTwoLeftPreviewGrouped(
                        outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData(),
                        outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData(),
                        this::getOutgoingDetailsResultPreviewFromOutgoingInput);

    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewFromOutgoingInput() {
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        Outgoing currentOutgoing = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue();
        List<OutgoingDetailsResultPreview> list = currentOutgoing.getOutgoingDetails().stream().map(m -> getPreviewValuesForOutgoingDetails(m, tempList, outgoingDetailsResListOnFb)).collect(Collectors.toList());
           /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(OutgoingDetailsResultPreview::getProductBoxName));
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
        return list;
    }


    private OutgoingDetailsResultPreview getPreviewValuesForOutgoingDetails(OutgoingDetails outgoingDetails,
                                                                            List<OutgoingDetailsResult> tempList,
                                                                            List<OutgoingDetailsResult> outgoingDetailsResListOnFb) {

        ProductBox pb = productBoxRepository.getProductBoxByID(outgoingDetails.getProductBoxID());
        if (pb == null)
            return null;

        //U PhaseOnePreviewAdapter-u ima oznaceno kako je sta mapirano
        OutgoingDetailsResultPreview outgoingDetailsResultPreview = new OutgoingDetailsResultPreview(
                0,
                pb.getProductBoxName(),
                pb.getProductBoxCode(),
                0,
                (int) outgoingDetails.getQuantity(),
                outgoingDetails.getProductBoxID(),
                pb.getProductItemTypeID()
        );

        setColorAndQuantityOnProdBoxForPreview(outgoingDetailsResultPreview, tempList, outgoingDetailsResListOnFb);
        return outgoingDetailsResultPreview;
    }


    public MediatorLiveData<Integer> getTotalNumberOfAddedProdMediatorLiveData() {
        if (totalNumberOfAddedProdMediatorLiveData == null) {
            totalNumberOfAddedProdMediatorLiveData = new MediatorLiveData<>();
            totalNumberOfAddedProdMediatorLiveData.addSource(outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData(),
                    value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            if (outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE) {
                totalNumberOfAddedProdMediatorLiveData.addSource(outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData(),
                        value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            } else {
                totalNumberOfAddedProdMediatorLiveData.addSource(outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData(),
                        value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            }
        }
        return totalNumberOfAddedProdMediatorLiveData;
    }

    private Integer sumTotalAddedQuantity() {
        int totalSum;

        int quantityInTemp = (int) outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue().stream()
                .mapToDouble(OutgoingDetailsResult::getQuantity)
                .sum();

        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        int quantityOnFb = (int) outgoingDetailsResListOnFb.stream()
                .mapToDouble(OutgoingDetailsResult::getQuantity)
                .sum();
        totalSum = quantityInTemp + quantityOnFb;
        return totalSum;
    }

    public LiveData<List<ProductBox>> getProductBoxListMediatorLiveData() {
        //DANIEL IZMENA
        if (mFilteredProductBoxListForSpinner == null) {
            if (outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE) {
                mFilteredProductBoxListForSpinner = AsyncMapperPhaseTwo.getProductBoxForSpinnerPhaseTwo(
                        outgoingPhaseTwoRepository.getWarehouseStatusPositionWithArticlesDetails(),
                        outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData(),
                        outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData(),
                        this::getProductBoxFromWPosDetails
                        //   this::changeColorAndQuantities
                );
            } else {
                mFilteredProductBoxListForSpinner = AsyncMapperPhaseTwo.getProductBoxForSpinnerPhaseTwoGrouped(
                        outgoingPhaseTwoRepository.getWarehouseStatusPositionWithArticlesDetails(),
                        outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData(),
                        outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData(),
                        this::getProductBoxFromWPosDetails);
            }
        }
        return mFilteredProductBoxListForSpinner;
    }

    public LiveData<List<OutgoingDetailsResult>> getOutgoingDetailsResultLiveData() {
        return outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData();
    }

    public LiveData<Boolean> toggleUndoAndRefreshBtnLiveData() {
        return Transformations.map(getOutgoingDetailsResultLiveData(), List::isEmpty);
    }

    private List<ProductBox> getProductBoxFromWPosDetails() {
        List<WarehouseStatusPositionDetails> listWithArticlesOnPos = outgoingPhaseTwoRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();

        if (listWithArticlesOnPos == null || listWithArticlesOnPos.isEmpty()) {
            List<ProductBox> productBoxList = new ArrayList<>();
            productBoxList.add(ProductBox.newPlaceHolderInstance());
            return productBoxList;
        }
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        //Ovde se lista outgoingDetailsResListOnFb definise u zavisnosti da li se radi o pojedinacnoj ili grupnoj otpremi
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        Outgoing currentOutgoing = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue();

        List<ProductBox> list = listWithArticlesOnPos.stream().map(warehouseStatusPositionDetails ->
                getProductBoxDataFromDb(warehouseStatusPositionDetails, tempList, outgoingDetailsResListOnFb, currentOutgoing)).collect(Collectors.toList());

            /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(ProductBox::getProductBoxName));
        return list;
    }

    private ProductBox getProductBoxDataFromDb(WarehouseStatusPositionDetails warehouseStatusPositionDetails,
                                               List<OutgoingDetailsResult> tempList,
                                               List<OutgoingDetailsResult> outgoingDetailsResListOnFb,
                                               Outgoing outgoing) {
        ProductBox pb = productBoxRepository.getProductBoxByID(warehouseStatusPositionDetails.getProductBoxID());
        if (pb == null)
            return null;

        //Ovde se za expected i added Qty stavlja 0 zato sto se u metodi unutra posle setuju te vrednosti
        ProductBox prodBox = new ProductBox(
                pb.getProductBoxID(),
                pb.getProductBoxName(),
                pb.getProductBoxBarcode(),
                pb.isSerialMustScan(),
                0,
                0,
                pb.getProductBoxCode()

        );
        setColorAndQuantityOnProdBox(prodBox, tempList, outgoingDetailsResListOnFb, outgoing);
        return prodBox;
    }

    private List<ProductBox> changeColorAndQuantities(List<ProductBox> productBoxList) {

        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue();
        Outgoing currentOutgoing = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue();
        for (ProductBox productBox : productBoxList) {

            if (productBox.getProductBoxID() == -1) {
                productBox.setColorStatus(0);
                productBox.setExpectedQuantity(0);
                productBox.setAddedQuantity(0);
                continue;
            }

            setColorAndQuantityOnProdBox(productBox, tempList, outgoingDetailsResListOnFb, currentOutgoing);
        }
        return productBoxList;
    }

    private List<OutgoingDetailsResultPreview> changeColorAndQuantitiesForPreview(List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList) {

        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue();
        for (OutgoingDetailsResultPreview outgoingDetailsResultPreview : outgoingDetailsResultPreviewList) {

            setColorAndQuantityOnProdBoxForPreview(outgoingDetailsResultPreview, tempList, outgoingDetailsResListOnFb);
        }
        return outgoingDetailsResultPreviewList;
    }

    private void setColorAndQuantityOnProdBoxForPreview(OutgoingDetailsResultPreview outgoingDetailsResultPreview,
                                                        List<OutgoingDetailsResult> tempList,
                                                        List<OutgoingDetailsResult> outgoingDetailsResListOnFb
    ) {
        try {


            int addedQuantityInTemp;
            int addedQuantityOnFirebase;

            addedQuantityInTemp = tempList == null ? 0 :
                    (int) tempList.stream()
                            .filter(k -> k.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

            addedQuantityOnFirebase = outgoingDetailsResListOnFb == null ? 0 :
                    (int) outgoingDetailsResListOnFb.stream()
                            .filter(m -> m.getProductBoxID() == outgoingDetailsResultPreview.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();


            outgoingDetailsResultPreview.setQuantity(addedQuantityInTemp + addedQuantityOnFirebase);
            outgoingDetailsResultPreview.setExpectedQuantity(outgoingDetailsResultPreview.getExpectedQuantity());

            //Postavljanje colorstatus zbog bojenja u spinneru
            if (addedQuantityInTemp + addedQuantityOnFirebase >= outgoingDetailsResultPreview.getExpectedQuantity()) {
                outgoingDetailsResultPreview.setColorStatus(2);
            } else {
                outgoingDetailsResultPreview.setColorStatus(0);
            }
        } catch (ConcurrentModificationException exception) {
            Utility.writeErrorToFile(exception);
        }
    }

    private void setColorAndQuantityOnProdBox(ProductBox productBox,
                                              List<OutgoingDetailsResult> tempList,
                                              List<OutgoingDetailsResult> outgoingDetailsResListOnFb,
                                              Outgoing currentOutgoing) {
        try {
            int addedQuantityInTemp;
            int addedQuantityOnFirebase;
            int expectedQuantity;

            addedQuantityInTemp = tempList == null ? 0 :
                    (int) tempList.stream()
                            .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

            addedQuantityOnFirebase = outgoingDetailsResListOnFb == null ? 0 :
                    (int) outgoingDetailsResListOnFb.stream()
                            .filter(m -> m.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

            expectedQuantity = currentOutgoing == null ? 0 :
                    (int) currentOutgoing.getOutgoingDetails().stream()
                            .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(OutgoingDetails::getQuantity)
                            .sum();

            productBox.setAddedQuantity(addedQuantityInTemp + addedQuantityOnFirebase);
            productBox.setExpectedQuantity(expectedQuantity);

            //Postavljanje colorstatus zbog bojenja u spinneru
            if (addedQuantityInTemp + addedQuantityOnFirebase >= expectedQuantity) {
                productBox.setColorStatus(2);
            } else {
                productBox.setColorStatus(0);
            }

        } catch (ConcurrentModificationException exception) {
            Utility.writeErrorToFile(exception);
        }
    }

    public void refreshApiResponseStatus() {
        outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }


    public void codeScanned(String scannedCode) {
        if (scannedCode != null) {

            if (scannedCode.length() == Constants.POSITION_BARCODE_LENGTH || scannedCode.length() == Constants.SUB_POSITION_BARCODE_LENGTH) {
                /* Znaci da se radi o poziciji
                ili podpoziciji */
                positionWithArticlesSelected(scannedCode);

            } else {
                //Znaci da se radi o artiklu
                articleScanned(scannedCode);
            }

        } else {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.wrong_barcode)));
        }
    }

    private void articleScanned(String scannedCode) {
        WarehousePosition currentWPosition = outgoingPhaseTwoRepository.getCurrentWarehousePositionMutLiveData().getValue();
        if (currentWPosition == null) {
            /* Ne moze da se skenira artikal ako nije postavljena pozicija
             * */
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.position_not_picked)));
            return;
        }

        ProductBox scannedProductBox = getProductBoxListMediatorLiveData().getValue()
                .stream().filter(x -> x.getProductBoxBarcode().equals(scannedCode))
                .findAny()
                .orElse(null);
        if (scannedProductBox == null) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.scanned_prod_box_not_on_outgoing)));
            return;
        }

        outgoingPhaseTwoRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);

        if (scannedProductBox.isSerialMustScan()) {
            //Znaci da mora serisjki broj tj veliki artikal

            /* Cim je serialMustScan odmah kolicina
                            ide na 1 i disabluje se polje */
            //Trenutno nemamo evidenciju o serisjib brojevima. Zato se ovde postavlja prazan string incae bi se postavio serialNumber Ako se bude ubacila ovde ide kod TODO Serijski broj, serial number, sr num
            //Za sada samo iskace greska da mora da se unese sr broj kako bi blokiralo dalje skeniranje
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
        } else {
            //Znaci da ne mora serisjki broj tj mali artikal
            addSmallProductToTempList(currentWPosition.getBarcode(), scannedProductBox, "", 1, EnumAdditionType.SCANNING, true);
        }
    }

    public void setupCurrentOutgoing(Outgoing currentOutgoing) {
        outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().setValue(currentOutgoing);
    }

    public void setupCurrentOutgoingStyle(EnumOutgoingStyle enumOutgoingStyle) {
        outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().setValue(enumOutgoingStyle);
    }

    public void registerFirebaseRealTimeUpdates() {
//        outgoingPhaseTwoRepository.registerRealTimeUpdatesResultDetails();
//        outgoingPhaseTwoRepository.registerRealTimeUpdatesTruckResult();
//        outgoingPhaseTwoRepository.registerRealTimeUpdatesOutgoing();
    }

    public void removeFirebaseRealTimeListener() {
        outgoingPhaseTwoRepository.removeFirebaseRealTimeListener();
    }

    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewFromTempList() {
        return AsyncMapperPhaseOne.getTempListPreview(
                outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData(),
                this::getOutgoingDetailsResultPreviewFromTempListInput
        );
    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewFromTempListInput() {
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<OutgoingDetailsResultPreview> list = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue().stream().map(this::getPreviewValuesForTempList).collect(Collectors.toList());
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
        return list;
    }

    private OutgoingDetailsResultPreview getPreviewValuesForTempList(OutgoingDetailsResult outgoingDetailsResult) {
        ProductBox pb = productBoxRepository.getProductBoxByID(outgoingDetailsResult.getProductBoxID());

        return new OutgoingDetailsResultPreview(
                (int) outgoingDetailsResult.getQuantity(),
                resources.getString(R.string.product_code_and_name, pb.getProductBoxCode(), pb.getProductBoxName()),
                outgoingDetailsResult.getwPositionBarcode(),
                outgoingDetailsResult.getSerialNo()
        );
    }


    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewListLiveData() {

        return outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                AsyncMapperPhaseTwo.getOutgoingPhaseTwoDonePreview(
                        outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData(),
                        this::getOutgoingDetailsResultPreviewDoneFromFb) :
                AsyncMapperPhaseTwo.getOutgoingPhaseTwoDonePreviewGrouped(
                        outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData(),
                        this::getOutgoingDetailsResultPreviewDoneFromFb);
    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewDoneFromFb() {
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        List<OutgoingDetailsResultPreview> list = outgoingDetailsResListOnFb.stream().map(this::getPreviewValues).collect(Collectors.toList());
            /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(OutgoingDetailsResultPreview::getProductBoxName));
        outgoingPhaseTwoRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
        return list;
    }

    private OutgoingDetailsResultPreview getPreviewValues(OutgoingDetailsResult outgoingDetailsResult) {
        ProductBox pb = productBoxRepository.getProductBoxByID(outgoingDetailsResult.getProductBoxID());
        if (pb == null)
            return null;

        return new OutgoingDetailsResultPreview(
                (int) outgoingDetailsResult.getQuantity(),
                pb.getProductBoxName(),
                outgoingDetailsResult.getOutgoingID(),
                resources.getString(R.string.product_code_and_name, pb.getProductBoxCode(), pb.getProductBoxName()),
                outgoingDetailsResult.getwPositionBarcode(),
                outgoingDetailsResult.getSerialNo(),
                outgoingDetailsResult.getProductBoxID(),
                outgoingDetailsResult.isSent(),
                outgoingDetailsResult.getCreateDate()
        );
    }

    public void productBoxManuallySelected(ProductBox currentProductBox) {
        if (currentProductBox.isSerialMustScan()) {
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoAddNoSrNumBtn, "", true, EnumViewType.BUTTON, View.VISIBLE);
            requestFocusViewID.setValue(R.id.outgoingPhaseTwoSrNumberEt);
        } else {
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoSrNumberEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.INVISIBLE);
        }
        //Ovo za ocekivanu  i dodatu kolicinu ide ovde posto se postavlja u svakom slucaju
        outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoLeftQtyEt, resources.getString(R.string.added_and_expected_qty, currentProductBox.getAddedQuantity(), currentProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
        outgoingPhaseTwoRepository.getCurrentProductBox().setValue(currentProductBox);
    }

    public void positionWithArticlesSelected(String scannedCode) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        //TODO Pitaj cofija da li treba provera da li je obicna pozicija zakljucana posto moze da bude
        WarehousePosition warehousePosition = isPositionByBarcodeValid(scannedCode);
        if (warehousePosition == null) {
            return;
        }

        outgoingPhaseTwoRepository.getCurrentWarehousePositionMutLiveData().setValue(warehousePosition);
        outgoingPhaseTwoRepository.getPositionBarcodeMutLiveData().setValue(warehousePosition.getBarcode());
        outgoingPhaseTwoRepository.getProductBoxesOnPosition(warehousePosition.getBarcode(), employeeID);
    }

    private WarehousePosition isPositionByBarcodeValid(String scannedCode) {

        /* Dobijanje pozicije na osnovu skeniranog barkoda*/
        WarehousePosition warehousePosition = warehousePositionRepository.getWarehousePositionByBarcode(scannedCode);
        if (warehousePosition == null) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_exist_or_not_for_outgoing)));
            outgoingPhaseTwoRepository.resetProductSpinner();
            return null;
        }
        if (!warehousePosition.isForOutgoing()) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_defined_for_outgoing)));
            outgoingPhaseTwoRepository.resetProductSpinner();
            return null;
        }
        return warehousePosition;
    }

    public void resetPositionBarcode() {
        outgoingPhaseTwoRepository.resetPositionBarcode();
    }

    public void removeAllFromTempList() {
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null) {
            tempList.clear();
            outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_all_from_temp_list)));
        }
    }

    public void removeLastFromTempList() {
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(tempList.size() - 1);
            outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_last_from_temp_list)));
        }

    }

    public void deleteProductBoxFromTempList(int position) {
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(position);
            outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_temp_list)));
        }
    }

    public void addProductBoxManually(ProductBox currentProductBox, String serialNumber, String quantity) {

        WarehousePosition currentWPosition = outgoingPhaseTwoRepository.getCurrentWarehousePositionMutLiveData().getValue();
        if (currentWPosition == null) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.position_not_picked)));
            return;
        }

        if (currentProductBox == null || currentProductBox.getProductBoxID() == -1) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.product_not_selected_from_list)));
            return;
        }

        if (quantity.isEmpty()) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_not_inserted)));
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_bad_format)));
            return;
        }

        if (qty <= 0) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_must_be_over_zero)));
            return;
        }

        if (currentProductBox.isSerialMustScan()) {
            if (!serialNumber.isEmpty()) {
                addBigProductToTempList(currentWPosition.getBarcode(), currentProductBox, serialNumber, qty, false);
            } else {
                outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
            }

        } else {
            addSmallProductToTempList(currentWPosition.getBarcode(), currentProductBox, "", qty, EnumAdditionType.MANUALLY, false);
        }

    }

    private void addBigProductToTempList(String wPositionBarcode, ProductBox selectedProductBox, String serialNumber, int quantity, boolean isScanned) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        /*Mora ovako posto ne moze u lambda expression da se prosledi selectedProduct objekat
         * */
        int selectedProductBoxID = selectedProductBox.getProductBoxID();
        List<WarehouseStatusPositionDetails> articlesOnCurrentPosList = outgoingPhaseTwoRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        Outgoing currentOutgoing = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue();

        if (articlesOnCurrentPosList.stream()
                .noneMatch(x -> x.getSerialNo() != null && x.getSerialNo().equals(serialNumber) &&
                        x.getProductBoxID() == selectedProductBoxID)) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_not_exist_on_pos)));
            return;
        }
            /*Sada je moguce dodati 2 artikla sa istim serijskim brojem i istim IDjem. Tako da se ovde proerava kolicina kao kod malih artikla.
            Ako je sve ok ide dodavanje u listi. U temp listi to ce biti 2 objekta ali na firebase ce se kolicina grupisati.
            * */
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        int addedQuantityInTemp = tempList == null ? 0 :
                (int) tempList.stream()
                        .filter(k -> k.getProductBoxID() == selectedProductBoxID && k.getSerialNo().equals(serialNumber))
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();

        int quantityOnPosition = (int) articlesOnCurrentPosList
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID && x.getSerialNo().equals(serialNumber))
                .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                .sum();

        int reservedQuantityOnPos = (int) articlesOnCurrentPosList
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID && x.getSerialNo().equals(serialNumber))
                .mapToDouble(WarehouseStatusPositionDetails::getReservedQuantity)
                .sum();

        int addedQuantityOnFirebase = outgoingDetailsResListOnFb == null ? 0 :
                (int) outgoingDetailsResListOnFb.stream()
                        .filter(x -> x.getProductBoxID() == selectedProductBoxID && x.getSerialNo().equals(serialNumber))
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();


        //TODO Ovde ne postoji u OutgoingDetails polje za serijski broj. Tako da ne mogu u uslovu da ga postavim.
        //TODO Svejedno ne bi trebalo da pravi probleme posto se kolicina grupise po ID artikla
        int expectedQuantity = currentOutgoing == null ? 0 :
                (int) currentOutgoing.getOutgoingDetails().stream()
                        .filter(k -> k.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(OutgoingDetails::getQuantity)
                        .sum();

        if (addedQuantityInTemp + quantity <= quantityOnPosition) {

            //Proverd da li je je uneta kolicina (na FB + tempList + quantitiy) veca od expectedQty.
            //Ako jeste ne moze d ase doda
            if (addedQuantityInTemp + addedQuantityOnFirebase + quantity > expectedQuantity) {
                int totalAddedQty = addedQuantityInTemp + addedQuantityOnFirebase + quantity;
                outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                        ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected_on_outgoing, expectedQuantity, totalAddedQty)));
                return;
            }


            //Provera za rezervisanu kolicinu
            //Proverava se i za poz sa artiklima i za poz za predutovar, posto kod prijema, moguce je staviti rezervisane
            //artikle i na poziciju za predutovar. Tako da onda i to treba da se proverava ovdee

            if ((quantityOnPosition - reservedQuantityOnPos >= addedQuantityInTemp + quantity)) {
                //Znaci da je sve ok i nema potreb da se prikaze obavestenje
                tempList.add(new OutgoingDetailsResult(
                        selectedProductBoxID,
                        serialNumber,
                        quantity,
                        wPositionBarcode,
                        isScanned,
                        false,
                        employeeID
                ));

                outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                //Resetovanje polja za serijski broj
                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);


            } else {
                //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                int totalAddedQty = addedQuantityInTemp + quantity;
                outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                        ApiResponse.prompt(resources.getString(R.string.reserved_quantity_prompt, quantityOnPosition, reservedQuantityOnPos, totalAddedQty), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tempList.add(new OutgoingDetailsResult(
                                        selectedProductBoxID,
                                        serialNumber,
                                        quantity,
                                        wPositionBarcode,
                                        isScanned,
                                        true,
                                        employeeID
                                ));

                                outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                //Resetovanje polja za serijski broj
                                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                            }
                        }));
            }

        } else {
            /* Znaci da je uneta kolicina vece od one koja je na poziciji
             *  */
            int totalAddedQty = addedQuantityInTemp + quantity;
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
        }
    }

    private void addSmallProductToTempList(String wPositionBarcode, ProductBox selectedProductBox, String serialNumber, int quantity, EnumAdditionType enumAdditionType, boolean isScanned) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        int selectedProductBoxID = selectedProductBox.getProductBoxID();
        List<OutgoingDetailsResult> tempList = outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<WarehouseStatusPositionDetails> articlesOnCurrentPosList = outgoingPhaseTwoRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();
        List<OutgoingDetailsResult> outgoingDetailsResListOnFb = outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE ?
                outgoingPhaseTwoRepository.getOutgoingDetailsResultFromFbMutLiveData().getValue() :
                outgoingPhaseTwoRepository.getHashMapDetailsResultMutableLiveData().getValue()
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        Outgoing currentOutgoing = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue();
        int addedQuantityInTemp = tempList == null ? 0 :
                (int) tempList.stream()
                        .filter(k -> k.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();


        int quantityOnPosition = articlesOnCurrentPosList == null ? 0 :
                (int) articlesOnCurrentPosList
                        .stream()
                        .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                        .sum();

        int reservedQuantityOnPos = articlesOnCurrentPosList == null ? 0 :
                (int) articlesOnCurrentPosList
                        .stream()
                        .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(WarehouseStatusPositionDetails::getReservedQuantity)
                        .sum();


        int addedQuantityOnFirebase = outgoingDetailsResListOnFb == null ? 0 :
                (int) outgoingDetailsResListOnFb.stream()
                        .filter(m -> m.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();


        int expectedQuantity = currentOutgoing == null ? 0 :
                (int) currentOutgoing.getOutgoingDetails().stream()
                        .filter(k -> k.getProductBoxID() == selectedProductBoxID)
                        .mapToDouble(OutgoingDetails::getQuantity)
                        .sum();

        /*
        Ova provera se samo vrsi kada se rucno dodaje artikal*/
        if (enumAdditionType == EnumAdditionType.MANUALLY) {
            if (tempList.size() > 0) {
                OutgoingDetailsResult lastAddedProd = tempList.get(tempList.size() - 1);
                if (lastAddedProd.getProductBoxID() == selectedProductBoxID) {
                    //Generalna provera da li ima dovoljna kolicina na pozicija koja se dodaje u temp listu
                    if (addedQuantityInTemp + quantity - (int) lastAddedProd.getQuantity() <= quantityOnPosition) {


                        //Proverd da li je je uneta kolicina (na FB + tempList + quantitiy) - lastAddedQty veca od expectedQty.
                        //Ako jeste ne moze d ase doda
                        if (addedQuantityInTemp + addedQuantityOnFirebase + quantity - (int) lastAddedProd.getQuantity() > expectedQuantity) {
                            int totalAddedQty = addedQuantityInTemp + addedQuantityOnFirebase + quantity - (int) lastAddedProd.getQuantity();
                            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                                    ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected_on_outgoing, expectedQuantity, totalAddedQty)));
                            return;
                        }

                        //Provera rezervisane kolicine
                        if (quantityOnPosition - reservedQuantityOnPos - (int) lastAddedProd.getQuantity() >= addedQuantityInTemp + quantity) {
                            //Znaci da je sve ok i nema potreb da se prikaze obavestenje

                            tempList.remove(tempList.size() - 1);
                            tempList.add(new OutgoingDetailsResult(
                                    selectedProductBoxID,
                                    serialNumber,
                                    quantity,
                                    wPositionBarcode,
                                    isScanned,
                                    false,
                                    employeeID
                            ));
                            outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                        } else {
                            //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                            //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                            int totalAddedQty = addedQuantityInTemp + quantity;
                            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                                    ApiResponse.prompt(resources.getString(R.string.reserved_quantity_prompt, quantityOnPosition, reservedQuantityOnPos, totalAddedQty), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            tempList.remove(tempList.size() - 1);
                                            tempList.add(new OutgoingDetailsResult(
                                                    selectedProductBoxID,
                                                    serialNumber,
                                                    quantity,
                                                    wPositionBarcode,
                                                    isScanned,
                                                    true,
                                                    employeeID
                                            ));

                                            outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                            outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                                        }
                                    }));
                        }


                    } else {
                        /* Znaci da je uneta kolicina vece od one koja je na poziciji
                         *  */
                        int totalAddedQty = addedQuantityInTemp + (int) quantity - (int) lastAddedProd.getQuantity();
                        outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                                ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
                    }
                    return;
                }
            }
        }
        if (addedQuantityInTemp + quantity <= quantityOnPosition) {

            //Proverd da li je je uneta kolicina (na FB + tempList + quantitiy) veca od expectedQty.
            //Ako jeste ne moze d ase doda
            if (addedQuantityInTemp + addedQuantityOnFirebase + quantity > expectedQuantity) {
                int totalAddedQty = addedQuantityInTemp + addedQuantityOnFirebase + quantity;
                outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                        ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected_on_outgoing, expectedQuantity, totalAddedQty)));
                return;
            }

            //Provera za rezervisanu kolicinu
            if (quantityOnPosition - reservedQuantityOnPos >= addedQuantityInTemp + quantity) {
                //Znaci da je sve ok i nema potreb da se prikaze obavestenje
                tempList.add(new OutgoingDetailsResult(
                        selectedProductBoxID,
                        serialNumber,
                        quantity,
                        wPositionBarcode,
                        isScanned,
                        false,
                        employeeID
                ));
                outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

            } else {
                //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                int totalAddedQty = addedQuantityInTemp + quantity;
                outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                        ApiResponse.prompt(resources.getString(R.string.reserved_quantity_prompt, quantityOnPosition, reservedQuantityOnPos, totalAddedQty), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tempList.add(new OutgoingDetailsResult(
                                        selectedProductBoxID,
                                        serialNumber,
                                        quantity,
                                        wPositionBarcode,
                                        isScanned,
                                        true,
                                        employeeID
                                ));
                                outgoingPhaseTwoRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                            }
                        }));

            }
        } else {
            /* Znaci da je uneta kolicina vece od one koja je na poziciji
             *  */
            int totalAddedQty = addedQuantityInTemp + quantity;
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
        }
    }

    public void refreshExpectedQty(ProductBox productBox) {
        if (productBox != null) {
            if (productBox.getProductBoxID() == -1) {
                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoQtyEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoLeftQtyEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            } else {
                outgoingPhaseTwoRepository.toggleViewEnabledAndText(R.id.outgoingPhaseTwoLeftQtyEt, resources.getString(R.string.added_and_expected_qty, productBox.getAddedQuantity(), productBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            }
            outgoingPhaseTwoRepository.getCurrentProductBox().setValue(productBox);
        }
    }


    public void pushTempListToFirebase() {
        if (outgoingPhaseTwoRepository.getEnumOutgoingStyleMutableLiveData().getValue() == EnumOutgoingStyle.SINGLE)
            outgoingPhaseTwoRepository.pushTempListToFirebase();
        else
            outgoingPhaseTwoRepository.pushTempListToFirebaseGrouped();

    }

    public void sendOutgoingToServer() {
        //TODO PRovera za grupne naloge i ovde
        //Provera da li treba da iskace dijalog sa pitanjem ako nalog nije zavrsen
        boolean isFinished = outgoingPhaseTwoRepository.getIsOutgoingFinishedMutableLiveData().getValue() == null ?
                false :
                outgoingPhaseTwoRepository.getIsOutgoingFinishedMutableLiveData().getValue();
        if (isFinished)
            outgoingPhaseTwoRepository.sendOutgoingToServerAndFirebase();
        else {
            outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.outgoing_not_finished_send_prompt),
                            (dialogInterface, i) -> outgoingPhaseTwoRepository.sendOutgoingToServerAndFirebase()));
        }
    }

    public void sendOutgoingToServerGrouped() {
        outgoingPhaseTwoRepository.getResponseMutableLiveData().setValue(
                ApiResponse.prompt(resources.getString(R.string.outgoings_send_prompt),
                        (dialogInterface, i) -> outgoingPhaseTwoRepository.getOutgoingListToBeSent()));
    }

    public void insertTruckToFirebase(String truckDriver, String licencePlate, int employeeId) {
        outgoingPhaseTwoRepository.insertTruckToFirebase(truckDriver, licencePlate, employeeId);
    }

    public void deleteTruckFromFirebase(OutgoingTruckResult outgoingTruckResult) {
        outgoingPhaseTwoRepository.deleteTruckFromFirebase(outgoingTruckResult);
    }


    public void deleteOutgoingDetailsResultFromFirebase(OutgoingDetailsResultPreview outgoingDetailsResultPreview) {
        outgoingPhaseTwoRepository.deleteOutgoingDetailsResultFromFirebase(outgoingDetailsResultPreview);
    }

    //DUSAN DODAO - RAD SA FILTEROM
    public List<ProductItemType> getDistinctProductTypeID() {
        //ovde pravi listu svih ProductBoxID koji su jedinstveni za tu otpemu
        List<Integer> distinctProductBoxIDsList = outgoingPhaseTwoRepository.getSelectedOutgoingMutLiveData().getValue().getOutgoingDetails()
                .stream().filter(Utility.distinctByKey(x -> x.getProductBoxID())).map(OutgoingDetails::getProductBoxID).collect(Collectors.toList());
        return productItemTypeDao.getProductItemTypeList(productBoxDao.getProductItemTypeByID(distinctProductBoxIDsList));
    }

    public List<OutgoingDetailsResultPreview> getFilteredOutgoingDetailsResultPreview(Set<Integer> productTypeIDsList) {
        return mOutgoingDetailsResultPreviewList.getValue().stream()
                .filter(x -> productTypeIDsList.contains(x.getProductTypeID())).collect(Collectors.toList());
    }

    public void registerFirebaseRealTimeUpdatesOutgoing(Outgoing currentOutgoing) {
        setupCurrentOutgoingStyle(EnumOutgoingStyle.SINGLE);
        setupCurrentOutgoing(currentOutgoing);
        outgoingPhaseTwoRepository.registerRealTimeUpdatesOutgoingDetailsSingle(currentOutgoing);
        outgoingPhaseTwoRepository.registerRealTimeUpdatesOutgoingTruckSingle(currentOutgoing);
        outgoingPhaseTwoRepository.registerRealTimeUpdatesOutgoing(currentOutgoing);
    }

    public void registerFirebaseRealTimeUpdatesOutgoingGrouped(OutgoingGrouped currentOutgoingGrouped) {
        //Da bih radio samo sa jednim objektom pretvaram ovu grupnu otpremu u pojedinacnu. I tako cu uvek
        //raditi sa pojedinacnom, posto mi je nebitno u fragmentu koji je tip. Inace bih morao uvek da postavljam uslov
        //da li je SINGLE ili GROUPED
        Outgoing currentOutgoing = new Outgoing();
        currentOutgoing.setTotalNumOfProd(currentOutgoingGrouped.getTotalNumOfProds());
        currentOutgoing.setOutgoingDetails(currentOutgoingGrouped.getOutgoingDetailsList());
        currentOutgoing.setOutgoingIDList(currentOutgoingGrouped.getOutgoingIDList());
        setupCurrentOutgoingStyle(EnumOutgoingStyle.GROUPED);
        setupCurrentOutgoing(currentOutgoing);
        outgoingPhaseTwoRepository.registerRealTimeUpdatesOutgoingDetailsGrouped(currentOutgoingGrouped);
    }


}
