package com.example.wms_app.viewmodel.outgoing.phaseone;

import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.wms_app.R;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumAdditionType;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.model.WarehouseStatusPosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.repository.outgoing.phaseone.OutgoingPhaseOneRepository;
import com.example.wms_app.repository.data.ProductBoxRepository;
import com.example.wms_app.repository.data.WarehousePositionRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.utilities.mapper.AsyncMapperPhaseOne;
import com.example.wms_app.utilities.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhaseOneViewModel extends AndroidViewModel {
    private final Resources resources;
    private final OutgoingPhaseOneRepository outgoingPhaseOneRepository;
    private final WarehousePositionRepository warehousePositionRepository;
    private final ProductBoxRepository productBoxRepository;
    private LiveData<List<ProductBox>> mFilteredProductBoxListForSpinner;
    private MutableLiveData<Integer> requestFocusViewID;
    private final int employeeIDDb;

    public PhaseOneViewModel(@NonNull Application application) {
        super(application);
        resources = application.getResources();
        outgoingPhaseOneRepository = new OutgoingPhaseOneRepository(application.getApplicationContext());
        warehousePositionRepository = new WarehousePositionRepository(application.getApplicationContext());
        productBoxRepository = new ProductBoxRepository(application.getApplicationContext());
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();
    }

    public LiveData<ApiResponse> getApiResponseLiveData() {
        return outgoingPhaseOneRepository.getResponseMutableLiveData();
    }

    public LiveData<List<OutgoingDetailsResult>> getOutgoingDetailsResultLiveData() {
        return outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData();
    }

    /**
     * Integer je ovde ID View-a koje je potrebno fokusirati. Koristi se za focus na kolicinu
     * ili serijski broj EditText nakon skeniranja/odabira artikla
     *
     * @return requestFocusViewID
     */
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
        return outgoingPhaseOneRepository.getViewEnableHelperLiveData();
    }

    public LiveData<ProductBox> getScannedProductBoxLiveData() {
        return outgoingPhaseOneRepository.getScannedProductBoxMutLiveData();
    }

    public LiveData<String> getPositionBarcode() {
        return outgoingPhaseOneRepository.getPositionBarcodeMutLiveData();
    }

    public LiveData<List<ProductBox>> getProductBoxListMediatorLiveData() {
        //DANIEL IZMENA
        if (mFilteredProductBoxListForSpinner == null) {
            mFilteredProductBoxListForSpinner = AsyncMapperPhaseOne.getProductBoxForSpinnerPhaseOne(
                    outgoingPhaseOneRepository.getWarehouseStatusPositionWithArticlesDetails(),
                    outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData(),
                    outgoingPhaseOneRepository.getPreloadingStatusPosDetailsFilteredMutLive(),
                    this::getProductBoxFromWPosDetails
                    //this::changeColorAndQuantities
            );
        }
        return mFilteredProductBoxListForSpinner;
    }


    private List<ProductBox> getProductBoxFromWPosDetails() {
        List<WarehouseStatusPositionDetails> listWithArticlesOnPos = outgoingPhaseOneRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();

        if (listWithArticlesOnPos == null || listWithArticlesOnPos.isEmpty()) {
            List<ProductBox> productBoxList = new ArrayList<>();
            productBoxList.add(ProductBox.newPlaceHolderInstance());
            return productBoxList;
        }
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<WarehouseStatusPositionDetails> preloadingPositionsList = outgoingPhaseOneRepository.getPreloadingStatusPosDetailsFilteredMutLive().getValue();
        Outgoing currentOutgoing = outgoingPhaseOneRepository.getCurrentOutgoing().getValue();

        List<ProductBox> list = listWithArticlesOnPos.stream().map(warehouseStatusPositionDetails ->
                getProductBoxDataFromDb(warehouseStatusPositionDetails, tempList, preloadingPositionsList, currentOutgoing)).collect(Collectors.toList());

          /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(ProductBox::getProductBoxName));
        return list;
    }

    private ProductBox getProductBoxDataFromDb(WarehouseStatusPositionDetails warehouseStatusPositionDetails, List<OutgoingDetailsResult> tempList, List<WarehouseStatusPositionDetails> preloadingPositionsList, Outgoing outgoing) {
        ProductBox pb = productBoxRepository.getProductBoxByID(warehouseStatusPositionDetails.getProductBoxID());

        if (pb == null)
            return null;

        int addedQuantityInTemp;
        int addedQuantityOnPreloadingPos;
        int expectedQuantity;

        addedQuantityInTemp = tempList == null ? 0 :
                (int) tempList.stream()
                        .filter(k -> k.getProductBoxID() == warehouseStatusPositionDetails.getProductBoxID())
                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                        .sum();

        addedQuantityOnPreloadingPos = preloadingPositionsList == null ? 0 :
                (int) preloadingPositionsList.stream()
                        .filter(m -> m.getProductBoxID() == warehouseStatusPositionDetails.getProductBoxID())
                        .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                        .sum();

        expectedQuantity = outgoing == null ? 0 :
                (int) outgoing.getOutgoingDetails().stream()
                        .filter(k -> k.getProductBoxID() == pb.getProductBoxID())
                        .mapToDouble(OutgoingDetails::getQuantity)
                        .sum();

        ProductBox prodBox = new ProductBox(
                pb.getProductBoxID(),
                pb.getProductBoxName(),
                pb.getProductBoxBarcode(),
                pb.isSerialMustScan(),
                expectedQuantity,
                addedQuantityInTemp + addedQuantityOnPreloadingPos,
                pb.getProductBoxCode()

        );

        //Postavljanje colorstatus zbog bojenja u spinneru
        if (addedQuantityInTemp + addedQuantityOnPreloadingPos >= expectedQuantity) {
            prodBox.setColorStatus(2);
        } else {
            prodBox.setColorStatus(0);
        }

        return prodBox;

    }

    private List<ProductBox> changeColorAndQuantities(List<ProductBox> productBoxList) {

        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<WarehouseStatusPositionDetails> preloadingPositionsList = outgoingPhaseOneRepository.getPreloadingStatusPosDetailsFilteredMutLive().getValue();
        Outgoing currentOutgoing = outgoingPhaseOneRepository.getCurrentOutgoing().getValue();
        for (ProductBox productBox : productBoxList) {

            try {
                if (productBox.getProductBoxID() == -1) {
                    productBox.setColorStatus(0);
                    productBox.setExpectedQuantity(0);
                    productBox.setAddedQuantity(0);
                    continue;
                }

                int addedQuantityInTemp;
                int addedQuantityOnPreloadingPos;
                int expectedQuantity;

                addedQuantityInTemp = tempList == null ? 0 :
                        (int) tempList.stream()
                                .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                                .mapToDouble(OutgoingDetailsResult::getQuantity)
                                .sum();

                addedQuantityOnPreloadingPos = preloadingPositionsList == null ? 0 :
                        (int) preloadingPositionsList.stream()
                                .filter(m -> m.getProductBoxID() == productBox.getProductBoxID())
                                .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                                .sum();

                expectedQuantity = currentOutgoing == null ? 0 :
                        (int) currentOutgoing.getOutgoingDetails().stream()
                                .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                                .mapToDouble(OutgoingDetails::getQuantity)
                                .sum();

                productBox.setAddedQuantity(addedQuantityInTemp + addedQuantityOnPreloadingPos);
                productBox.setExpectedQuantity(expectedQuantity);

                //Postavljanje colorstatus zbog bojenja u spinneru
                if (addedQuantityInTemp + addedQuantityOnPreloadingPos >= expectedQuantity) {
                    productBox.setColorStatus(2);
                } else {
                    productBox.setColorStatus(0);
                }

            } catch (ConcurrentModificationException exception) {
                Utility.writeErrorToFile(exception);
            }
        }

        return productBoxList;
    }

    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewFromTempList() {
        return AsyncMapperPhaseOne.getTempListPreview(
                outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData(),
                this::getOutgoingDetailsResultPreviewFromTempListInput
        );
    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewFromTempListInput() {
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<OutgoingDetailsResultPreview> list = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue().stream().map(this::getPreviewValuesForTempList).collect(Collectors.toList());
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
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

    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewFromOutgoing() {
        return AsyncMapperPhaseOne.getOutgoingPhaseOnePreview(
                outgoingPhaseOneRepository.getCurrentOutgoing(),
                this::getOutgoingDetailsResultPreviewFromOutgoingInput
        );
    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewFromOutgoingInput() {
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<WarehouseStatusPositionDetails> preloadingPositionList = outgoingPhaseOneRepository.getPreloadingStatusPosDetailsFilteredMutLive().getValue();
        List<OutgoingDetailsResultPreview> list = outgoingPhaseOneRepository.getCurrentOutgoing().getValue().getOutgoingDetails().stream().map(m -> getPreviewValuesForOutgoingDetails(m, tempList, preloadingPositionList)).collect(Collectors.toList());

           /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(OutgoingDetailsResultPreview::getProductBoxName));
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
        return list;
    }

    private OutgoingDetailsResultPreview getPreviewValuesForOutgoingDetails(OutgoingDetails outgoingDetails,
                                                                            List<OutgoingDetailsResult> tempList,
                                                                            List<WarehouseStatusPositionDetails> preloadingPositionList) {

        ProductBox pb = productBoxRepository.getProductBoxByID(outgoingDetails.getProductBoxID());

        if (pb == null)
            return null;

        int addedQtyInTemp = (int) tempList
                .stream()
                .filter(x -> x.getProductBoxID() == outgoingDetails.getProductBoxID())
                .mapToDouble(OutgoingDetailsResult::getQuantity).sum();

        int addedQuantityOnPreloadingPos = preloadingPositionList == null ? 0 :
                (int) preloadingPositionList.stream()
                        .filter(x -> x.getProductBoxID() == outgoingDetails.getProductBoxID())
                        .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                        .sum();

        int colorStatus = addedQtyInTemp + addedQuantityOnPreloadingPos >= outgoingDetails.getQuantity() ? 2 : 0;
        //U PhaseOnePreviewAdapter-u ima oznaceno kako je sta mapirano
        return new OutgoingDetailsResultPreview(
                (addedQtyInTemp + addedQuantityOnPreloadingPos),
                pb.getProductBoxName(),
                pb.getProductBoxCode(),
                colorStatus,
                (int) outgoingDetails.getQuantity(),
                outgoingDetails.getProductBoxID()
        );

    }

    public LiveData<List<OutgoingDetailsResultPreview>> getOutgoingDetailsResultPreviewFromPreloading() {
        return AsyncMapperPhaseOne.getPreloadingPositionPreview(
                outgoingPhaseOneRepository.getWarehouseStatusPositionMutableLiveData(),
                this::getOutgoingDetailsResultPreviewFromPreloadingInput
        );
    }

    private List<OutgoingDetailsResultPreview> getOutgoingDetailsResultPreviewFromPreloadingInput() {
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.loading());
        List<WarehouseStatusPosition> prelaodingPositions = outgoingPhaseOneRepository.getWarehouseStatusPositionMutableLiveData().getValue();
        if (prelaodingPositions == null) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.preloading_positions_not_loaded_yet)));
            return null;
        }
        List<OutgoingDetailsResultPreview> list = outgoingPhaseOneRepository.getWarehouseStatusPositionMutableLiveData()
                .getValue()
                .stream()
                .flatMap(x -> x.getWspDetails().stream().map(m -> getPreviewValuesForPreloadingPositions(m, x.getWarehousePositionBarcode())))
                .collect(Collectors.toList());

           /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        if (list.stream().anyMatch(Objects::isNull)) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.outgoing_not_all_boxes_synced)));
            list.removeIf(Objects::isNull);
        }

        list.sort(Comparator.comparing(OutgoingDetailsResultPreview::getProductBoxName));
        outgoingPhaseOneRepository.getResponseMutableLiveData().postValue(ApiResponse.success());
        return list;
    }

    private OutgoingDetailsResultPreview getPreviewValuesForPreloadingPositions(WarehouseStatusPositionDetails wspDetails,
                                                                                String warehousePositionBarcode) {
        ProductBox pb = productBoxRepository.getProductBoxByID(wspDetails.getProductBoxID());
        if (pb == null)
            return null;

        return new OutgoingDetailsResultPreview(
                wspDetails.getQuantity(),
                pb.getProductBoxName(),
                pb.getProductBoxCode(),
                warehousePositionBarcode,
                wspDetails.getSerialNo()
        );
    }

    public void codeScanned(String scannedCode) {
        if (scannedCode != null) {

            if (scannedCode.length() == Constants.POSITION_BARCODE_LENGTH || scannedCode.length() == Constants.SUB_POSITION_BARCODE_LENGTH) {
                /* Znaci da se radi o poziciji
                ili podpoziciji */
                positionScanned(scannedCode);

            } else {
                //Znaci da se radi o artiklu
                articleScanned(scannedCode);
            }

        } else {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.wrong_barcode)));
        }
    }

    private void articleScanned(String scannedCode) {
        WarehousePosition currentWPosition = outgoingPhaseOneRepository.getCurrentWarehousePositionMutLiveData().getValue();
        if (currentWPosition == null) {
            /* Ne moze da se skenira artikal ako nije postavljena pozicija
             * */
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.position_not_picked)));
            return;
        }

        ProductBox scannedProductBox = getProductBoxListMediatorLiveData().getValue()
                .stream().filter(x -> x.getProductBoxBarcode().equals(scannedCode))
                .findAny()
                .orElse(null);
        if (scannedProductBox == null) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.scanned_prod_box_not_on_outgoing)));
            return;
        }
        if (scannedProductBox.isSerialMustScan()) {
            //Znaci da mora serisjki broj tj veliki artikal

            /* Cim je serialMustScan odmah kolicina
                            ide na 1 i disabluje se polje */
            //Trenutno nemamo evidenciju o serisjib brojevima. Zato se ovde postavlja prazan string incae bi se postavio serialNumber Ako se bude ubacila ovde ide kod TODO Serijski broj, serial number, sr num
            //Za sada samo iskace greska da mora da se unese sr broj kako bi blokiralo dalje skeniranje
            outgoingPhaseOneRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
        } else {
            //Znaci da ne mora serisjki broj tj mali artikal
            outgoingPhaseOneRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);
            addSmallProductToTempList(currentWPosition.getBarcode(), scannedProductBox, "", 1, EnumAdditionType.SCANNING, true);
        }
    }

    private void positionScanned(String scannedCode) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        WarehousePosition warehousePosition = isPositionByBarcodeValid(scannedCode);
        if (warehousePosition == null) {
            return;
        }

        if (warehousePosition.isForPreloading()) {
            /*Znaci da je pozicija za predutovar*/
            if (isTempListEmpty()) {
                outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
                return;
            }
            outgoingPhaseOneRepository.updateWarehouseStatusForPreloading(warehousePosition.getWPositionID(), warehousePosition.getBarcode());
        } else {
            /*Znaci da je pozicija sa artiklima*/
            outgoingPhaseOneRepository.getCurrentWarehousePositionMutLiveData().setValue(warehousePosition);
            outgoingPhaseOneRepository.getPositionBarcodeMutLiveData().setValue(warehousePosition.getBarcode());
            outgoingPhaseOneRepository.getProductBoxesOnPosition(warehousePosition.getBarcode(), employeeID);
        }
    }


    public void positionWithArticlesSelected(String scannedCode) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        WarehousePosition warehousePosition = isPositionByBarcodeValid(scannedCode);
        if (warehousePosition == null) {
            return;
        }
        if (warehousePosition.isForPreloading()) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.field_only_for_with_articles_pos)));
            outgoingPhaseOneRepository.resetProductSpinner();
            return;
        }

        outgoingPhaseOneRepository.getCurrentWarehousePositionMutLiveData().setValue(warehousePosition);
        outgoingPhaseOneRepository.getPositionBarcodeMutLiveData().setValue(warehousePosition.getBarcode());
        outgoingPhaseOneRepository.getProductBoxesOnPosition(warehousePosition.getBarcode(), employeeID);

    }

    public void positionForPreloadingSelected(String scannedCode) {
        WarehousePosition warehousePosition = isPositionByBarcodeValid(scannedCode);
        if (warehousePosition == null) {
            return;
        }
        if (!warehousePosition.isForPreloading()) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.field_only_for_preloading_pos)));
            outgoingPhaseOneRepository.resetProductSpinner();
            return;
        }
        if (isTempListEmpty()) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
            return;
        }
        outgoingPhaseOneRepository.updateWarehouseStatusForPreloading(warehousePosition.getWPositionID(), warehousePosition.getBarcode());
    }

    private boolean isTempListEmpty() {
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        return tempList == null || tempList.isEmpty();
    }

    private WarehousePosition isPositionByBarcodeValid(String scannedCode) {

        /* Dobijanje pozicije na osnovu skeniranog barkoda*/
        WarehousePosition warehousePosition = warehousePositionRepository.getWarehousePositionByBarcode(scannedCode);
        if (warehousePosition == null) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_exist_or_not_for_outgoing)));
            outgoingPhaseOneRepository.resetProductSpinner();
            return null;
        }
        if (!warehousePosition.isForOutgoing()) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_defined_for_outgoing)));
            outgoingPhaseOneRepository.resetProductSpinner();
            return null;
        }

        return warehousePosition;
    }


    public void registerFirebaseRealTimeUpdatesOutgoing(Outgoing outgoing) {
        outgoingPhaseOneRepository.registerRealTimeUpdatesOutgoingSingle(outgoing);

    }

    public void refreshApiResponseStatus() {
        outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public void setupCurrentOutgoing(Outgoing currentOutgoing) {
        if (currentOutgoing != null) {
            outgoingPhaseOneRepository.getCurrentOutgoing().setValue(currentOutgoing);
        } else
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.error_loading_current_outgoing_leave)));

    }

    public LiveData<Boolean> toggleUndoAndRefreshBtnLiveData() {
        return Transformations.map(getOutgoingDetailsResultLiveData(), List::isEmpty);
    }

    public void removeFirebaseRealTimeListener() {
        outgoingPhaseOneRepository.removeFirebaseRealTimeListener();
    }

    public void registerFirebaseRealTimeUpdatesPreloadingPos() {
        outgoingPhaseOneRepository.registerRealTimeUpdatesPreloadingPos();
    }

    public void removeAllFromTempList() {
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null) {
            tempList.clear();
            outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_all_from_temp_list)));
        }
    }

    public void removeLastFromTempList() {
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(tempList.size() - 1);
            outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_last_from_temp_list)));
        }

    }

    public void productBoxManuallySelected(ProductBox selectedProductBox) {
        if (selectedProductBox.isSerialMustScan()) {
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSerialEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneAddNoSrNumBtn, "", true, EnumViewType.BUTTON, View.VISIBLE);
            requestFocusViewID.setValue(R.id.outgoingPhaseOneSerialEt);
        } else {
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSerialEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.GONE);
        }
        //Ovo za ocekivanu  i dodatu kolicinu ide ovde posto se postavlja u svakom slucaju
        outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneQuantityEt, resources.getString(R.string.added_and_expected_qty, selectedProductBox.getAddedQuantity(), selectedProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
        outgoingPhaseOneRepository.getCurrentProductBox().setValue(selectedProductBox);
    }

    public void refreshExpectedQty(ProductBox productBox) {
        if (productBox != null) {
            if (productBox.getProductBoxID() == -1) {
                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneQuantityEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            } else {
                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneQuantityEt, resources.getString(R.string.added_and_expected_qty, productBox.getAddedQuantity(), productBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            }
            outgoingPhaseOneRepository.getCurrentProductBox().setValue(productBox);
        }
    }

    public void addProductBoxManually(ProductBox currentProductBox, String serialNumber, String quantity) {

        WarehousePosition currentWPosition = outgoingPhaseOneRepository.getCurrentWarehousePositionMutLiveData().getValue();
        if (currentWPosition == null) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.position_not_picked)));
            return;
        }

        if (currentProductBox == null || currentProductBox.getProductBoxID() == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.product_not_selected_from_list)));
            return;
        }

        if (quantity.isEmpty()) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_not_inserted)));
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_bad_format)));
            return;
        }

        if (qty <= 0) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_must_be_over_zero)));
            return;
        }

        if (currentProductBox.isSerialMustScan()) {
            if (!serialNumber.isEmpty()) {
                addBigProductToTempList(currentWPosition.getBarcode(), currentProductBox, serialNumber, qty, false);
            } else {
                outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
            }

        } else {
            addSmallProductToTempList(currentWPosition.getBarcode(), currentProductBox, "", qty, EnumAdditionType.MANUALLY, false);
        }

    }

    private void addBigProductToTempList(String wPositionBarcode, ProductBox selectedProductBox, String serialNumber, int quantity, boolean isScanned) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        /*Mora ovako posto ne moze u lambda expression da se prosledi selectedProduct objekat
         * */
        int selectedProductBoxID = selectedProductBox.getProductBoxID();
        List<WarehouseStatusPositionDetails> articlesOnCurrentPosList = outgoingPhaseOneRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();

        if (articlesOnCurrentPosList.stream()
                .noneMatch(x -> x.getSerialNo() != null && x.getSerialNo().equals(serialNumber) &&
                        x.getProductBoxID() == selectedProductBoxID)) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_not_exist_on_pos)));
            return;
        }
            /*Sada je moguce dodati 2 artikla sa istim serijskim brojem i istim IDjem. Tako da se ovde proerava kolicina kao kod malih artikla.
            Ako je sve ok ide dodavanje u listi. U temp listi to ce biti 2 objekta ali na firebase ce se kolicina grupisati.
            * */
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
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

        if (addedQuantityInTemp + quantity <= quantityOnPosition) {

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

                outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                //Resetovanje polja za serijski broj
                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSerialEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);


            } else {
                //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                int totalAddedQty = addedQuantityInTemp + quantity;
                outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
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

                                outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                //Resetovanje polja za serijski broj
                                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSerialEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                            }
                        }));
            }

        } else {
            /* Znaci da je uneta kolicina vece od one koja je na poziciji
             *  */
            int totalAddedQty = addedQuantityInTemp + quantity;
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
        }
    }

    private void addSmallProductToTempList(String wPositionBarcode, ProductBox selectedProductBox, String serialNumber, int quantity, EnumAdditionType enumAdditionType, boolean isScanned) {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        int selectedProductBoxID = selectedProductBox.getProductBoxID();
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        List<WarehouseStatusPositionDetails> articlesOnCurrentPosList = outgoingPhaseOneRepository.getWarehouseStatusPositionWithArticlesDetails().getValue();

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


        /*
        Ova provera se samo vrsi kada se rucno dodaje artikal*/
        if (enumAdditionType == EnumAdditionType.MANUALLY) {
            if (tempList.size() > 0) {
                OutgoingDetailsResult lastAddedProd = tempList.get(tempList.size() - 1);
                if (lastAddedProd.getProductBoxID() == selectedProductBoxID) {
                    if (addedQuantityInTemp + quantity - (int) lastAddedProd.getQuantity() <= quantityOnPosition) {

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
                            outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                        } else {
                            //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                            //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                            int totalAddedQty = addedQuantityInTemp + quantity;
                            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
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

                                            outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                            outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                                        }
                                    }));
                        }


                    } else {
                        /* Znaci da je uneta kolicina vece od one koja je na poziciji
                         *  */
                        int totalAddedQty = addedQuantityInTemp + (int) quantity - (int) lastAddedProd.getQuantity();
                        outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
                                ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
                    }
                    return;
                }
            }
        }
        if (addedQuantityInTemp + quantity <= quantityOnPosition) {
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
                outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

            } else {
                //Znaci da je potrebno da se prikaze obavestenje i parametar isReservedQtyPromptAsked se stavlja na true
                //da bi kasnije preko tog polja mogao da kontrolisem update na WarehouseStatusPos na firebaseu
                int totalAddedQty = addedQuantityInTemp + quantity;
                outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
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
                                outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
                                outgoingPhaseOneRepository.toggleViewEnabledAndText(R.id.outgoingPhaseOneSelectedQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);

                            }
                        }));

            }
        } else {
            /* Znaci da je uneta kolicina vece od one koja je na poziciji
             *  */
            int totalAddedQty = addedQuantityInTemp + quantity;
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.error(resources.getString(R.string.added_qty_greater_than_expected, quantityOnPosition, totalAddedQty)));
        }
    }

