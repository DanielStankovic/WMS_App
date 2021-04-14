package com.example.wms_app.fragment.outgoing.phaseone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import com.example.wms_app.databinding.FragmentOutgoingPhaseOneDefaultBinding;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.enums.EnumSoftKeyboard;
import com.example.wms_app.enums.EnumViewType;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.InternetCheck;
import com.example.wms_app.utilities.OnOneOffClickListener;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.outgoing.phaseone.PhaseOneViewModel;

import java.util.ArrayList;
import java.util.List;


public class OutgoingPhaseOneDefaultFragment extends Fragment {

    private FragmentOutgoingPhaseOneDefaultBinding binding;
    private PhaseOneViewModel phaseOneViewModel;
    private Context context;
    private boolean isTempListEmpty = true;
    private OnBackPressedCallback callback;
    private AlertDialog errorDialog;
    private Outgoing outgoing;
    private OutgoingGrouped outgoingGrouped;
    private EnumOutgoingStyle enumOutgoingStyle;
    private ArrayAdapter<ProductBox> spinnerAdapter;
    private List<ProductBox> productBoxListForSpinner;
    private ProductBox currentProductBox;
    private String currentPositionBarcode = "";

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
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
                                        phaseOneViewModel.codeScanned(barcode);
                                    } else {
                                        DialogBuilder.showNoInternetDialog(context);
                                    }
                                }, context);
                            } else {
                                phaseOneViewModel.codeScanned(barcode);
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


    public OutgoingPhaseOneDefaultFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        //Registrovanje skenera za barkod
        registerScanReceiver(context);

        //Dobijanje viewmodela
        initViewModels();

        //Postavljanje osluskivaca za sve pozicije za preduotvar. Postavljam ga ovde
        //da se ne bi postavljao svaki put kada odem na drugi fragment pa se vratim ovde.
        //Osluskivac za pojedinacnu otpremu ostaje u onCreate zato sto mi treba enum odatle.
        phaseOneViewModel.registerFirebaseRealTimeUpdatesPreloadingPos();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            outgoing = (Outgoing) getArguments().getSerializable(Constants.SELECTED_OUTGOING_ID_TAG);
            outgoingGrouped = (OutgoingGrouped) getArguments().getSerializable(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG);
            enumOutgoingStyle = Utility.getCurrentOutgoingType(outgoing, outgoingGrouped);
        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (!isTempListEmpty) {
                    DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning), getResources().getString(R.string.temp_list_not_empty_prompt), (dialogInterface, i) -> {
                        phaseOneViewModel.unlockPositions();
                        callback.setEnabled(false);
                        getActivity().onBackPressed();
                    });
                } else {
                    phaseOneViewModel.unlockPositions();
                    callback.setEnabled(false);
                    getActivity().onBackPressed();
                }

            }
        };

        getActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            if (outgoing == null) {
                DialogBuilder.showOkDialogWithCallback(context,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.error_loading_current_outgoing_leave),
                        (dialogInterface, i) -> {
                            getActivity().onBackPressed();
                        });
                return;
            }
            phaseOneViewModel.registerFirebaseRealTimeUpdatesOutgoing(outgoing);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOutgoingPhaseOneDefaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObjectsViewsAndLists(context);

        //Postavljanje adaptera za spinner sa proizvodima
        setupSpinnerAdapter();

        //Postavljanje observera
        setupObservers();

        //Postavljanje osluskivaca
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Sklanjanje bindinga
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.unregisterReceiver(scanReceiver);

        phaseOneViewModel.removeFirebaseRealTimeListener();

        this.context = null;
    }

    private void setupObservers() {

        phaseOneViewModel.toggleUndoAndRefreshBtnLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                isTempListEmpty = aBoolean;
                binding.outgoingPhaseOneCancelCurrentPickup.setEnabled(!aBoolean);
                binding.outgoingPhaseOneUndoLastPickup.setEnabled(!aBoolean);
            }
        });

        phaseOneViewModel.getProductBoxListMediatorLiveData().observe(getViewLifecycleOwner(), productBoxes -> {
            if (productBoxes != null) {
                productBoxListForSpinner.clear();
                spinnerAdapter.notifyDataSetInvalidated();
                productBoxListForSpinner.addAll(productBoxes);
                spinnerAdapter.notifyDataSetChanged();
                phaseOneViewModel.refreshExpectedQty(productBoxListForSpinner.get(0));
            }
        });

        phaseOneViewModel.getViewEnableHelperLiveData().observe(getViewLifecycleOwner(), viewEnableHelper -> {
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

        phaseOneViewModel.getRequestFocusViewID().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                View viewToFocus = binding.getRoot().findViewById(integer);
                if (viewToFocus != null)
                    Utility.toggleSoftKeyboard(context, viewToFocus, EnumSoftKeyboard.SHOW);
            }
        });

        phaseOneViewModel.getScannedProductBoxLiveData().observe(getViewLifecycleOwner(), productBox -> {
            if (productBox != null) {
                binding.outgoingPhaseOneSpinnerProduct.setSelection(Utility.getSpinnerSelectionByValue(productBoxListForSpinner, productBox), false);
            }
        });

        phaseOneViewModel.getPositionBarcode().observe(getViewLifecycleOwner(), barcode -> {
            if (barcode != null) {
                currentPositionBarcode = barcode;
                togglePositionViews(barcode);
            } else {
                currentPositionBarcode = "";
            }
        });
    }

    private void togglePositionViews(String barcode) {
        binding.outgoingPhaseOnePosEt.setText(barcode);
        if (barcode.isEmpty()) {
            binding.outgoingPhaseOnePosEt.setEnabled(true);
            binding.outgoingPhaseOnePosBtn.setText(getResources().getString(R.string.set));
        } else {
            binding.outgoingPhaseOnePosEt.setEnabled(false);
            binding.outgoingPhaseOnePosBtn.setText(getResources().getString(R.string.delete));
        }
    }

    private void setupListeners() {

        binding.outgoingPhaseOneSpinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentProductBox = (ProductBox) adapterView.getItemAtPosition(i);
                phaseOneViewModel.productBoxManuallySelected(currentProductBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.outgoingPhaseOnePosBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (currentPositionBarcode.isEmpty()) {
                    String barcode = binding.outgoingPhaseOnePosEt.getText().toString().trim();
                    if (!barcode.isEmpty()) {
                        if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                            new InternetCheck(internet -> {
                                if (internet) {
                                    phaseOneViewModel.positionWithArticlesSelected(barcode);
                                } else {
                                    DialogBuilder.showNoInternetDialog(context);
                                }
                            }, context);

                        } else
                            binding.outgoingPhaseOnePosEt.setError(getResources().getString(R.string.pos_must_have_num_of_chars, Constants.POSITION_BARCODE_LENGTH));
                    } else
                        binding.outgoingPhaseOnePosEt.setError(getResources().getString(R.string.pos_barcode_not_set));
                } else {
                    phaseOneViewModel.resetPositionBarcode();
                }
            }
        });

        binding.outgoingPhaseOnePosPreloadingBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                String barcode = binding.outgoingPhaseOnePosPreloadingEt.getText().toString().trim();
                if (!barcode.isEmpty()) {
                    if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                        new InternetCheck(internet -> {
                            if (internet) {
                                phaseOneViewModel.positionForPreloadingSelected(barcode);
                            } else {
                                DialogBuilder.showNoInternetDialog(context);
                            }
                        }, context);

                    } else
                        binding.outgoingPhaseOnePosPreloadingEt.setError(getResources().getString(R.string.pos_must_have_num_of_chars, Constants.POSITION_BARCODE_LENGTH));
                } else
                    binding.outgoingPhaseOnePosPreloadingEt.setError(getResources().getString(R.string.pos_barcode_not_set));
            }
        });

        binding.outgoingPhaseOnePosEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.outgoingPhaseOnePosEt.setError(null);
        });

        binding.outgoingPhaseOnePosPreloadingEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.outgoingPhaseOnePosPreloadingEt.setError(null);
        });

        binding.outgoingPhaseOneCancelCurrentPickup.setOnClickListener(view ->
                DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning),
                        getResources().getString(R.string.delete_scanned_product_prompt),
                        (dialogInterface, i) ->
                                phaseOneViewModel.removeAllFromTempList()));

        binding.outgoingPhaseOneUndoLastPickup.setOnClickListener(view ->
                phaseOneViewModel.removeLastFromTempList());

        binding.outgoingPhaseOneAddBtn.setOnClickListener(view ->
                phaseOneViewModel.addProductBoxManually(currentProductBox,
                        binding.outgoingPhaseOneSerialEt.getText().toString().trim(),
                        binding.outgoingPhaseOneSelectedQtyEt.getText().toString().trim()));

        binding.outgoingPhaseOneAddNoSrNumBtn.setOnClickListener(view ->
                phaseOneViewModel.addProductBoxManually(currentProductBox,
                        "0",
                        binding.outgoingPhaseOneSelectedQtyEt.getText().toString().trim()));


    }

    private void initViewModels() {
        phaseOneViewModel = new ViewModelProvider(requireActivity()).get(PhaseOneViewModel.class);
    }

    private void initObjectsViewsAndLists(Context context) {
        //Dobijanje objekta koji je trenutna otprema
        Outgoing currentOutgoing = getCurrentOutgoing();
        phaseOneViewModel.setupCurrentOutgoing(currentOutgoing);

        //inicijalizacija Dijaloga za loading
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

        productBoxListForSpinner = new ArrayList<>();
    }

    private Outgoing getCurrentOutgoing() {
        //Da bih radio samo sa jednim objektom pretvaram ovu grupnu otpremu u pojedinacnu. I tako cu uvek
        //raditi sa pojedinacnom, posto mi je nebitno u fragmentu koji je tip. Inace bih morao uvek da postavljam uslov
        //da li je SINGLE ili GROUPED
        Outgoing outgoing = new Outgoing();
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            outgoing = this.outgoing;
        } else {
            outgoing.setTotalNumOfProd(outgoingGrouped.getTotalNumOfProds());
            outgoing.setOutgoingDetails(outgoingGrouped.getOutgoingDetailsList());
        }

        return outgoing;
    }

    private void setupSpinnerAdapter() {
        spinnerAdapter = new ProductBoxSpinnerAdapter(context, R.layout.item_spinner_product, productBoxListForSpinner, false);
        binding.outgoingPhaseOneSpinnerProduct.setTitle(getResources().getString(R.string.pick_article));
        binding.outgoingPhaseOneSpinnerProduct.setPositiveButton(getResources().getString(R.string.close));
        binding.outgoingPhaseOneSpinnerProduct.setAdapter(spinnerAdapter);
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

}