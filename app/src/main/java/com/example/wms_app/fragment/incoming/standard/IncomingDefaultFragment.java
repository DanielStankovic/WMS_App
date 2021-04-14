package com.example.wms_app.fragment.incoming.standard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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

import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.wms_app.R;
import com.example.wms_app.adapter.ProductBoxSpinnerAdapter;
import com.example.wms_app.databinding.FragmentSingleIncomingDefaultBinding;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.enums.EnumSoftKeyboard;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.InternetCheck;
import com.example.wms_app.utilities.OnOneOffClickListener;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.incoming.standard.SingleIncomingViewModel;

import java.util.ArrayList;
import java.util.List;


public class IncomingDefaultFragment extends Fragment {

    private Context context;
    private FragmentSingleIncomingDefaultBinding binding;
    private SingleIncomingViewModel singleIncomingViewModel;
    private Incoming currentIncoming;
    private IncomingGrouped currentIncomingGrouped;
    private EnumIncomingStyle enumIncomingStyle;
    private AlertDialog errorDialog;
    private List<ProductBox> productBoxListForSpinner;
    private ArrayAdapter<ProductBox> spinnerAdapter;
    private ProductBox currentProductBox;
    private boolean isUserSelectedSpinner = true;
    private int totalProdNumber;
    private boolean isTempListEmpty = true;
    private OnBackPressedCallback callback;
    private boolean isIncomingCanceled = false;


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
                                        singleIncomingViewModel.codeScanned(barcode, binding.reservedCb.isChecked());
                                    } else {
                                        DialogBuilder.showNoInternetDialog(context);
                                    }
                                }, context);
                            } else {
                                singleIncomingViewModel.codeScanned(barcode, binding.reservedCb.isChecked());
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

    public IncomingDefaultFragment() {
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
            currentIncoming = (Incoming) getArguments().getSerializable(Constants.SELECTED_INCOMING_ID_TAG);
            currentIncomingGrouped = (IncomingGrouped) getArguments().getSerializable(Constants.SELECTED_INCOMING_GROUPED_ID_TAG);
            enumIncomingStyle = Utility.getCurrentIncomingType(currentIncoming, currentIncomingGrouped);
//            isIncomingCanceled = selectedIncoming.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED));
//            singleIncomingViewModel.setupSelectedIncoming(selectedIncoming);
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

        if (enumIncomingStyle == EnumIncomingStyle.SINGLE) {
            if (currentIncoming == null) {
                DialogBuilder.showOkDialogWithCallback(context,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.error_loading_current_incoming_leave),
                        (dialogInterface, i) -> {
                            getActivity().onBackPressed();
                        });
                return;
            }
            singleIncomingViewModel.registerFirebaseRealTimeUpdatesIncoming(currentIncoming);
        } else {
            if (currentIncomingGrouped == null) {
                DialogBuilder.showOkDialogWithCallback(context,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.error_loading_current_incoming_leave),
                        (dialogInterface, i) -> {
                            getActivity().onBackPressed();
                        });
                return;
            }
            singleIncomingViewModel.registerFirebaseRealTimeUpdatesIncomingGrouped(currentIncomingGrouped);
        }

