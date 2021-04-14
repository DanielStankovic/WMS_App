package com.example.wms_app.viewmodel.incoming.production;

import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;

import com.example.wms_app.R;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumAdditionType;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetails;
import com.example.wms_app.model.IncomingDetailsResult;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.repository.incoming.production.IncomingProductionRepository;
import com.example.wms_app.repository.data.ProductBoxRepository;
import com.example.wms_app.repository.data.WarehousePositionRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.utilities.mapper.AsyncMapper;
import com.example.wms_app.utilities.Constants;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class IncomingProductionViewModel extends AndroidViewModel {

    private Resources resources;
    private IncomingProductionRepository incomingProductionRepository;
    private ProductBoxRepository productBoxRepository;
    private WarehousePositionRepository warehousePositionRepository;
    private MediatorLiveData<Integer> totalNumberOfAddedProdMediatorLiveData;
    private LiveData<List<IncomingDetailsResult>> incomingDetailsResultLiveData;
    private LiveData<List<ProductBox>> productBoxListMediatorLiveData;
    private LiveData<List<ProductBox>> productBoxListLeftMediatorLiveData;
    private MutableLiveData<Integer> requestFocusViewID;
    private final int employeeIDDb;

    public IncomingProductionViewModel(@NonNull Application application) {
        super(application);
        resources = application.getResources();
        incomingProductionRepository = new IncomingProductionRepository(application.getApplicationContext());
        productBoxRepository = new ProductBoxRepository(application.getApplicationContext());
        warehousePositionRepository = new WarehousePositionRepository(application.getApplicationContext());
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();

    }

    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        return incomingProductionRepository.getResponseMutableLiveData();
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


    public void refreshApiResponseStatus() {
        incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public LiveData<Integer> getTotalNumberOfProdLiveData() {
        return incomingProductionRepository.getTotalNumberOfProdMutLiveData();
    }

    public MediatorLiveData<Integer> getTotalNumberOfAddedProdMediatorLiveData() {
        if (totalNumberOfAddedProdMediatorLiveData == null) {
            totalNumberOfAddedProdMediatorLiveData = new MediatorLiveData<>();
            totalNumberOfAddedProdMediatorLiveData.addSource(incomingProductionRepository.getIncomingDetailsResultMutLiveData(),
                    value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            totalNumberOfAddedProdMediatorLiveData.addSource(incomingProductionRepository.getHashMapDetailsResultMutableLiveData(),
                    value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
        }
        return totalNumberOfAddedProdMediatorLiveData;
    }

    private Integer sumTotalAddedQuantity() {
        int totalSum;

        //Kolicina koja se nalazi u temp listi
        int quantityInTemp = (int) incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue().stream()
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();
        int quantityOnFb;

        //Ovde se sabiraju vrednost iz svih lsiti koje se nalaze u hashmapi
        quantityOnFb = (int) incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue().values()
                .stream()
                .flatMap(Collection::stream)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();
        totalSum = quantityInTemp + quantityOnFb;
        return totalSum;
    }


    public LiveData<List<IncomingDetailsResult>> getIncomingDetailsResultLiveData() {
        incomingDetailsResultLiveData = incomingProductionRepository.getIncomingDetailsResultMutLiveData();
        return incomingDetailsResultLiveData;
    }

    public void registerRealTimeUpdates(String selectedProductionTypeCode) {
        incomingProductionRepository.registerRealTimeUpdates(selectedProductionTypeCode);
    }

    public void removeFirebaseRealTimeListener() {
        incomingProductionRepository.removeFirebaseRealTimeListener();
    }

    /**
     * Ovaj objekat se koristi kako bi Enable/Disable polja za unos kolicine i serijskog broja
     *
     * @return viewEnableHelperMutableLiveData
     */
    public LiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        return incomingProductionRepository.getViewEnableHelperLiveData();
    }

    public LiveData<Boolean> toggleUndoAndRefreshBtnLiveData() {
        return Transformations.map(incomingDetailsResultLiveData, List::isEmpty);
    }

    public LiveData<List<ProductBox>> getProductBoxListMediatorLiveData() {
        //OVDE IDE IZMENA DANIEL
        if (productBoxListMediatorLiveData == null) {
            productBoxListMediatorLiveData = AsyncMapper.getProductionProductBoxFromResult(
                    incomingProductionRepository.getUniqueProductBoxIDMutLiveData(),
                    incomingProductionRepository.getIncomingDetailsResultMutLiveData(),
                    incomingProductionRepository.getHashMapDetailsResultMutableLiveData(),
                    this::combineResultData
//                    new LoadingDialogInterface() {
//                        @Override
//                        public void showLoadingDialog() {
//                            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.loading());
//                        }
//
//                        @Override
//                        public void hideLoadingDialog() {
//                            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
//                        }
//                    }

                    // this::changeColorAndQuantities

            );
        }

        return productBoxListMediatorLiveData;
    }


    private List<ProductBox> combineResultData() {

        List<Integer> uniqueListOfIDs = incomingProductionRepository.getUniqueProductBoxIDMutLiveData().getValue();
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue();
        List<Incoming> incomingList = incomingProductionRepository.getIncomingListMutableLiveData().getValue();

        List<ProductBox> list = uniqueListOfIDs.stream().map(
                uniqueID -> getProductBoxFromIncomingDetails(uniqueID, tempList, hashMapFromFirebase, incomingList)).collect(Collectors.toList());

          /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
           DORADJENO : Ovde se samo sklanjaju nullovi a provera s evrsi kada se udje u formu
         */

        list.removeIf(Objects::isNull);

//        if (list.stream().anyMatch(Objects::isNull)) {
//            incomingProductionRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.incoming_not_all_boxes_synced)));
//            list.removeIf(Objects::isNull);
//        }

        list.sort(Comparator.comparing(ProductBox::getProductBoxName));
        list.add(0, ProductBox.newPlaceHolderInstance());

        return list;
    }

    private ProductBox getProductBoxFromIncomingDetails(Integer uniqueID,
                                                        List<IncomingDetailsResult> tempList,
                                                        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase,
                                                        List<Incoming> incomingList) {

        ProductBox pb = productBoxRepository.getProductBoxByID(uniqueID);

        if (pb == null)
            return null;

        ProductBox prodBox = new ProductBox(
                pb.getProductBoxID(),
                pb.getProductBoxName(),
                pb.getProductBoxBarcode(),
                pb.isSerialMustScan(),
                0,
                0,
                pb.getProductBoxCode()
        );

        setColorAndQuantityOnProdBox(prodBox, tempList, hashMapFromFirebase, incomingList);

//        ProductBox prodBox = new ProductBox();
//        prodBox.setProductBoxID(pb.getProductBoxID());
//        prodBox.setProductBoxName(pb.getProductBoxName());
//        prodBox.setProductBoxBarcode(pb.getProductBoxBarcode());
//        prodBox.setExpectedQuantity(expectedQuantity);
//        prodBox.setAddedQuantity(quantityInTemp + quantityOnFb);
//        prodBox.setSerialMustScan(pb.isSerialMustScan());
//        if (quantityInTemp + quantityOnFb > expectedQuantity) {
//            prodBox.setColorStatus(1);
//        } else if (quantityInTemp + quantityOnFb == expectedQuantity) {
//            prodBox.setColorStatus(2);
//        } else {
//            prodBox.setColorStatus(0);
//        }
        return prodBox;
    }

    private List<ProductBox> changeColorAndQuantities(List<ProductBox> productBoxList) {
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue();
        List<Incoming> incomingList = incomingProductionRepository.getIncomingListMutableLiveData().getValue();

        for (ProductBox productBox : productBoxList) {
            setColorAndQuantityOnProdBox(productBox, tempList, hashMapFromFirebase, incomingList);
        }
        return productBoxList;
    }

    private void setColorAndQuantityOnProdBox(ProductBox productBox,
                                              List<IncomingDetailsResult> tempList,
                                              HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase,
                                              List<Incoming> incomingList) {
        try {


            int expectedQuantity = (int) incomingList.stream().flatMap(x -> x.getIncomingDetails().stream())
                    .filter(incomingDetails -> incomingDetails.getProductBoxID() == productBox.getProductBoxID())
                    .mapToDouble(IncomingDetails::getQuantity)
                    .sum();


            int quantityInTemp = (int) tempList.stream()
                    .filter(x -> x.getProductBoxID() == productBox.getProductBoxID())
                    .mapToDouble(IncomingDetailsResult::getQuantity)
                    .sum();


            int quantityOnFb = (int) hashMapFromFirebase.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(x -> x.getProductBoxID() == productBox.getProductBoxID())
                    .mapToDouble(IncomingDetailsResult::getQuantity)
                    .sum();

            productBox.setAddedQuantity(quantityInTemp + quantityOnFb);
            productBox.setExpectedQuantity(expectedQuantity);

            //Postavljanje colorstatus zbog bojenja u spinneru
            if (quantityInTemp + quantityOnFb > expectedQuantity) {
                productBox.setColorStatus(1);
            } else if (quantityInTemp + quantityOnFb == expectedQuantity) {
                productBox.setColorStatus(2);
            } else {
                productBox.setColorStatus(0);
            }
        } catch (ConcurrentModificationException exception) {
            Utility.writeErrorToFile(exception);
        }
    }

    public void refreshExpectedQty(ProductBox productBox) {
        if (productBox != null) {
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionLeftQtyEt, resources.getString(R.string.added_and_expected_qty, productBox.getAddedQuantity(), productBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.getCurrentProductBox().setValue(productBox);
        }
    }

    public void productBoxManuallySelected(ProductBox selectedProductBox) {

        //Provera ako je selektovan prvi u listi artikal koji je placeholder

        if (selectedProductBox.getProductBoxID() == -1) {
            //Ovo za ocekivanu  i dodatu kolicinu ide ovde posto se postavlja u svakom slucaju
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionLeftQtyEt, resources.getString(R.string.added_and_expected_qty, selectedProductBox.getAddedQuantity(), selectedProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.getCurrentProductBox().setValue(selectedProductBox);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionSrNumberEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.GONE);

            return;
        }


        if (selectedProductBox.isSerialMustScan()) {
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionAddNoSrNumBtn, "", true, EnumViewType.BUTTON, View.VISIBLE);
            requestFocusViewID.setValue(R.id.incomingProductionSrNumberEt);
        } else {
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionSrNumberEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.GONE);
        }
        //Ovo za ocekivanu  i dodatu kolicinu ide ovde posto se postavlja u svakom slucaju
        incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionLeftQtyEt, resources.getString(R.string.added_and_expected_qty, selectedProductBox.getAddedQuantity(), selectedProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
        incomingProductionRepository.getCurrentProductBox().setValue(selectedProductBox);
    }

    public LiveData<ProductBox> getScannedProductBoxLiveData() {
        return incomingProductionRepository.getScannedProductBoxMutLiveData();

    }

    public void codeScanned(String scannedCode, boolean isReserved) {
        if (scannedCode != null) {

            if (scannedCode.length() == Constants.POSITION_BARCODE_LENGTH || scannedCode.length() == Constants.SUB_POSITION_BARCODE_LENGTH) {
                /* Znaci da se radi o poziciji
                ili podpoziciji */
                positionScanned(scannedCode);

            } else {
                //Znaci da se radi o artiklu
                articleScanned(scannedCode, isReserved);
            }

        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.wrong_barcode)));
        }

    }

    private void positionScanned(String scannedCode) {
        /*Provera da li postoji nesto u temp listi pre postavljanja
         * */
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList.size() > 0) {
//            WarehousePosition warehousePosition = warehousePositionRepository.getWarehousePosMutableLiveData().getValue()
//                    .stream().filter(x -> x.getBarcode().equals(scannedCode) && x.isForIncoming())
//                    .findAny()
//                    .orElse(null);
            WarehousePosition warehousePosition = warehousePositionRepository.getWarehousePositionByBarcode(scannedCode);

            /*Provera da li postoji skenirana pozicija u sistemu. Na loginu je namesteno da se povlace pozicije koje su definisane
            samo za logovanog korisnika
            * */
            if (warehousePosition != null) {

                if (warehousePosition.isForIncoming()) {

                    incomingProductionRepository.getWarehousePositionFromFirebase(tempList, warehousePosition.getBarcode());
//                    incomingProductionRepository.pushTempListToFirebase(tempList, warehousePosition.getWPositionID(), warehousePosition.getBarcode());
                } else {
                    incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_defined_for_incoming)));
                }
            } else {
                incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_exist)));
            }
        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
        }
    }

    private void articleScanned(String scannedCode, boolean isReserved) {

        ProductBox scannedProductBox = getProductBoxListMediatorLiveData().getValue()
                .stream().filter(x -> x.getProductBoxBarcode().equals(scannedCode))
                .findAny()
                .orElse(null);
        if (scannedProductBox == null) {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.scanned_prod_box_not_on_inc)));
            return;
        }

        if (scannedProductBox.isSerialMustScan()) {
            //Znaci da mora serisjki broj tj veliki artikal

            /* Cim je serialMustScan odmah kolicina
                            ide na 1 i disabluje se polje */
            //  incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //Trenutno nemamo evidenciju o serisjib brojevima. Zato se ovde postavlja prazan string incae bi se postavio serialNumber Ako se bude ubacila ovde ide kod TODO Serijski broj, serial number, sr num
            //  incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingProductionRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);


        } else {

            //Znaci da ne mora serisjki broj tj mali artikal
            incomingProductionRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);
            addSmallProductToTempList(scannedProductBox, "", 1, EnumAdditionType.SCANNING, true, isReserved);
        }
    }


    public void addProductBoxManually(ProductBox currentProductBox, String serialNumber, String quantity, boolean isReserved) {

        if (currentProductBox != null && currentProductBox.getProductBoxID() != -1) {

            if (!quantity.isEmpty()) {
                int qty = -1;
                try {
                    qty = Integer.parseInt(quantity);
                } catch (NumberFormatException e) {
                    incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_bad_format)));
                    return;
                }
                if (qty > 0) {

                    if (currentProductBox.isSerialMustScan()) {
                        if (!serialNumber.isEmpty()) {
                            addBigProductToTempList(currentProductBox, serialNumber, qty, false, isReserved);
                        } else {
                            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
                        }

                    } else {
                        addSmallProductToTempList(currentProductBox, "", qty, EnumAdditionType.MANUALLY, false, isReserved);
                    }

                } else {
                    incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_must_be_over_zero)));
                }
            } else {
                incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_not_inserted)));
            }

        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.product_not_selected_from_list)));
        }

    }


    private void addBigProductToTempList(ProductBox selectedProductBox, String serialNumber, int quantity, boolean isScanned, boolean isReserved) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        /*Mora ovako posto ne moze u lambda expression da se prosledi selectedProduct objekat
         * */
        int selectedProductBoxID = selectedProductBox.getProductBoxID();


            /*Sada je moguce dodati 2 artikla sa istim serijskim brojem i istim IDjem. Tako da se ovde proerava kolicina kao kod malih artikla.
            Ako je sve ok ide dodavanje u listi. U temp listi to ce biti 2 objekta ali na firebase ce se kolicina grupisati.
            * */
        List<Incoming> incomingList = incomingProductionRepository.getIncomingListMutableLiveData().getValue();
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue();

        // String incomingID = incomingProductionRepository.getSelectedIncomingMutLiveData().getValue().getIncomingID();

        int expectedQuantity = (int) incomingList.stream().flatMap(x -> x.getIncomingDetails().stream())
                .filter(incomingDetails -> incomingDetails.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetails::getQuantity)
                .sum();

        int quantityOnFb = (int) hashMapFromFirebase.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        int quantityInTempList = (int) Objects.requireNonNull(tempList)
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        if (quantityInTempList + (int) quantity + quantityOnFb <= expectedQuantity) {

            tempList.add(new IncomingDetailsResult(
                    new Date(),
                    employeeID,
                    true,
                    selectedProductBoxID,
                    quantity,
                    isScanned,
                    false,
                    serialNumber,
                    isReserved
            ));

            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //Resetovanje polja za serijski broj
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //setupAddedAndExpectedQuantity(selectedProductID);


        } else {
            int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFb;
            incomingProductionRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, expectedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tempList.add(new IncomingDetailsResult(
                                    new Date(),
                                    employeeID,
                                    true,
                                    selectedProductBoxID,
                                    quantity,
                                    isScanned,
                                    false,
                                    serialNumber,
                                    isReserved
                            ));

                            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                            //Resetovanje polja za serijski broj
                            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        }
                    }));
        }
    }

    private void addSmallProductToTempList(ProductBox selectedProductBox, String serialNumber, int quantity, EnumAdditionType enumAdditionType, boolean isScanned, boolean isReserved) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        int selectedProductBoxID = selectedProductBox.getProductBoxID();

        List<Incoming> incomingList = incomingProductionRepository.getIncomingListMutableLiveData().getValue();
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue();

        int expectedQuantity = (int) incomingList.stream().flatMap(x -> x.getIncomingDetails().stream())
                .filter(incomingDetails -> incomingDetails.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetails::getQuantity)
                .sum();

        int quantityOnFb = (int) hashMapFromFirebase.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        int quantityInTempList = (int) Objects.requireNonNull(tempList)
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        /*
        Ova provera se samo vrsi kada se rucno dodaje artikal*/
        if (enumAdditionType == EnumAdditionType.MANUALLY) {
            if (tempList.size() > 0) {
                IncomingDetailsResult lastAddedProd = tempList.get(tempList.size() - 1);
                if (lastAddedProd.getProductBoxID() == selectedProductBox.getProductBoxID()) {
                    if (quantityInTempList + (int) quantity + quantityOnFb - (int) lastAddedProd.getQuantity() <= expectedQuantity) {
                        tempList.remove(tempList.size() - 1);
                        tempList.add(new IncomingDetailsResult(
                                new Date(),
                                employeeID,
                                true,
                                selectedProductBoxID,
                                quantity,
                                isScanned,
                                false,
                                serialNumber,
                                isReserved
                        ));
                        incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                        incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        //  setupAddedAndExpectedQuantity(selectedProduct.getProductID());
                        return;
                    } else {
                        /* Znaci da je uneta kolicina vece od one koja je na poziciji
                         *  */
                        int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFb - (int) lastAddedProd.getQuantity();
                        incomingProductionRepository.getResponseMutableLiveData().setValue(
                                ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, expectedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        tempList.remove(tempList.size() - 1);
                                        tempList.add(new IncomingDetailsResult(
                                                new Date(),
                                                employeeID,
                                                true,
                                                selectedProductBoxID,
                                                quantity,
                                                isScanned,
                                                false,
                                                serialNumber,
                                                isReserved
                                        ));

                                        incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                                        incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                                    }
                                }));
                        return;
                    }
                }
            }
        }


        if (quantityInTempList + (int) quantity + quantityOnFb <= expectedQuantity) {

            tempList.add(new IncomingDetailsResult(
                    new Date(),
                    employeeID,
                    true,
                    selectedProductBoxID,
                    quantity,
                    isScanned,
                    false,
                    serialNumber,
                    isReserved
            ));

            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //setupAddedAndExpectedQuantity(selectedProductID);


        } else {
            int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFb;
            incomingProductionRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, expectedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tempList.add(new IncomingDetailsResult(
                                    new Date(),
                                    employeeID,
                                    true,
                                    selectedProductBoxID,
                                    quantity,
                                    isScanned,
                                    false,
                                    serialNumber,
                                    isReserved
                            ));

                            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                            incomingProductionRepository.toggleViewEnabledAndText(R.id.incomingProductionQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        }
                    }));
        }
    }

    public void removeAllFromTempList() {
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null) {
            tempList.clear();
            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
//            setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_all_from_temp_list)));

        }
    }

    public void removeLastFromTempList() {
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(tempList.size() - 1);
            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //  setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_last_from_temp_list)));
        }

    }

    public LiveData<List<IncomingDetailsResultLocal>> getIncomingDetailsFromTempList() {

        return AsyncMapper.getProductBoxFromResult(
                incomingProductionRepository.getIncomingDetailsResultMutLiveData(),
                this::getIncomingDetailsResultFromTempListInput
        );

        // return Transformations.map(incomingProductionRepository.getIncomingDetailsResultMutLiveData(), this::getIncomingDetailsResultFromTempListInput);
    }

    private List<IncomingDetailsResultLocal> getIncomingDetailsResultFromTempListInput() {
        return incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue().stream().map(this::getLocalValuesForTempList).collect(Collectors.toList());
    }

    private IncomingDetailsResultLocal getLocalValuesForTempList(IncomingDetailsResult incomingDetailsResult) {
        ProductBox pb = productBoxRepository.getProductBoxByID(incomingDetailsResult.getProductBoxID());

        return new IncomingDetailsResultLocal(
                (int) incomingDetailsResult.getQuantity(),
                pb.getProductBoxName(),
                resources.getString(R.string.product_code_and_name, pb.getProductBoxCode(), pb.getProductBoxName()),
                incomingDetailsResult.getSerialNo(),
                incomingDetailsResult.getProductBoxID(),
                incomingDetailsResult.getIncomingId(),
                incomingDetailsResult.isReserved(),
                incomingDetailsResult.getCreateDate()
        );
    }

    public void deleteProductFromTempList(int position) {
        List<IncomingDetailsResult> tempList = incomingProductionRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(position);
            incomingProductionRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //  setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingProductionRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_temp_list)));
        }
    }

    public LiveData<List<ProductBox>> getProductBoxListLeftMediatorLiveData() {
        if (productBoxListLeftMediatorLiveData == null) {
            productBoxListLeftMediatorLiveData = AsyncMapper.getProductionProductBoxFromResult(
                    incomingProductionRepository.getUniqueProductBoxIDMutLiveData(),
                    incomingProductionRepository.getHashMapDetailsResultMutableLiveData(),
                    this::combineLeftResultData
            );

//            productBoxListLeftMediatorLiveData.addSource(incomingProductionRepository.getUniqueProductBoxIDMutLiveData(),
//                    value -> productBoxListLeftMediatorLiveData.setValue(combineLeftResultData()));
//
//            productBoxListLeftMediatorLiveData.addSource(incomingProductionRepository.getHashMapDetailsResultMutableLiveData(),
//                    value -> productBoxListLeftMediatorLiveData.setValue(combineLeftResultData()));
        }

        return productBoxListLeftMediatorLiveData;
    }

    private List<ProductBox> combineLeftResultData() {
        List<Integer> uniqueListOfIDs = incomingProductionRepository.getUniqueProductBoxIDMutLiveData().getValue();
        HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue();
        List<Incoming> incomingList = incomingProductionRepository.getIncomingListMutableLiveData().getValue();

        List<ProductBox> list = uniqueListOfIDs.stream().map(
                incomingDetails -> getProductBoxLeftFromIncomingDetails(incomingDetails, hashMapFromFirebase, incomingList)).collect(Collectors.toList());

          /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */

        list.removeIf(Objects::isNull);
        //TODO Doraditi ovo da ide provera u iz fragmenta
//        if (list.stream().anyMatch(Objects::isNull)) {
//            incomingProductionRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.incoming_not_all_boxes_synced)));
//            list.removeIf(Objects::isNull);
//        }

        list.sort(Comparator.comparing(ProductBox::getProductBoxName));
        return list;
    }

    private ProductBox getProductBoxLeftFromIncomingDetails(Integer uniqueID,
                                                            HashMap<String, List<IncomingDetailsResult>> hashMapFromFirebase,
                                                            List<Incoming> incomingList) {

        ProductBox pb = productBoxRepository.getProductBoxByID(uniqueID);

        if (pb == null)
            return null;

        int expectedQuantity = (int) incomingList.stream().flatMap(x -> x.getIncomingDetails().stream())
                .filter(incomingDetails -> incomingDetails.getProductBoxID() == uniqueID)
                .mapToDouble(IncomingDetails::getQuantity)
                .sum();

        int quantityOnFb = (int) hashMapFromFirebase.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(x -> x.getProductBoxID() == uniqueID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        ProductBox prodBox = new ProductBox(
                pb.getProductBoxID(),
                pb.getProductBoxName(),
                pb.getProductBoxBarcode(),
                pb.isSerialMustScan(),
                expectedQuantity,
                quantityOnFb,
                pb.getProductBoxCode()
        );

//        ProductBox prodBox = new ProductBox();
//        prodBox.setProductBoxID(pb.getProductBoxID());
//        prodBox.setProductBoxName(pb.getProductBoxName());
//        prodBox.setProductBoxBarcode(pb.getProductBoxBarcode());
//        prodBox.setExpectedQuantity((int) expectedQuantity);
//        prodBox.setAddedQuantity(quantityOnFb);
//        prodBox.setSerialMustScan(pb.isSerialMustScan());

        if (quantityOnFb > expectedQuantity) {
            prodBox.setColorStatus(1);
        } else if (quantityOnFb == expectedQuantity) {
            prodBox.setColorStatus(2);
        } else {
            prodBox.setColorStatus(0);
        }
        return prodBox;

    }

    public LiveData<List<IncomingDetailsResultLocal>> getIncomingDetailsResultLocalListLiveData() {

        return AsyncMapper.getIncomingDetailsLocalFromResult(
                incomingProductionRepository.getHashMapDetailsResultMutableLiveData(),
                this::getIncomingDetailsResultLocalFromFbInput
        );
//        return Transformations.map(incomingProductionRepository.getHashMapDetailsResultMutableLiveData(), this::getIncomingDetailsResultLocalFromFbInput);
    }

    private List<IncomingDetailsResultLocal> getIncomingDetailsResultLocalFromFbInput() {
        List<IncomingDetailsResultLocal> list = incomingProductionRepository.getHashMapDetailsResultMutableLiveData().getValue().values().stream().flatMap(x -> x.stream().map(this::getLocalValues)).collect(Collectors.toList());
        list.sort(Comparator.comparing(IncomingDetailsResultLocal::getProductBoxName));
        return list;
    }

    private IncomingDetailsResultLocal getLocalValues(IncomingDetailsResult incomingDetailsResult) {
        ProductBox pb = productBoxRepository.getProductBoxByID(incomingDetailsResult.getProductBoxID());

        String barcode = warehousePositionRepository.getWarehouseBarcodeByID(incomingDetailsResult.getwPositionID());

        return new IncomingDetailsResultLocal(
                (int) incomingDetailsResult.getQuantity(),
                pb.getProductBoxName(),
                resources.getString(R.string.product_code_and_name, pb.getProductBoxCode(), pb.getProductBoxName()),
                incomingDetailsResult.getSerialNo(),
                incomingDetailsResult.getProductBoxID(),
                incomingDetailsResult.getIncomingId(),
                incomingDetailsResult.isReserved(),
                incomingDetailsResult.isSent(),
                barcode,
                incomingDetailsResult.getCreateDate()
        );
    }

    public void deleteIncomingDetailsResultFromFirebase(IncomingDetailsResultLocal incomingDetailsResultLocal) {
        incomingProductionRepository.deleteIncomingDetailsResultFromFirebase(incomingDetailsResultLocal);
    }

    public void sendIncomingToServerAndFirebase() {
        incomingProductionRepository.sendIncomingToServerAndFirebase();
    }
}
