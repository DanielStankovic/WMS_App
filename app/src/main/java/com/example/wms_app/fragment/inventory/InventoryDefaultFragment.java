package com.example.wms_app.fragment.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.R;
import com.example.wms_app.adapter.ProductBoxSpinnerAdapter;
import com.example.wms_app.databinding.FragmentInventoryDefaultBinding;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.OnOneOffClickListener;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.inventory.InventoryViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class InventoryDefaultFragment extends Fragment {

    private FragmentInventoryDefaultBinding binding;

    private InventoryViewModel inventoryViewModel;

    private ImageView btnReset, btnUndo;
    private CustomGauge gauge;
    private TextView tvGauge;

    private List<InventoryDetailsResult> idrList;
    private List<ProductBox> productsForInventory;
    private List<WarehousePosition> warehousePositions;
    //privremena lista i poslednji skenirani artikal
    private List<InventoryDetailsResult> idrLastList;
    private List<ProductBox> placeholderProductList;

    //artikal koji je u trenutnoj selekciji
    private ProductBox selectedProduct;
    private InventoryDetailsResult idrLast;
    private AlertDialog loadingDialog;
    private OnBackPressedCallback callback;

    private AlertDialog dialogOk, dialogYesNo;

    private ArrayAdapter<ProductBox> spinnerAdapter;
    private WarehousePosition currentPosition = null;
    private boolean isPositionSelect = false;
    private String currentInventoryID = "0";
    private int employeeIDDb = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryDefaultBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModels();
        init();
        //initAdapters();
        initDialogs();
        setObservers();
        setListeners();
        //editTextListeners();

        if(isPositionSelect)
            setPositionFields(currentPosition);
        else
            positionClear();
    }

    private void initDialogs() {

        loadingDialog = DialogBuilder.getLoadingDialog(getContext());
        dialogOk = DialogBuilder.showOkDialogWithoutCallback(getContext(),getResources().getString(R.string.warning), "Ovo je dialog");

    }



    private void initViewModels() {
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
    }

    private void setListeners() {

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPosition != null){
                    if (selectedProduct.getProductBoxID() != -1)
                        if (!binding.etSelectedQuantity.getText().toString().equals("") && !binding.etSelectedQuantity.getText().toString().equals("0")) {

                            int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
                            if (employeeID == -1) {
                                DialogBuilder.showOkDialogWithCallback(requireContext(), getResources().getString(R.string.error), getResources().getString(R.string.employee_id_invalid), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getActivity().onBackPressed();
                                    }
                                });
                                return;
                            }

                            if (idrLastList.size() > 0) {
                                if (idrLastList.get(idrLastList.size() - 1).getProductBoxID() != selectedProduct.getProductBoxID())
                                    addProductToTempList(false, binding.etSerial.getText().toString(), Integer.valueOf(binding.etSelectedQuantity.getText().toString()), currentPosition, employeeID);
                                else {
                                    idrLastList.remove(idrLastList.size() - 1);
                                    addProductToTempList(false, binding.etSerial.getText().toString(), Integer.valueOf(binding.etSelectedQuantity.getText().toString()), currentPosition, employeeID);
                                }
                            } else {
                                addProductToTempList(false, binding.etSerial.getText().toString(), Integer.valueOf(binding.etSelectedQuantity.getText().toString()), currentPosition, employeeID);
                            }
                        } else
                            DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.quantity_error)).show();

                    else
                        DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning),getResources().getString(R.string.product_must_scan)).show();
                }else{
                    DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning),getResources().getString(R.string.position_first_add)).show();
                }
            }
        });
        binding.btnPosition.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                //metoda za upis u firebase
                if (!isPositionSelect) {
                    if (!binding.etPosition.getText().toString().isEmpty()) {
                        Optional<WarehousePosition> position = warehousePositions.stream().filter(x -> x.getBarcode().equals(binding.etPosition.getText().toString())).findFirst();
                        if (position.isPresent()) {
                            //pronadjena je pozicija u magacinu
                            setPositionFields(position.get());
                        } else {
                            DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.position_not_found)).show();
                            binding.etPosition.setText("");
                        }

                    }else{
                        DialogBuilder.showOkDialogWithoutCallback(getContext(),  getResources().getString(R.string.warning), getResources().getString(R.string.position_not_selected)).show();
                    }
                }else {
                    positionClear();
                }
            }
        });

        binding.btnLoad.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (idrLastList.size() > 0) {
                    inventoryViewModel.pushInventoryResult(idrLastList, currentInventoryID);
                } else {
                    DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.temp_list_empty)).show();

                }
            }
        });



        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inventoryViewModel.emptyTempList();