//    /**
//     * Metoda koja kreira objekat OutgoingDetailsResult.
//     * Poziva se kada se mali ili veliki artikal dodaju u tempListu
//     *
//     * @param productBoxID
//     * @param serialNumber
//     * @param quantity
//     * @param wPositionBarcode barkod pozicije sa koje se artikal skida
//     * @return outgoingDetailsResult
//     */
//    private OutgoingDetailsResult getOutgoingDetailsResultToAdd(int productBoxID, String serialNumber,
//                                                                int quantity, String wPositionBarcode,
//                                                                boolean isScanned, boolean isReservedQtyPromptAsked) {
//        OutgoingDetailsResult outgoingDetailsResult = new OutgoingDetailsResult();
//        outgoingDetailsResult.setProductBoxID(productBoxID);
//        outgoingDetailsResult.setSerialNo(serialNumber);
//        outgoingDetailsResult.setQuantity(quantity);
//        outgoingDetailsResult.setwPositionBarcode(wPositionBarcode);
//        outgoingDetailsResult.setScanned(isScanned);
//        outgoingDetailsResult.setReserveQtyPromptAsked(isReservedQtyPromptAsked);
//        return outgoingDetailsResult;
//    }

    /**
     * Unlock positions.
     */
    public void unlockPositions() {
        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }
        outgoingPhaseOneRepository.unlockAllPositions(employeeID);
    }

    public void resetPositionBarcode() {
        outgoingPhaseOneRepository.resetPositionBarcode();
    }

    public void deleteProductBoxFromTempList(int position) {
        List<OutgoingDetailsResult> tempList = outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(position);
            outgoingPhaseOneRepository.getOutgoingDetailsResultMutLiveData().setValue(tempList);
        } else {
            outgoingPhaseOneRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_temp_list)));
        }
    }
}
