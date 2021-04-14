package com.example.wms_app.fragment.incoming.production;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.wms_app.R;
import com.example.wms_app.adapter.ProductBoxSpinnerAdapter;
import com.example.wms_app.databinding.FragmentIncomingProductionDefaultBinding;
import com.example.wms_app.enums.EnumSoftKeyboard;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.InternetCheck;
import com.example.wms_app.utilities.OnOneOffClickListener;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.incoming.production.IncomingProductionViewModel;

import java.util.ArrayList;
import java.util.List;

public class IncomingProductionDefaultFragment extends Fragment {

    private FragmentIncomingProductionDefaultBinding binding;
    private Context context;
    private IncomingProductionViewModel incomingProductionViewModel;
    private AlertDialog errorDialog;
    private List<ProductBox> productBoxListForSpinner;
    private ArrayAdapter<ProductBox> spinnerAdapter;
    private ProductBox currentProductBox;
    private int totalProdNumber;
    private int totalAddedProdNumber;
    private String selectedProductionTypeCode;
    private boolean isTempListEmpty = true;
    private OnBackPressedCallback callback;

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (errorDialog != null && !errorDialog.isShowing()) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                        //  Received a barcode scan
                        try {
                            String barcode = intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data));
                            if (barcode != null && barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                                //OVde se stavlja provera da li ima interneta u trenutku skeniranja, ali samo kada se radi o poziciji
                                new InternetCheck(internet -> {
                                    if (internet) {
                                        incomingProductionViewModel.codeScanned(barcode, binding.reservedProductionCb.isChecked());
                                    } else {
                                        DialogBuilder.showNoInternetDialog(context);
                                    }
                                }, context);
                            } else {
                                incomingProductionViewModel.codeScanned(barcode, binding.reservedProductionCb.isChecked());
                            }
                        } catch (Exception e) {
                            errorDialog.setMessage(getResources().getString(R.string.scanning_error, e.getMessage()));
                            errorDialog.show();
                        }
                    }
                }
            }
        }
    };


    public IncomingProductionDefaultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        Log.i("LIFECYCLEAAA", "OnAttach CALLED");

        //Registrovanje skenera za barkod
        registerScanReceiver(context);


        //Dobijanje viewmodela
        initViewModels();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            selectedProductionTypeCode = getArguments().getString(Constants.SELECTED_PRODUCTION_TYPE_TAG);
        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (!isTempListEmpty) {
                    DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning), getResources().getString(R.string.temp_list_not_empty_prompt), (dialogInterface, i) -> {
                        callback.setEnabled(false);
                        getActivity().onBackPressed();
                    });
                } else {
                    callback.setEnabled(false);
                    getActivity().onBackPressed();
                }

            }
        };

        getActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //Registracija na realtime osluskivac na firebaseu
        incomingProductionViewModel.registerRealTimeUpdates(selectedProductionTypeCode);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIncomingProductionDefaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Ovde se vrsi inicijalizacija svih potrebnih objekata, listi i viewova
        initObjectsViewsAndLists(context);

        //Postavljanje observera
        setupObservers();

        //Postavljanje listenera
        setupListeners();

        //Postavljanje adaptera za spinner sa proizvodima
        setupSpinnerAdapter();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("LIFECYCLEAAA", "onDestroyView CALLED");
        //Sklanjanje bindinga
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.unregisterReceiver(scanReceiver);
        //Sklanjanje registracija na realtime osluskivac na firebaseu
        incomingProductionViewModel.removeFirebaseRealTimeListener();
        Log.i("LIFECYCLEAAA", "OnDetach CALLED");

        this.context = null;
    }

    private void initObjectsViewsAndLists(Context context) {

        //inicijalizacija Dijaloga za loading
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");
        productBoxListForSpinner = new ArrayList<>();
    }

    private void setupObservers() {
        // incomingProductionViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(),this::consumeResponse);

        incomingProductionViewModel.getTotalNumberOfProdLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                setupGaugeAndNumbers(integer);
                binding.incomingProdCounterTv.setText(getResources().getString(R.string.counter_numbers, totalAddedProdNumber, integer));
            }
        });

        incomingProductionViewModel.getIncomingDetailsResultLiveData().observe(getViewLifecycleOwner(), incomingDetailsResults -> {
            if (incomingDetailsResults != null) {

            }
        });

        incomingProductionViewModel.getTotalNumberOfAddedProdMediatorLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                totalAddedProdNumber = integer;
                binding.productNumberGaugeView.setValue(integer);
                binding.incomingProdCounterTv.setText(getResources().getString(R.string.counter_numbers, integer, totalProdNumber));
                setGaugeColor(integer >= totalProdNumber);
            }
        });

        incomingProductionViewModel.toggleUndoAndRefreshBtnLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                isTempListEmpty = aBoolean;
                binding.incomingProductionCancelCurrentPickup.setEnabled(!aBoolean);
                binding.incomingProductionUndoLastPickup.setEnabled(!aBoolean);
            }
        });

        incomingProductionViewModel.getProductBoxListMediatorLiveData().observe(getViewLifecycleOwner(), productBoxes -> {
            if (productBoxes != null) {
                productBoxListForSpinner.clear();
                productBoxListForSpinner.addAll(productBoxes);
                spinnerAdapter.notifyDataSetChanged();
                if (currentProductBox != null)
                    incomingProductionViewModel.refreshExpectedQty(productBoxListForSpinner.stream().filter(x -> x.getProductBoxID() == currentProductBox.getProductBoxID()).findAny().orElse(null));

            }
        });

        incomingProductionViewModel.getViewEnableHelperLiveData().observe(getViewLifecycleOwner(), viewEnableHelper -> {
            if (viewEnableHelper != null) {
                if (viewEnableHelper.getEnumViewType() == EnumViewType.EDIT_TEXT) {
                    EditText viewToChange = binding.getRoot().findViewById(viewEnableHelper.getViewID());
                    viewToChange.setText(viewEnableHelper.getViewText());
                    viewToChange.setEnabled(viewEnableHelper.isEnabled());
                    viewToChange.setVisibility(viewEnableHelper.getmViewVisibility());
                } else if (viewEnableHelper.getEnumViewType() == EnumViewType.IMAGE_VIEW) {
                    ImageView viewToChange = binding.getRoot().findViewById(viewEnableHelper.getViewID());
                    viewToChange.setEnabled(viewEnableHelper.isEnabled());
                } else if (viewEnableHelper.getEnumViewType() == EnumViewType.BUTTON) {
                    Button viewToChange = binding.getRoot().findViewById(viewEnableHelper.getViewID());
                    viewToChange.setVisibility(viewEnableHelper.getmViewVisibility());
                } else if (viewEnableHelper.getEnumViewType() == EnumViewType.CHECK_BOX) {
                    MaterialCheckBox viewToChange = binding.getRoot().findViewById(viewEnableHelper.getViewID());
                    viewToChange.setChecked(viewEnableHelper.isEnabled());
                }
            }
        });

        incomingProductionViewModel.getRequestFocusViewID().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                View viewToFocus = binding.getRoot().findViewById(integer);
                if (viewToFocus != null) {
                    Utility.toggleSoftKeyboard(context, viewToFocus, EnumSoftKeyboard.SHOW);
                }
            }
        });

        incomingProductionViewModel.getScannedProductBoxLiveData().observe(getViewLifecycleOwner(), productBox -> {
            if (productBox != null) {
                binding.incomingProdSpinner.setSelection(Utility.getSpinnerSelectionByValue(productBoxListForSpinner, productBox), false);
            }
        });

    }

    private void setupListeners() {
        binding.incomingProdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentProductBox = (ProductBox) adapterView.getItemAtPosition(i);
                incomingProductionViewModel.productBoxManuallySelected(currentProductBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.incomingAddProdBtn.setOnClickListener(view -> {
            incomingProductionViewModel.addProductBoxManually(
                    currentProductBox,
                    binding.incomingProductionSrNumberEt.getText().toString().trim(),
                    binding.incomingProductionQtyEt.getText().toString().trim(),
                    binding.reservedProductionCb.isChecked());
        });

        binding.incomingProductionAddNoSrNumBtn.setOnClickListener(view -> {
            incomingProductionViewModel.addProductBoxManually(
                    currentProductBox,
                    "0",
                    binding.incomingProductionQtyEt.getText().toString().trim(),
                    binding.reservedProductionCb.isChecked());
        });

        binding.incomingProductionCancelCurrentPickup.setOnClickListener(view -> DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning),
                getResources().getString(R.string.delete_scanned_product_prompt), (dialogInterface, i) -> incomingProductionViewModel.removeAllFromTempList()));

        binding.incomingProductionUndoLastPickup.setOnClickListener(view -> incomingProductionViewModel.removeLastFromTempList());

        binding.incomingProductionPosEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.incomingProductionPosEt.setError(null);
        });

