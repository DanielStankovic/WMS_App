package com.example.wms_app.viewmodel.incoming.standard;

import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;

import com.example.wms_app.R;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.enums.EnumAdditionType;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetails;
import com.example.wms_app.model.IncomingDetailsResult;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.IncomingTruckResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ViewEnableHelper;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.repository.data.EmployeeRepository;
import com.example.wms_app.repository.incoming.standard.IncomingRepository;
import com.example.wms_app.repository.data.ProductBoxRepository;
import com.example.wms_app.repository.data.TruckRepository;
import com.example.wms_app.repository.data.WarehousePositionRepository;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.utilities.mapper.AsyncCheckIfAllProductBoxesSynchronized;
import com.example.wms_app.utilities.mapper.AsyncMapper;
import com.example.wms_app.utilities.Constants;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class SingleIncomingViewModel extends AndroidViewModel {

    private final Resources resources;
    private final IncomingRepository incomingRepository;
    private final ProductBoxRepository productBoxRepository;
    private final WarehousePositionRepository warehousePositionRepository;
    private final TruckRepository truckRepository;
    private final EmployeeRepository employeeRepository;
    private LiveData<List<IncomingDetailsResult>> incomingDetailsResultLiveData;
    private LiveData<List<ProductBox>> productBoxListMediatorLiveData;
    private LiveData<List<ProductBox>> productBoxListLeftMediatorLiveData;
    private MediatorLiveData<Integer> totalNumberOfAddedProdMediatorLiveData;
    private final int employeeIDDb;

    private MutableLiveData<Integer> requestFocusViewID;

    public SingleIncomingViewModel(@NonNull Application application) {
        super(application);
        resources = application.getResources();
        incomingRepository = new IncomingRepository(application.getApplicationContext());
        productBoxRepository = new ProductBoxRepository(application.getApplicationContext());
        warehousePositionRepository = new WarehousePositionRepository(application.getApplicationContext());
        truckRepository = new TruckRepository(application.getApplicationContext());
        employeeRepository = new EmployeeRepository(application.getApplicationContext());
        employeeIDDb = RoomDb.getDatabase(application.getApplicationContext()).employeeDao().getEmployeeID();

    }

    /**
     * Dobija se LiveData ApiResponse. Njegov status moze biti LOADING, SUCCESS,
     * SUCCESS_WITH_ACTION i ERROR. Koristi se kada se komunicira sa Firebaseom ili Serverom
     *
     * @return apiResponseLiveData
     */
    public LiveData<ApiResponse> getApiResponseLiveData() {
        return incomingRepository.getResponseMutableLiveData();
    }

    public void refreshApiResponseStatus() {
        incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.idle());
    }

    public LiveData<List<IncomingDetailsResult>> getIncomingDetailsResultLiveData() {
        incomingDetailsResultLiveData = incomingRepository.getIncomingDetailsResultMutLiveData();
        return incomingDetailsResultLiveData;
    }

    public LiveData<Incoming> getSelectedIncomingLiveData() {
        return incomingRepository.getSelectedIncomingMutLiveData();

    }

    public LiveData<ProductBox> getScannedProductBoxLiveData() {
        return incomingRepository.getScannedProductBoxMutLiveData();

    }

    public LiveData<List<IncomingTruckResult>> getIncomingTruckResultLiveData() {
        return incomingRepository.getIncomingTruckResultMutLiveData();
    }

    public LiveData<List<String>> getLicencePlateListLiveData() {
        return truckRepository.getLicencePlateListMutLiveData();
    }

    public LiveData<Integer> getEmployeeIDLiveData() {
        return employeeRepository.getEmployeeIDLiveData();
    }


    public LiveData<List<IncomingDetailsResultLocal>> getIncomingDetailsResultLocalListLiveData() {

        return incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                AsyncMapper.getProductBoxFromResult(
                        incomingRepository.getIncomingDetailsResultFromFbMutLiveData(),
                        this::getIncomingDetailsResultLocalFromFbInput
                ) :
                AsyncMapper.getProductBoxFromResultGrouped(
                        incomingRepository.getHashMapDetailsResultMutableLiveData(),
                        this::getIncomingDetailsResultLocalFromFbInput
                );

    }


    private List<IncomingDetailsResultLocal> getIncomingDetailsResultLocalFromFbInput() {
        List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<IncomingDetailsResultLocal> list = listFromFirebase.stream().map(this::getLocalValues).collect(Collectors.toList());
        list.sort(Comparator.comparing(IncomingDetailsResultLocal::getProductBoxName));
        return list;
    }

    public LiveData<Boolean> getIsIncomingFinishedLiveData() {
        return incomingRepository.getIsIncomingFinishedMutableLiveData();

    }

    private IncomingDetailsResultLocal getLocalValues(IncomingDetailsResult incomingDetailsResult) {
        ProductBox pb = productBoxRepository.getProductBoxByID(incomingDetailsResult.getProductBoxID());

        return new IncomingDetailsResultLocal(
                (int) incomingDetailsResult.getQuantity(),
                pb.getProductBoxName(),
                resources.getString(R.string.product_code_and_name, pb.getProductBoxCode(), pb.getProductBoxName()),
                incomingDetailsResult.getSerialNo(),
                incomingDetailsResult.getProductBoxID(),
                incomingDetailsResult.getIncomingId(),
                incomingDetailsResult.isReserved(),
                incomingDetailsResult.isSent(),
                incomingDetailsResult.getWarehousePositionBarcode(),
                incomingDetailsResult.getCreateDate()
        );
    }

    public LiveData<List<IncomingDetailsResultLocal>> getIncomingDetailsFromTempList() {

        return AsyncMapper.getProductBoxFromResult(
                incomingRepository.getIncomingDetailsResultMutLiveData(),
                this::getIncomingDetailsResultFromTempListInput
        );

//        return Transformations.map(incomingRepository.getIncomingDetailsResultMutLiveData(), this::getIncomingDetailsResultFromTempListInput);
    }

    private List<IncomingDetailsResultLocal> getIncomingDetailsResultFromTempListInput() {
        return incomingRepository.getIncomingDetailsResultMutLiveData().getValue().stream().map(this::getLocalValuesForTempList).collect(Collectors.toList());
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


    public LiveData<Boolean> toggleUndoAndRefreshBtnLiveData() {
        return Transformations.map(incomingRepository.getIncomingDetailsResultMutLiveData(), List::isEmpty);
    }

    public LiveData<List<ProductBox>> getProductBoxListMediatorLiveData() {
        if (productBoxListMediatorLiveData == null) {
            if (incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE) {
                productBoxListMediatorLiveData = AsyncMapper.getProductBoxFromResult(
                        incomingRepository.getSelectedIncomingMutLiveData(),
                        incomingRepository.getIncomingDetailsResultMutLiveData(),
                        incomingRepository.getIncomingDetailsResultFromFbMutLiveData(),
                        this::combineResultData
                );
            } else {
                productBoxListMediatorLiveData = AsyncMapper.getProductBoxFromResultGrouped(
                        incomingRepository.getSelectedIncomingMutLiveData(),
                        incomingRepository.getIncomingDetailsResultMutLiveData(),
                        incomingRepository.getHashMapDetailsResultMutableLiveData(),
                        this::combineResultData
                );
            }
        }

        return productBoxListMediatorLiveData;
    }

    public LiveData<List<ProductBox>> getProductBoxListLeftMediatorLiveData() {
        if (productBoxListLeftMediatorLiveData == null) {
            productBoxListLeftMediatorLiveData = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                    AsyncMapper.getProductBoxFromResult(
                            incomingRepository.getSelectedIncomingMutLiveData(),
                            incomingRepository.getIncomingDetailsResultFromFbMutLiveData(),
                            this::combineLeftResultData
                    ) :
                    AsyncMapper.getProductBoxFromResultGrouped(
                            incomingRepository.getSelectedIncomingMutLiveData(),
                            incomingRepository.getHashMapDetailsResultMutableLiveData(),
                            this::combineLeftResultData
                    );

//            productBoxListLeftMediatorLiveData.addSource(incomingRepository.getSelectedIncomingMutLiveData(),
//                    value -> productBoxListLeftMediatorLiveData.setValue(combineLeftResultData()));
//
//            productBoxListLeftMediatorLiveData.addSource(incomingRepository.getIncomingDetailsResultFromFbMutLiveData(),
//                    value -> productBoxListLeftMediatorLiveData.setValue(combineLeftResultData()));
        }

        return productBoxListLeftMediatorLiveData;
    }

    public MediatorLiveData<Integer> getTotalNumberOfAddedProdMediatorLiveData() {
        if (totalNumberOfAddedProdMediatorLiveData == null) {
            totalNumberOfAddedProdMediatorLiveData = new MediatorLiveData<>();
            totalNumberOfAddedProdMediatorLiveData.addSource(incomingRepository.getIncomingDetailsResultMutLiveData(),
                    value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            if (incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE) {
                totalNumberOfAddedProdMediatorLiveData.addSource(incomingRepository.getIncomingDetailsResultFromFbMutLiveData(),
                        value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            } else {
                totalNumberOfAddedProdMediatorLiveData.addSource(incomingRepository.getHashMapDetailsResultMutableLiveData(),
                        value -> totalNumberOfAddedProdMediatorLiveData.setValue(sumTotalAddedQuantity()));
            }
        }
        return totalNumberOfAddedProdMediatorLiveData;
    }

    private Integer sumTotalAddedQuantity() {
        int totalSum;

        List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        int quantityInTemp = (int) incomingRepository.getIncomingDetailsResultMutLiveData().getValue().stream()
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();
        int quantityOnFb = (int) listFromFirebase.stream()
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();
        totalSum = quantityInTemp + quantityOnFb;
        return totalSum;
    }

    private List<ProductBox> combineResultData() {
        try {
            List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
            //Ovde se lista listFromFirebase definise u zavisnosti da li se radi o pojedinacnom ili grupnom prijemu

            List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                    incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                    : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            List<IncomingDetails> incomingDetailsList = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingDetails();
            List<Integer> uniqueProductBoxIDs = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingDetails()
                    .stream()
                    .map(IncomingDetails::getProductBoxID)
                    .distinct()
                    .collect(Collectors.toList());


            List<ProductBox> list = uniqueProductBoxIDs.stream().map(
                    uniqueProductBoxID -> getProductBoxFromIncomingDetails(uniqueProductBoxID, incomingDetailsList, tempList, listFromFirebase)).collect(Collectors.toList());

        /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
        DORADJENO : Ovde se samo sklanjaju nullovi a provera s evrsi kada se udje u formu
         */
            list.removeIf(Objects::isNull);

//        if (list.stream().anyMatch(Objects::isNull)) {
//            incomingRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.incoming_not_all_boxes_synced)));
//            list.removeIf(Objects::isNull);
//        }
            list.sort(Comparator.comparing(ProductBox::getProductBoxName));
            list.add(0, ProductBox.newPlaceHolderInstance());
            return list;
        } catch (ConcurrentModificationException e) {
            Utility.writeErrorToFile(e);
            return null;
        }
    }

    private List<ProductBox> combineLeftResultData() {
        List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<IncomingDetails> incomingDetailsList = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingDetails();
        List<Integer> uniqueProductBoxIDs = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingDetails()
                .stream()
                .map(IncomingDetails::getProductBoxID)
                .distinct()
                .collect(Collectors.toList());

        List<ProductBox> list = uniqueProductBoxIDs.stream().map(
                uniqueProductBoxID -> getProductBoxLeftFromIncomingDetails(uniqueProductBoxID, incomingDetailsList, listFromFirebase)).collect(Collectors.toList());

          /*Moze da se desi situacija da na firebase-u postoji neki dokument sa kutijom koja nije sinhronizovana na uredjaju.
        U tom slucaju kutija u listi je null, zato ovde ide provera i onda iskace poruka i takva kutija se izbacuje iz liste.
         */
        //TODO Doradjeno. Napraviti bolju proveru iz fragmenta
        list.removeIf(Objects::isNull);

//        if (list.stream().anyMatch(Objects::isNull)) {
//            incomingRepository.getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.incoming_not_all_boxes_synced)));
//            list.removeIf(Objects::isNull);
//        }

        list.sort(Comparator.comparing(ProductBox::getProductBoxName));
        return list;
    }

    /**
     * Ovaj objekat se koristi kako bi Enable/Disable polja za unos kolicine i serijskog broja
     *
     * @return viewEnableHelperMutableLiveData
     */
    public LiveData<ViewEnableHelper> getViewEnableHelperLiveData() {
        return incomingRepository.getViewEnableHelperLiveData();
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

    public void setupSelectedIncoming(Incoming selectedIncoming) {
        if (selectedIncoming != null) {
            incomingRepository.getSelectedIncomingMutLiveData().setValue(selectedIncoming);
        } else
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_loading_current_incoming_leave)));

    }

    public void setupCurrentIncoming(Incoming currentOutgoing) {
        incomingRepository.getSelectedIncomingMutLiveData().setValue(currentOutgoing);
    }

    public void setupCurrentIncomingStyle(EnumIncomingStyle enumIncomingStyle) {
        incomingRepository.getEnumIncomingStyleMutableLiveData().setValue(enumIncomingStyle);
    }

//    public LiveData<List<ProductBox>> getSpinnerProductBoxListLiveData() {
//        xdsa
//        return Transformations.map(incomingRepository.getSelectedIncomingMutLiveData(),
//                x -> x.getIncomingDetails().stream().map(incomingDetails -> getProductBoxFromIncomingDetails(incomingDetails,te))
//                        .collect(Collectors.toList())
//        );
//    }

    private ProductBox getProductBoxFromIncomingDetails(Integer uniqueProductBoxID, List<IncomingDetails> incomingDetailsList,
                                                        List<IncomingDetailsResult> tempList,
                                                        List<IncomingDetailsResult> listFromFirebase) {

        ProductBox pb = productBoxRepository.getProductBoxByID(uniqueProductBoxID);

        if (pb == null)
            return null;

        ProductBox prodBox = null;
        try {
            int quantityInTemp = (int) tempList.stream()
                    .filter(x -> x.getProductBoxID() == uniqueProductBoxID)
                    .mapToDouble(IncomingDetailsResult::getQuantity)
                    .sum();
            int quantityOnFb = (int) listFromFirebase.stream()
                    .filter(x -> x.getProductBoxID() == uniqueProductBoxID)
                    .mapToDouble(IncomingDetailsResult::getQuantity)
                    .sum();

            int expectedQuantity = (int) incomingDetailsList.stream()
                    .filter(x -> x.getProductBoxID() == uniqueProductBoxID)
                    .mapToDouble(IncomingDetails::getQuantity)
                    .sum();

            prodBox = new ProductBox(
                    pb.getProductBoxID(),
                    pb.getProductBoxName(),
                    pb.getProductBoxBarcode(),
                    pb.isSerialMustScan(),
                    expectedQuantity,
                    quantityInTemp + quantityOnFb,
                    pb.getProductBoxCode()
            );

            if (quantityInTemp + quantityOnFb > expectedQuantity) {
                prodBox.setColorStatus(1);
            } else if (quantityInTemp + quantityOnFb == expectedQuantity) {
                prodBox.setColorStatus(2);
            } else {
                prodBox.setColorStatus(0);
            }
        } catch (ConcurrentModificationException e) {
            Utility.writeErrorToFile(e);
        }
        return prodBox;
    }

    private ProductBox getProductBoxLeftFromIncomingDetails(Integer uniqueProductBoxID, List<IncomingDetails> incomingDetailsList,
                                                            List<IncomingDetailsResult> listFromFirebase) {

        ProductBox pb = productBoxRepository.getProductBoxByID(uniqueProductBoxID);

        if (pb == null)
            return null;
        ProductBox prodBox = null;
        try {
            int quantityOnFb = (int) listFromFirebase.stream()
                    .filter(x -> x.getProductBoxID() == uniqueProductBoxID)
                    .mapToDouble(IncomingDetailsResult::getQuantity)
                    .sum();

            int expectedQuantity = (int) incomingDetailsList.stream()
                    .filter(x -> x.getProductBoxID() == uniqueProductBoxID)
                    .mapToDouble(IncomingDetails::getQuantity)
                    .sum();

            prodBox = new ProductBox();
            prodBox.setProductBoxID(pb.getProductBoxID());
            prodBox.setProductBoxName(pb.getProductBoxName());
            prodBox.setProductBoxBarcode(pb.getProductBoxBarcode());
            prodBox.setExpectedQuantity(expectedQuantity);
            prodBox.setAddedQuantity(quantityOnFb);
            prodBox.setSerialMustScan(pb.isSerialMustScan());
            if (quantityOnFb > expectedQuantity) {
                prodBox.setColorStatus(1);
            } else if (quantityOnFb == expectedQuantity) {
                prodBox.setColorStatus(2);
            } else {
                prodBox.setColorStatus(0);
            }

        } catch (ConcurrentModificationException e) {
            Utility.writeErrorToFile(e);
        }
        return prodBox;
    }

    public void productBoxManuallySelected(ProductBox selectedProductBox) {

        //Provera ako je selektovan prvi u listi artikal koji je placeholder

        if (selectedProductBox.getProductBoxID() == -1) {
            incomingRepository.toggleViewEnabledAndText(R.id.incomingLeftQtyEt, resources.getString(R.string.added_and_expected_qty, selectedProductBox.getAddedQuantity(), selectedProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.getCurrentProductBox().setValue(selectedProductBox);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.GONE);

            return;
        }

        if (selectedProductBox.isSerialMustScan()) {
            incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingAddNoSrNumBtn, "", true, EnumViewType.BUTTON, View.VISIBLE);
            requestFocusViewID.setValue(R.id.incomingSrNumberEt);
        } else {
            incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", false, EnumViewType.EDIT_TEXT, View.INVISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingAddNoSrNumBtn, "1", true, EnumViewType.BUTTON, View.GONE);
        }
        //Ovo za ocekivanu  i dodatu kolicinu ide ovde posto se postavlja u svakom slucaju
        incomingRepository.toggleViewEnabledAndText(R.id.incomingLeftQtyEt, resources.getString(R.string.added_and_expected_qty, selectedProductBox.getAddedQuantity(), selectedProductBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
        incomingRepository.getCurrentProductBox().setValue(selectedProductBox);
    }


    public void addProductBoxManually(ProductBox currentProductBox, String serialNumber, String quantity, boolean isReserved) {

        if (currentProductBox != null && currentProductBox.getProductBoxID() != -1) {

            if (!quantity.isEmpty()) {
                int qty = -1;
                try {
                    qty = Integer.parseInt(quantity);
                } catch (NumberFormatException e) {
                    incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_bad_format)));
                    return;
                }
                if (qty > 0) {

                    if (currentProductBox.isSerialMustScan()) {
                        if (!serialNumber.isEmpty()) {
                            addBigProductToTempList(currentProductBox, serialNumber, qty, false, isReserved);
                        } else {
                            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.sr_num_mandatory)));
                        }

                    } else {
                        addSmallProductToTempList(currentProductBox, "", qty, EnumAdditionType.MANUALLY, false, isReserved);
                    }

                } else {
                    incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_must_be_over_zero)));
                }
            } else {
                incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.qty_not_inserted)));
            }

        } else {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.product_not_selected_from_list)));
        }

    }

    private void addBigProductToTempList(ProductBox selectedProductBox, String serialNumber, int quantity, boolean isScanned, boolean isReserved) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        /*Mora ovako posto ne moze u lambda expression da se prosledi selectedProduct objekat
         * */
        int selectedProductBoxID = selectedProductBox.getProductBoxID();


            /*Sada je moguce dodati 2 artikla sa istim serijskim brojem i istim IDjem. Tako da se ovde proerava kolicina kao kod malih artikla.
            Ako je sve ok ide dodavanje u listi. U temp listi to ce biti 2 objekta ali na firebase ce se kolicina grupisati.
            * */

        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
        List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        String incomingID = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingID();

        int definedQuantity = (int) incomingRepository.getSelectedIncomingMutLiveData().getValue()
                .getIncomingDetails()
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetails::getQuantity)
                .sum();

        int quantityOnFirebase = (int) listFromFirebase
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        int quantityInTempList = (int) Objects.requireNonNull(tempList)
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetailsResult::getQuantity)
                .sum();

        if (quantityInTempList + (int) quantity + quantityOnFirebase <= definedQuantity) {

            tempList.add(getIncomingDetailsResultToAdd(
                    incomingID,
                    selectedProductBoxID,
                    serialNumber,
                    quantity,
                    employeeID,
                    new Date(),
                    isScanned,
                    isReserved
            ));

            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //Resetovanje polja za serijski broj
            incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //setupAddedAndExpectedQuantity(selectedProductID);


        } else {

            int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFirebase;
            incomingRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, definedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tempList.add(getIncomingDetailsResultToAdd(
                                    incomingID,
                                    selectedProductBoxID,
                                    serialNumber,
                                    quantity,
                                    employeeID,
                                    new Date(),
                                    isScanned,
                                    isReserved
                            ));

                            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                            //Resetovanje polja za serijski broj
                            incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        }
                    }));
        }
    }

    private void addSmallProductToTempList(ProductBox selectedProductBox, String serialNumber, int quantity, EnumAdditionType enumAdditionType, boolean isScanned, boolean isReserved) {

        int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
        if (employeeID == -1) {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }

        int selectedProductBoxID = selectedProductBox.getProductBoxID();
        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
        List<IncomingDetailsResult> listFromFirebase = incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE ?
                incomingRepository.getIncomingDetailsResultFromFbMutLiveData().getValue()
                : incomingRepository.getHashMapDetailsResultMutableLiveData().getValue()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        String incomingID = incomingRepository.getSelectedIncomingMutLiveData().getValue().getIncomingID();

        int definedQuantity = (int) incomingRepository.getSelectedIncomingMutLiveData().getValue()
                .getIncomingDetails()
                .stream()
                .filter(x -> x.getProductBoxID() == selectedProductBoxID)
                .mapToDouble(IncomingDetails::getQuantity)
                .sum();

        int quantityOnFirebase = (int) listFromFirebase
                .stream()
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
                    if (quantityInTempList + (int) quantity + quantityOnFirebase - (int) lastAddedProd.getQuantity() <= definedQuantity) {
                        tempList.remove(tempList.size() - 1);
                        tempList.add(getIncomingDetailsResultToAdd(
                                incomingID,
                                selectedProductBoxID,
                                serialNumber,
                                quantity,
                                employeeID,
                                new Date(),
                                isScanned,
                                isReserved
                        ));
                        incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                        incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        //  setupAddedAndExpectedQuantity(selectedProduct.getProductID());
                        return;
                    } else {
                        /* Znaci da je uneta kolicina vece od one koja je na poziciji
                         *  */

                        int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFirebase - (int) lastAddedProd.getQuantity();
                        incomingRepository.getResponseMutableLiveData().setValue(
                                ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, definedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        tempList.remove(tempList.size() - 1);
                                        tempList.add(getIncomingDetailsResultToAdd(
                                                incomingID,
                                                selectedProductBoxID,
                                                serialNumber,
                                                quantity,
                                                employeeID,
                                                new Date(),
                                                isScanned,
                                                isReserved
                                        ));

                                        incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                                        incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                                    }
                                }));
                        return;
                    }
                }
            }
        }


        if (quantityInTempList + (int) quantity + quantityOnFirebase <= definedQuantity) {

            tempList.add(getIncomingDetailsResultToAdd(
                    incomingID,
                    selectedProductBoxID,
                    serialNumber,
                    quantity,
                    employeeID,
                    new Date(),
                    isScanned,
                    isReserved
            ));

            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //setupAddedAndExpectedQuantity(selectedProductID);


        } else {
            int totalAddedQty = quantityInTempList + (int) quantity + quantityOnFirebase;
            incomingRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.added_qty_greater_than_defined, definedQuantity, totalAddedQty), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tempList.add(getIncomingDetailsResultToAdd(
                                    incomingID,
                                    selectedProductBoxID,
                                    serialNumber,
                                    quantity,
                                    employeeID,
                                    new Date(),
                                    isScanned,
                                    isReserved
                            ));

                            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
                            incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
                        }
                    }));
        }
    }

    private IncomingDetailsResult getIncomingDetailsResultToAdd(String incomingID, int selectedProductBoxID,
                                                                String serialNumber, int quantity,
                                                                int employeeId, Date date, boolean isScanned,
                                                                boolean isReserved) {
        IncomingDetailsResult idr = new IncomingDetailsResult();
        idr.setIncomingId(incomingID);
        idr.setCreateDate(date);
        idr.setEmployeeID(employeeId);
        idr.setOnIncoming(true);
        idr.setProductBoxID(selectedProductBoxID);
        idr.setQuantity(quantity);
        idr.setScanned(isScanned);
        idr.setSent(false);
        idr.setSerialNo(serialNumber);
        idr.setReserved(isReserved);
        return idr;
    }

    public void removeFirebaseRealTimeListener() {
        incomingRepository.removeFirebaseRealTimeListener();
    }

    public void removeAllFromTempList() {
        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null) {
            tempList.clear();
            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
//            setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_all_from_temp_list)));

        }
    }

    public void removeLastFromTempList() {
        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(tempList.size() - 1);
            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //  setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_last_from_temp_list)));
        }

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
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.wrong_barcode)));
        }

    }

    private void positionScanned(String scannedCode) {
        /*Provera da li postoji nesto u temp listi pre postavljanja
         * */
        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
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

//                    if (incomingRepository.getEnumIncomingStyleMutableLiveData().getValue() == EnumIncomingStyle.SINGLE)
                    incomingRepository.getWarehousePositionFromFirebase(tempList, warehousePosition.getBarcode());
                    // incomingRepository.pushTempListToFirebase(tempList, warehousePosition.getWPositionID(), warehousePosition.getBarcode());
//                    else
//                        incomingRepository.pushTempListToFirebaseGrouped(tempList, warehousePosition.getWPositionID(), warehousePosition.getBarcode());

                } else {
                    incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_defined_for_incoming)));
                }
            } else {
                incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.pos_not_exist)));
            }
        } else {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.no_added_prods)));
        }
    }

    private void articleScanned(String scannedCode, boolean isReserved) {

        ProductBox scannedProductBox = getProductBoxListMediatorLiveData().getValue()
                .stream().filter(x -> x.getProductBoxBarcode().equals(scannedCode))
                .findAny()
                .orElse(null);
        if (scannedProductBox == null) {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.scanned_prod_box_not_on_inc)));
            return;
        }

        if (scannedProductBox.isSerialMustScan()) {
            //Znaci da mora serisjki broj tj veliki artikal

            /* Cim je serialMustScan odmah kolicina
                            ide na 1 i disabluje se polje */
            //  incomingRepository.toggleViewEnabledAndText(R.id.incomingQtyEt, "1", false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            //Trenutno nemamo evidenciju o serisjib brojevima. Zato se ovde postavlja prazan string incae bi se postavio serialNumber Ako se bude ubacila ovde ide kod TODO Serijski broj, serial number, sr num
            //  incomingRepository.toggleViewEnabledAndText(R.id.incomingSrNumberEt, "", true, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);


        } else {

            //Znaci da ne mora serisjki broj tj mali artikal
            incomingRepository.getScannedProductBoxMutLiveData().setValue(scannedProductBox);
            addSmallProductToTempList(scannedProductBox, "", 1, EnumAdditionType.SCANNING, true, isReserved);
        }
    }

    public void refreshExpectedQty(ProductBox productBox) {
        if (productBox != null) {
            incomingRepository.toggleViewEnabledAndText(R.id.incomingLeftQtyEt, resources.getString(R.string.added_and_expected_qty, productBox.getAddedQuantity(), productBox.getExpectedQuantity()), false, EnumViewType.EDIT_TEXT, View.VISIBLE);
            incomingRepository.getCurrentProductBox().setValue(productBox);
        }
    }

    public void insertTruckToFirebase(String truckDriver, String licencePlate, int employeeId) {
        if (employeeId == -1) {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.errorWithAction(resources.getString(R.string.employee_id_invalid)));
            return;
        }
        incomingRepository.insertTruckToFirebase(truckDriver, licencePlate, employeeId);

    }

    public void deleteTruckFromFirebase(IncomingTruckResult incomingTruckResult) {
        incomingRepository.deleteTruckFromFirebase(incomingTruckResult);
    }

    public void deleteIncomingDetailsResultFromFirebase(IncomingDetailsResultLocal incomingDetailsResultLocal) {
        incomingRepository.deleteIncomingDetailsResultFromFirebase(incomingDetailsResultLocal);
    }

    public void deleteProductFromTempList(int position) {
        List<IncomingDetailsResult> tempList = incomingRepository.getIncomingDetailsResultMutLiveData().getValue();
        if (tempList != null &&
                tempList.size() > 0) {
            tempList.remove(position);
            incomingRepository.getIncomingDetailsResultMutLiveData().setValue(tempList);
            //  setupAddedAndExpectedQuantity(selectedProductID);
        } else {
            incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.error_deleting_prod_from_temp_list)));
        }
    }


    public void sendIncomingToServerAndFirebase() {
        //Provera da li treba da iskace dijalog sa pitanjem ako nalog nije zavrsen
        boolean isFinished = incomingRepository.getIsIncomingFinishedMutableLiveData().getValue() == null ?
                false :
                incomingRepository.getIsIncomingFinishedMutableLiveData().getValue();
        if (isFinished)
            incomingRepository.sendIncomingToServerAndFirebase();
        else {
            incomingRepository.getResponseMutableLiveData().setValue(
                    ApiResponse.prompt(resources.getString(R.string.incoming_not_finished_send_prompt),
                            (dialogInterface, i) -> incomingRepository.sendIncomingToServerAndFirebase()));
        }
    }

    public void sendIncomingToServerAndFirebaseGrouped() {
        incomingRepository.getResponseMutableLiveData().setValue(
                ApiResponse.prompt(resources.getString(R.string.incomings_send_prompt),
                        (dialogInterface, i) -> incomingRepository.getIncomingListToBeSent()));
    }

    public void registerFirebaseRealTimeUpdatesIncoming(Incoming currentIncoming) {
        setupCurrentIncomingStyle(EnumIncomingStyle.SINGLE);
        setupCurrentIncoming(currentIncoming);
        incomingRepository.setPositionBarcodeByWarehouseName(currentIncoming.getPartnerWarehouseName());
        incomingRepository.registerRealTimeUpdatesIncomingDetailsSingle(currentIncoming);
        incomingRepository.registerRealTimeUpdatesIncomingTruckSingle(currentIncoming);
        incomingRepository.registerRealTimeUpdatesIncoming(currentIncoming);
    }

    public void registerFirebaseRealTimeUpdatesIncomingGrouped(IncomingGrouped currentIncomingGrouped) {
        Incoming currentIncoming = new Incoming();
        currentIncoming.setTotalNumOfProd(currentIncomingGrouped.getTotalNumOfProds());
        currentIncoming.setIncomingDetails(currentIncomingGrouped.getIncomingDetailsList());
        currentIncoming.setIncomingIDList(currentIncomingGrouped.getIncomingIDList());
        currentIncoming.setUniqueProductBoxIDList(currentIncomingGrouped.getUniqueProductBoxIDList());
        setupCurrentIncomingStyle(EnumIncomingStyle.GROUPED);
        setupCurrentIncoming(currentIncoming);
        incomingRepository.registerRealTimeUpdatesIncomingDetailsGrouped(currentIncomingGrouped);
    }

    public void checkIfAllProductBoxesSynchronized() {
        new AsyncCheckIfAllProductBoxesSynchronized(productBoxRepository.getProductBoxDao(), incomingRepository.getSelectedIncomingMutLiveData()
                .getValue()
                .getIncomingDetails()
                .stream()
                .map(IncomingDetails::getProductBoxID)
                .distinct()
                .collect(Collectors.toList()), allExist -> {
            if (!allExist) {
                incomingRepository.getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_not_all_boxes_synced)));
            }
        });

    }
}