//                singleIncomingViewModel.setProductWithQuantity(currentIncomingDetails, selectedProduct, idrList);
            }
        });
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inventoryViewModel.undoTempList();
//                singleIncomingViewModel.setProductWithQuantity(currentIncomingDetails, selectedProduct, idrList);

            }
        });

    }

    private void setPositionFields(WarehousePosition position) {
        binding.etPosition.setEnabled(false);
        binding.btnPosition.setText("Obriši");
        currentPosition = position;
        isPositionSelect = true;
        if(idrLastList.size() > 0)
            inventoryViewModel.pushInventoryResult(idrLastList, currentInventoryID);

        initAdapters(productsForInventory);

    }

    private void positionClear(){
        binding.etPosition.setEnabled(true);
        binding.etPosition.setText("");
        binding.btnPosition.setText("Postavi");
        currentPosition = null;
        isPositionSelect = false;

        if(idrLastList.size() > 0)
            inventoryViewModel.pushInventoryResult(idrLastList, currentInventoryID);
        initAdapters(placeholderProductList);
    }

    private void addProductToTempList(boolean isScan, String serialNo, Integer quantity, WarehousePosition position, int employeeID) {
        idrLast = new InventoryDetailsResult();
        idrLast.setEmployeeID(employeeID);
        idrLast.setCreateDate(new Date());
        idrLast.setInventoryID(currentInventoryID);
        idrLast.setProductBoxID(selectedProduct.getProductBoxID());
        idrLast.setQuantity(quantity);
        idrLast.setSent(false);
        idrLast.setSerialNo(serialNo);
        idrLast.setScanned(isScan);
        idrLast.setwPositionID(position.getwPositionID());
        idrLast.setWarehousePositionBarcode(position.getBarcode());
        idrLastList.add(idrLast);
        inventoryViewModel.setIdrLastList(idrLastList);
//            singleIncomingViewModel.setProductWithQuantity(currentIncomingDetails, selectedProduct, idrList);
    }


    private void initAdapters(List<ProductBox> productBoxList) {

        spinnerAdapter = new ProductBoxSpinnerAdapter(getContext() , R.layout.item_spinner_product, productBoxList, false);
        binding.searchableSpinner.setTitle("Izaberi proizvod");
        binding.searchableSpinner.setAdapter(spinnerAdapter);

        binding.searchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //uzima trenutne detalje za izabrani artikal
                selectedProduct = (ProductBox) binding.searchableSpinner.getSelectedItem();
                setPomLabels();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void setObservers() {

        inventoryViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(), new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {
                consumeResponse(apiResponse);
            }
        });

        inventoryViewModel.getAllProducts().observe(getViewLifecycleOwner(), new Observer<List<ProductBox>>() {
            @Override
            public void onChanged(List<ProductBox> productBoxes) {
                if(productBoxes.get(0).getProductBoxID() != -1){
                    ProductBox placeholder =  ProductBox.newPlaceHolderInstance();
                    productBoxes.add(0, placeholder);
                }
                productsForInventory = productBoxes;
             //   initAdapters(placeholderProductList);
            }
        });


        inventoryViewModel.getIdrLastList().observe(getViewLifecycleOwner(), new Observer<List<InventoryDetailsResult>>() {
            @Override
            public void onChanged(List<InventoryDetailsResult> inventoryDetailsResults) {
                idrLastList = inventoryDetailsResults;
                setGauge();
                setBackAndUndo(idrLastList);
                binding.etSerial.setText("");
                binding.etSelectedQuantity.setText("");
                binding.etSelectedQuantity.requestFocus();

            }
        });

        inventoryViewModel.getAllPositions().observe(getViewLifecycleOwner(), new Observer<List<WarehousePosition>>() {
            @Override
            public void onChanged(List<WarehousePosition> positions) {
                warehousePositions.clear();
                warehousePositions.addAll(positions);
            }
        });


        inventoryViewModel.getInventoryDetailsResult().observe(getViewLifecycleOwner(), new Observer<List<InventoryDetailsResult>>() {
            @Override
            public void onChanged(List<InventoryDetailsResult> inventoryDetailsResult) {
                idrList = inventoryDetailsResult;
                setGauge();
            }
        });

        inventoryViewModel.getCurrentInventoryID().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null)
                    currentInventoryID = s;
            }
        });

        inventoryViewModel.getEmployeeIDLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                employeeIDDb = integer;
            }
        });

    }



    private void logicOnScan(String barcode) {

        if(barcode != null){
            if(barcode.length() != 4 && barcode.length() != 6) {
                if (isPositionSelect) {
                    int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb);
                    if (employeeID == -1) {
                        DialogBuilder.showOkDialogWithCallback(requireContext(), getResources().getString(R.string.error), getResources().getString(R.string.employee_id_invalid), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().onBackPressed();
                            }
                        });
                        return;
                    }
                    productAddByBarcode(barcode, employeeID);
                } else
                    DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.position_must_scan)).show();

            }else {
                //ovde ide kod za stavljanje na poziciju
                positionScan(barcode);
            }
        } //else{