//        binding.incomingSetPosBtn.setOnClickListener(view -> {
//            new InternetCheck(internet -> {
//                if (internet) {
//                    String barcode = binding.incomingProductionPosEt.getText().toString().trim();
//                    if (!barcode.isEmpty()) {
//                        if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
//                            incomingProductionViewModel.codeScanned(barcode, binding.reservedProductionCb.isChecked());
//                        } else
//                            binding.incomingProductionPosEt.setError(getResources().getString(R.string.pos_must_have_num_char, Constants.POSITION_BARCODE_LENGTH));
//
//                    } else
//                        binding.incomingProductionPosEt.setError(getResources().getString(R.string.pos_barcode_not_set));
//                } else {
//                    DialogBuilder.showNoInternetDialog(context);
//                }
//            }, context);
//        });

        binding.incomingSetPosBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                new InternetCheck(internet -> {
                    if (internet) {
                        String barcode = binding.incomingProductionPosEt.getText().toString().trim();
                        if (!barcode.isEmpty()) {
                            if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                                incomingProductionViewModel.codeScanned(barcode, binding.reservedProductionCb.isChecked());
                            } else
                                binding.incomingProductionPosEt.setError(getResources().getString(R.string.pos_must_have_num_char, Constants.POSITION_BARCODE_LENGTH));

                        } else
                            binding.incomingProductionPosEt.setError(getResources().getString(R.string.pos_barcode_not_set));
                    } else {
                        DialogBuilder.showNoInternetDialog(context);
                    }
                }, context);
            }
        });
    }

    private void setupGaugeAndNumbers(int totalProdNum) {
        totalProdNumber = totalProdNum;
        binding.productNumberGaugeView.setEndValue(totalProdNum);
    }

    private void setGaugeColor(boolean allProductAdded) {
        int startColor;
        int endColor;
        if (allProductAdded) {
            startColor = ContextCompat.getColor(context, R.color.light_green);
            endColor = ContextCompat.getColor(context, R.color.dark_green);
        } else {
            startColor = ContextCompat.getColor(context, R.color.colorAccent);
            endColor = ContextCompat.getColor(context, R.color.colorAccentDark);
        }

        binding.productNumberGaugeView.setPointStartColor(startColor);
        binding.productNumberGaugeView.setPointEndColor(endColor);
    }

    private void initViewModels() {
        incomingProductionViewModel = new ViewModelProvider(requireActivity()).get(IncomingProductionViewModel.class);
    }

    private void registerScanReceiver(Context context) {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
            context.registerReceiver(scanReceiver, filter);
        } catch (Exception ex) {
            ErrorClass.handle(ex, getActivity());
        }
    }

    private void setupSpinnerAdapter() {
        spinnerAdapter = new ProductBoxSpinnerAdapter(context, R.layout.item_spinner_product, productBoxListForSpinner, false);
        binding.incomingProdSpinner.setTitle(getResources().getString(R.string.pick_article));
        binding.incomingProdSpinner.setPositiveButton(getResources().getString(R.string.close));
        binding.incomingProdSpinner.setAdapter(spinnerAdapter);
    }

}