//        //Registracija na realtime osluskivac na firebaseu
//        singleIncomingViewModel.registerRealTimeUpdatesResultDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSingleIncomingDefaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Ovde se vrsi inicijalizacija svih potrebnih objekata, listi i viewova
        initObjectsViewsAndLists(context);

        //Postavljanje adaptera za spinner sa proizvodima
        setupSpinnerAdapter();

        //Postavljanje vrednosti za brojace
        setupGaugeAndNumbers(currentIncoming, currentIncomingGrouped, enumIncomingStyle);

        //Postavljanje observera
        setupObservers();

        //Postavljanje listenera
        setupListeners();

        //Postavljanje inicijalne pozicije za predutovar
        setupInitialPosition();

        //Provera da li je nalog otkazan pa da se ide na fragment za brisanje sa firebase-a
        checkIfIncomingCanceled(currentIncoming, enumIncomingStyle);


    }

    private void checkIfIncomingCanceled(Incoming currentIncoming, EnumIncomingStyle enumIncomingStyle) {
        if (enumIncomingStyle == EnumIncomingStyle.SINGLE && currentIncoming.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED))) {

            IncomingOverviewDoneFragment fragment = new IncomingOverviewDoneFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsIncomingCanceled", true);
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.incomingNavigationHostFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            checkIfAllProductBoxesSynchronized();
        }
    }

    private void checkIfAllProductBoxesSynchronized() {
        singleIncomingViewModel.checkIfAllProductBoxesSynchronized();
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
        singleIncomingViewModel.removeFirebaseRealTimeListener();
        Log.i("LIFECYCLEAAA", "OnDetach CALLED");

        this.context = null;
    }

    private void initObjectsViewsAndLists(Context context) {

        //inicijalizacija Dijaloga za loading
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");
        productBoxListForSpinner = new ArrayList<>();
    }

    private void setupObservers() {
        //  singleIncomingViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(), this::consumeResponse);

        singleIncomingViewModel.getIncomingTruckResultLiveData().observe(getViewLifecycleOwner(), incomingTruckResults -> {
            if (incomingTruckResults != null) {
                toggleTruckIconAndLabel(incomingTruckResults.isEmpty());
            }
        });

        singleIncomingViewModel.getTotalNumberOfAddedProdMediatorLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.productNumberGaugeView.setValue(integer);
                binding.incomingProdCounterTv.setText(getResources().getString(R.string.counter_numbers, integer, totalProdNumber));
                setGaugeColor(integer >= totalProdNumber);

            }
        });

        singleIncomingViewModel.toggleUndoAndRefreshBtnLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                isTempListEmpty = aBoolean;
                binding.incomingCancelCurrentPickup.setEnabled(!aBoolean);
                binding.incomingUndoLastPickup.setEnabled(!aBoolean);
            }
        });

        singleIncomingViewModel.getProductBoxListMediatorLiveData().observe(getViewLifecycleOwner(), productBoxes -> {
            if (productBoxes != null) {
                productBoxListForSpinner.clear();
                spinnerAdapter.notifyDataSetInvalidated();
                productBoxListForSpinner.addAll(productBoxes);
                spinnerAdapter.notifyDataSetChanged();
                if (currentProductBox != null)
                    singleIncomingViewModel.refreshExpectedQty(productBoxListForSpinner.stream().filter(x -> x.getProductBoxID() == currentProductBox.getProductBoxID()).findAny().orElse(null));
            }
        });

        singleIncomingViewModel.getViewEnableHelperLiveData().observe(getViewLifecycleOwner(), viewEnableHelper -> {
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

        singleIncomingViewModel.getRequestFocusViewID().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                View viewToFocus = binding.getRoot().findViewById(integer);
                if (viewToFocus != null)
                    Utility.toggleSoftKeyboard(context, viewToFocus, EnumSoftKeyboard.SHOW);
            }
        });

        singleIncomingViewModel.getScannedProductBoxLiveData().observe(getViewLifecycleOwner(), productBox -> {
            if (productBox != null) {
                binding.incomingProdSpinner.setSelection(Utility.getSpinnerSelectionByValue(productBoxListForSpinner, productBox), false);
            }
        });

        singleIncomingViewModel.getIsIncomingFinishedLiveData().observe(getViewLifecycleOwner(), isFinished -> {
            if (isFinished) {
                DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.notice),
                        getResources().getString(R.string.incoming_finished_send_prompt),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                singleIncomingViewModel.sendIncomingToServerAndFirebase();
                                dialogInterface.dismiss();
                            }
                        });
            }
        });

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

    private void toggleTruckIconAndLabel(boolean isEmpty) {
        if (enumIncomingStyle == EnumIncomingStyle.SINGLE) {
            if (isEmpty) {
                Glide.with(context).load(R.drawable.ic_truck_black_white).fitCenter().into(binding.truckImageView);
                binding.truckLblTv.setText(getResources().getString(R.string.truck_not_selected));
            } else {
                Glide.with(context).load(R.drawable.ic_outgoing).fitCenter().into(binding.truckImageView);
                binding.truckLblTv.setText(getResources().getString(R.string.truck_selected));
            }
        } else {
            //Posto kod grupnog prijema nema transporta postavlja se logo i brise se tekst
            Glide.with(context).load(R.drawable.kolibri_logo).fitCenter().into(binding.truckImageView);
            binding.truckLblTv.setText("");
            //Kada se ocita logo premala je top margina. Ubacuje se kroz kod posto kada ima kamiona sve je ok.
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) binding.truckImageView.getLayoutParams();
            marginParams.setMargins(marginParams.leftMargin, 50, marginParams.rightMargin, marginParams.bottomMargin);
        }
    }

    private void setupListeners() {

        binding.incomingProdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentProductBox = (ProductBox) adapterView.getItemAtPosition(i);
                singleIncomingViewModel.productBoxManuallySelected(currentProductBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        binding.incomingAddProdBtn.setOnClickListener(view -> {
            singleIncomingViewModel.addProductBoxManually(
                    currentProductBox,
                    binding.incomingSrNumberEt.getText().toString().trim(),
                    binding.incomingQtyEt.getText().toString().trim(),
                    binding.reservedCb.isChecked());
        });

        binding.incomingAddNoSrNumBtn.setOnClickListener(view -> {
            singleIncomingViewModel.addProductBoxManually(
                    currentProductBox,
                    "0",
                    binding.incomingQtyEt.getText().toString().trim(),
                    binding.reservedCb.isChecked());
        });

        binding.incomingCancelCurrentPickup.setOnClickListener(view -> DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning),
                getResources().getString(R.string.delete_scanned_product_prompt), (dialogInterface, i) -> singleIncomingViewModel.removeAllFromTempList()));

        binding.incomingUndoLastPickup.setOnClickListener(view -> singleIncomingViewModel.removeLastFromTempList());

        binding.incomingPosEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.incomingPosEt.setError(null);
        });

        binding.incomingSetPosBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {

                new InternetCheck(internet -> {
                    if (internet) {
                        String barcode = binding.incomingPosEt.getText().toString().trim();
                        if (!barcode.isEmpty()) {
                            if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                                singleIncomingViewModel.codeScanned(barcode, binding.reservedCb.isChecked());
                            } else
                                binding.incomingPosEt.setError(getResources().getString(R.string.pos_must_have_num_char, Constants.POSITION_BARCODE_LENGTH));

                        } else
                            binding.incomingPosEt.setError(getResources().getString(R.string.pos_barcode_not_set));
                    } else {
                        DialogBuilder.showNoInternetDialog(context);
                    }
                }, context);
            }
        });
    }

    private void setupGaugeAndNumbers(Incoming currentIncoming, IncomingGrouped currentIncomingGrouped, EnumIncomingStyle enumIncomingStyle) {
        totalProdNumber = enumIncomingStyle == EnumIncomingStyle.SINGLE ? currentIncoming.getTotalNumOfProd() : currentIncomingGrouped.getTotalNumOfProds();
        binding.productNumberGaugeView.setEndValue(totalProdNumber);
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

    private void initViewModels() {
        singleIncomingViewModel = new ViewModelProvider(requireActivity()).get(SingleIncomingViewModel.class);
    }

    private void setupSpinnerAdapter() {
        spinnerAdapter = new ProductBoxSpinnerAdapter(context, R.layout.item_spinner_product, productBoxListForSpinner, false);
        binding.incomingProdSpinner.setTitle(getResources().getString(R.string.pick_article));
        binding.incomingProdSpinner.setPositiveButton(getResources().getString(R.string.close));
        binding.incomingProdSpinner.setAdapter(spinnerAdapter);
    }

    private void setupInitialPosition() {
        //    singleIncomingViewModel.setPositionBarcodeByWarehouseName(selectedIncoming.getPartnerWarehouseName());
    }
}