//            Utility.showToast(getContext(), "Barkod nije moguće skenirati!");
//            DialogBuilder.showOkDialogWithoutCallback(getContext(),getResources().getString(R.string.warning), getResources().getString(R.string.barcode_cant_be_scanned)).show();
//        }

    }

    private void productAddByBarcode(String barcode, int employeeID) {
        Optional<ProductBox> tempProd = productsForInventory.stream().filter(x -> x.getProductBoxBarcode().equals(barcode)).findFirst();
        if (tempProd.isPresent()) {
            binding.searchableSpinner.setSelection(((ArrayAdapter<ProductBox>) binding.searchableSpinner.getAdapter()).getPosition(tempProd.get()));
            selectedProduct = tempProd.get();
            addProductToTempList(true, "", 1, currentPosition, employeeID);
        } else {
            DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.barcode_dont_match)).show();

        }
    }

    //TODO podpozicije potrebno dodati
    private void positionScan(String barcode){
        Optional<WarehousePosition> tempPos = warehousePositions.stream().filter(x -> x.getBarcode().equals(barcode)).findFirst();
        if(tempPos.isPresent()){
            //skenirana pozicija postoji u sistemu
            if(isPositionSelect){
                //pozicija je vec izabrana da li zelite da je promenite
                DialogBuilder.getDialogWithYesNoCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.posicion_already), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        setPositionFields(tempPos.get());
                        binding.etPosition.setText(barcode);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }else{
                //nije izabrana postavljanje nove pozicije
                setPositionFields(tempPos.get());
                binding.etPosition.setText(barcode);
            }
        }else{
            //skenirana pozicija ne postoji u sistemu
            DialogBuilder.showOkDialogWithoutCallback(getContext(),  getResources().getString(R.string.warning), getResources().getString(R.string.position_not_exist)).show();

        }
    }


    private void init() {
        gauge = binding.smallProductNumber;
        tvGauge = binding.tvSmallProductScan;

        warehousePositions = new ArrayList<WarehousePosition>();
        idrLastList = new ArrayList<InventoryDetailsResult>();

        btnReset = binding.cancelCurrentPickup;
        btnUndo = binding.undoLastPickup;

        tvGauge.setText(String.format(Locale.getDefault(), "%1d", 0));

        btnReset.setEnabled(false);
        btnUndo.setEnabled(false);
        btnUndo.setImageDrawable(getResources().getDrawable(R.drawable.ic_undo_disabled, getActivity().getTheme()));
        btnReset.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_disabled, getActivity().getTheme()));

        ProductBox placeholder =  ProductBox.newPlaceHolderInstance();
        placeholderProductList = new ArrayList<>();
        placeholderProductList.add(placeholder);
    }

    private void setGauge(){
        int numOnLocation = 0;
        int numLocal = 0;

        if(idrList != null)
            if(idrList.size() > 0)
                numOnLocation =  (int) idrList.stream().mapToDouble(o -> o.getQuantity()).sum();

        if(idrLastList != null)
            if(idrLastList.size() > 0)
                numLocal = (int) idrLastList.stream().mapToDouble(o -> o.getQuantity()).sum();

        gauge.setValue(numOnLocation + numLocal);
        gauge.setEndValue(numOnLocation + numLocal + 10);
        tvGauge.setText(String.format(Locale.getDefault(), "%1d", numLocal + numOnLocation));
    }





    private void consumeResponse(ApiResponse apiResponse) {
        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                break;
            case SUCCESS_WITH_ACTION:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar;

                if(apiResponse.error.equals("newInventory")){
                    snackbar = Snackbar.make(binding.getRoot(), "Kreiran je novi popis!", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }else if(apiResponse.error.equals("existingInventory")){
                    snackbar = Snackbar.make(binding.getRoot(), "Popis već postoji, nastavljate rad.", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }else if(apiResponse.error.equals("successPush")){
                    inventoryViewModel.syncInventoryDetailsResult(currentInventoryID);
                    snackbar = Snackbar.make(binding.getRoot(), "Uspešno ste sačuvali dosadašni rad!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case ERROR:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.error_happened), getResources().getString(R.string.error_string, apiResponse.error)).show();
                break;
            default:
                break;
        }
    }



    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    String barcode = intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data));
                    logicOnScan(barcode);

                } catch (Exception e) {
                    //  TODO obraditi gresku skeniranja
                    Utility.showToast(getContext(), e.getMessage());
                }
            }
        }
    };


    private void registerScanReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        getActivity().registerReceiver(scanReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("LIFECYCLE", "onDestroyView CALLED");
        //Sklanjanje bindinga
        binding = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("LIFECYCLE", "OnSTOP CALLED");

    }

    @Override
    public void onResume() {
        super.onResume();
        registerScanReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(scanReceiver);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                DialogBuilder.showDialogWithYesNoCallback(getContext(), getResources().getString(R.string.warning), getResources().getString(R.string.leave_inventory), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(callback.isEnabled())
                            callback.setEnabled(false);
//                        if(idrLastList.size() > 0)
//                            inventoryViewModel.pushInventoryResult(idrLastList, currentInventoryID);

                        getActivity().onBackPressed();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setBackAndUndo(List<InventoryDetailsResult> idrLastList) {
        if(idrLastList.size() == 0 || idrLastList == null){
            btnReset.setEnabled(false);
            btnUndo.setEnabled(false);
            btnUndo.setImageDrawable(getResources().getDrawable(R.drawable.ic_undo_disabled, getActivity().getTheme()));
            btnReset.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_disabled, getActivity().getTheme()));
        }else {
            btnReset.setEnabled(true);
            btnUndo.setEnabled(true);
            btnReset.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel, getActivity().getTheme()));
            btnUndo.setImageDrawable(getResources().getDrawable(R.drawable.ic_undo, getActivity().getTheme()));
        }
    }


    private void setPomLabels() {
        binding.etSelectedQuantity.requestFocus();
        binding.etSelectedQuantity.setText("");

        if(selectedProduct.isSerialMustScan())
            binding.etSerial.setVisibility(View.VISIBLE);
        else
            binding.etSerial.setVisibility(View.INVISIBLE);
    }



}
