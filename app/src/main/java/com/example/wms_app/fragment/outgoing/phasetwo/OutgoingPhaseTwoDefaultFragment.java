package com.example.wms_app.fragment.outgoing.phasetwo;

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
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoDefaultBinding;
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
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;

import java.util.ArrayList;
import java.util.List;

public class OutgoingPhaseTwoDefaultFragment extends Fragment {

    private FragmentOutgoingPhaseTwoDefaultBinding binding;
    private PhaseTwoViewModel phaseTwoViewModel;
    private Context context;
    private boolean isTempListEmpty = true;
    private OnBackPressedCallback callback;
    private AlertDialog errorDialog;
    private Outgoing currentOutgoing;
    private OutgoingGrouped currentOutgoingGrouped;
    private EnumOutgoingStyle enumOutgoingStyle;
    private ArrayAdapter<ProductBox> spinnerAdapter;
    private List<ProductBox> productBoxListForSpinner;
    private ProductBox currentProductBox;
    private String currentPositionBarcode = "";
    private int totalProdNumber;


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
                                        phaseTwoViewModel.codeScanned(barcode);
                                    } else {
                                        DialogBuilder.showNoInternetDialog(context);
                                    }
                                }, context);
                            } else {
                                phaseTwoViewModel.codeScanned(barcode);
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

    public OutgoingPhaseTwoDefaultFragment() {
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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentOutgoing = (Outgoing) getArguments().getSerializable(Constants.SELECTED_OUTGOING_ID_TAG);
            currentOutgoingGrouped = (OutgoingGrouped) getArguments().getSerializable(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG);
            enumOutgoingStyle = Utility.getCurrentOutgoingType(currentOutgoing, currentOutgoingGrouped);
        }

//        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
//            if (currentOutgoing == null) {
//                DialogBuilder.showOkDialogWithCallback(context,
//                        getResources().getString(R.string.error),
//                        getResources().getString(R.string.error_loading_current_outgoing_leave),
//                        (dialogInterface, i) -> {
//                            getActivity().onBackPressed();
//                        });
//                return;
//            }
//            phaseTwoViewModel.setupCurrentOutgoing(currentOutgoing);
//        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (!isTempListEmpty) {
                    DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning), getResources().getString(R.string.temp_list_not_empty_prompt), (dialogInterface, i) -> {
//                      TODO Proveriti da li treba zakljucavanje pozicija za predutovar u fazi 2
//                        phaseTwoViewModel.unlockPositions();
                        callback.setEnabled(false);
                        getActivity().onBackPressed();
                    });
                } else {
//                    TODO Proveriti da li treba zakljucavanje pozicija za predutovar u fazi 2
//                    phaseOneViewModel.unlockPositions();
                    callback.setEnabled(false);
                    getActivity().onBackPressed();
                }

            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            if (currentOutgoing == null) {
                DialogBuilder.showOkDialogWithCallback(context,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.error_loading_current_outgoing_leave),
                        (dialogInterface, i) -> {
                            getActivity().onBackPressed();
                        });
                return;
            }
            phaseTwoViewModel.registerFirebaseRealTimeUpdatesOutgoing(currentOutgoing);
        } else {
            if (currentOutgoingGrouped == null) {
                DialogBuilder.showOkDialogWithCallback(context,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.error_loading_current_outgoing_leave),
                        (dialogInterface, i) -> {
                            getActivity().onBackPressed();
                        });
                return;
            }
            phaseTwoViewModel.registerFirebaseRealTimeUpdatesOutgoingGrouped(currentOutgoingGrouped);
        }

        //phaseTwoViewModel.registerFirebaseRealTimeUpdates();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOutgoingPhaseTwoDefaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObjectsViewsAndLists(context);

        //Postavljanje adaptera za spinner sa proizvodima
        setupSpinnerAdapter();

        //Postavljanje vrednosti za brojace
        setupGaugeAndNumbers(currentOutgoing, currentOutgoingGrouped, enumOutgoingStyle);

        //Postavljanje observera
        setupObservers();

        //Postavljanje osluskivaca
        setupListeners();

        //Provera da li je nalog otkazan pa da se ide na fragment za brisanje sa firebase-a
        checkIfOutgoingCanceled(currentOutgoing);

    }

    private void checkIfOutgoingCanceled(Outgoing currentOutgoing) {
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE && currentOutgoing.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED))) {

            OutgoingPhaseTwoPrevDoneFragment fragment = new OutgoingPhaseTwoPrevDoneFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsOutgoingCanceled", true);
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.outgoingPhaseTwoNavigationHostFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
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

        phaseTwoViewModel.removeFirebaseRealTimeListener();

        this.context = null;
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
        phaseTwoViewModel = new ViewModelProvider(requireActivity()).get(PhaseTwoViewModel.class);
    }

    private void initObjectsViewsAndLists(Context context) {

//        //Dobijanje objekta koji je trenutna otprema
//        Outgoing currentOutgoing = getCurrentOutgoing();
//        phaseTwoViewModel.setupCurrentOutgoing(currentOutgoing);

        //inicijalizacija Dijaloga za loading
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

        productBoxListForSpinner = new ArrayList<>();
    }

//    private Outgoing getCurrentOutgoing() {
//        //Da bih radio samo sa jednim objektom pretvaram ovu grupnu otpremu u pojedinacnu. I tako cu uvek
//        //raditi sa pojedinacnom, posto mi je nebitno u fragmentu koji je tip. Inace bih morao uvek da postavljam uslov
//        //da li je SINGLE ili GROUPED
//        Outgoing outgoing = new Outgoing();
//        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
//            outgoing = this.currentOutgoing;
//        } else {
//            outgoing.setTotalNumOfProd(currentOutgoingGrouped.getTotalNumOfProds());
//            outgoing.setOutgoingDetails(currentOutgoingGrouped.getOutgoingDetailsList());
//            outgoing.setOutgoingIDList(currentOutgoingGrouped.getOutgoingIDList());
//        }
//
//        return outgoing;
//    }

    private void setupSpinnerAdapter() {
        spinnerAdapter = new ProductBoxSpinnerAdapter(context, R.layout.item_spinner_product, productBoxListForSpinner, true);
        binding.outgoingPhaseTwoProdSpinner.setTitle(getResources().getString(R.string.pick_article));
        binding.outgoingPhaseTwoProdSpinner.setPositiveButton(getResources().getString(R.string.close));
        binding.outgoingPhaseTwoProdSpinner.setAdapter(spinnerAdapter);
    }

    private void setupObservers() {

        phaseTwoViewModel.toggleUndoAndRefreshBtnLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                isTempListEmpty = aBoolean;
                binding.outgoingPhaseTwoCancelCurrentPickup.setEnabled(!aBoolean);
                binding.outgoingPhaseTwoUndoLastPickup.setEnabled(!aBoolean);
            }
        });

        phaseTwoViewModel.getProductBoxListMediatorLiveData().observe(getViewLifecycleOwner(), productBoxes -> {
            if (productBoxes != null) {
                productBoxListForSpinner.clear();
                spinnerAdapter.notifyDataSetInvalidated();
                productBoxListForSpinner.addAll(productBoxes);
                spinnerAdapter.notifyDataSetChanged();
                phaseTwoViewModel.refreshExpectedQty(productBoxListForSpinner.get(0));
            }
        });

        phaseTwoViewModel.getViewEnableHelperLiveData().observe(getViewLifecycleOwner(), viewEnableHelper -> {
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

        phaseTwoViewModel.getRequestFocusViewID().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                View viewToFocus = binding.getRoot().findViewById(integer);
                if (viewToFocus != null)
                    Utility.toggleSoftKeyboard(context, viewToFocus, EnumSoftKeyboard.SHOW);
            }
        });

        phaseTwoViewModel.getScannedProductBoxLiveData().observe(getViewLifecycleOwner(), productBox -> {
            if (productBox != null) {
                binding.outgoingPhaseTwoProdSpinner.setSelection(Utility.getSpinnerSelectionByValue(productBoxListForSpinner, productBox), false);
            }
        });

        phaseTwoViewModel.getPositionBarcode().observe(getViewLifecycleOwner(), barcode -> {
            if (barcode != null) {
                currentPositionBarcode = barcode;
                togglePositionViews(barcode);
            } else {
                currentPositionBarcode = "";
            }
        });

        phaseTwoViewModel.getOutgoingTruckResultLiveData().observe(getViewLifecycleOwner(), incomingTruckResults -> {
            if (incomingTruckResults != null) {
                toggleTruckIconAndLabel(incomingTruckResults.isEmpty());
            }
        });

        phaseTwoViewModel.getTotalNumberOfAddedProdMediatorLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.productNumberGaugeView.setValue(integer);
                binding.outgoingPhaseTwoProdCounterTv.setText(getResources().getString(R.string.counter_numbers, integer, totalProdNumber));
                setGaugeColor(integer >= totalProdNumber);

            }
        });

        phaseTwoViewModel.getIsOutgoingFinishedLiveData().observe(getViewLifecycleOwner(), isFinished -> {
            if (isFinished) {
                DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.notice),
                        getResources().getString(R.string.outgoing_finished_send_prompt),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                phaseTwoViewModel.sendOutgoingToServer();
                                dialogInterface.dismiss();
                            }
                        });
            }
        });
    }

    private void togglePositionViews(String barcode) {
        binding.outgoingPhaseTwoPosEt.setText(barcode);
        if (barcode.isEmpty()) {
            binding.outgoingPhaseTwoPosEt.setEnabled(true);
            binding.outgoingPhaseTwoSetPosBtn.setText(getResources().getString(R.string.set));
        } else {
            binding.outgoingPhaseTwoPosEt.setEnabled(false);
            binding.outgoingPhaseTwoSetPosBtn.setText(getResources().getString(R.string.delete));
        }
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
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            if (isEmpty) {
                Glide.with(context).load(R.drawable.ic_truck_black_white).fitCenter().into(binding.truckImageView);
                binding.truckLblTv.setText(getResources().getString(R.string.truck_not_selected));
            } else {
                Glide.with(context).load(R.drawable.ic_outgoing).fitCenter().into(binding.truckImageView);
                binding.truckLblTv.setText(getResources().getString(R.string.truck_selected));
            }
        } else {
            //Posto kod grupne otpreme nema transporta postavlja se logo i brise se tekst
            Glide.with(context).load(R.drawable.kolibri_logo).fitCenter().into(binding.truckImageView);
            binding.truckLblTv.setText("");
            //Kada se ocita logo premala je top margina. Ubacuje se kroz kod posto kada ima kamiona sve je ok.
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) binding.truckImageView.getLayoutParams();
            marginParams.setMargins(marginParams.leftMargin, 50, marginParams.rightMargin, marginParams.bottomMargin);

        }
    }

    private void setupListeners() {
        binding.outgoingPhaseTwoProdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentProductBox = (ProductBox) adapterView.getItemAtPosition(i);
                phaseTwoViewModel.productBoxManuallySelected(currentProductBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.outgoingPhaseTwoSetPosBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (currentPositionBarcode.isEmpty()) {
                    String barcode = binding.outgoingPhaseTwoPosEt.getText().toString().trim();
                    if (!barcode.isEmpty()) {
                        if (barcode.length() == Constants.POSITION_BARCODE_LENGTH) {
                            new InternetCheck(internet -> {
                                if (internet) {
                                    phaseTwoViewModel.positionWithArticlesSelected(barcode);
                                } else {
                                    DialogBuilder.showNoInternetDialog(context);
                                }
                            }, context);

                        } else
                            binding.outgoingPhaseTwoPosEt.setError(getResources().getString(R.string.pos_must_have_num_of_chars, Constants.POSITION_BARCODE_LENGTH));
                    } else
                        binding.outgoingPhaseTwoPosEt.setError(getResources().getString(R.string.pos_barcode_not_set));
                } else {
                    phaseTwoViewModel.resetPositionBarcode();
                }
            }
        });

        binding.outgoingPhaseTwoPosEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.outgoingPhaseTwoPosEt.setError(null);
        });

        binding.outgoingPhaseTwoCancelCurrentPickup.setOnClickListener(view ->
                DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning),
                        getResources().getString(R.string.delete_scanned_product_prompt),
                        (dialogInterface, i) ->
                                phaseTwoViewModel.removeAllFromTempList()));

        binding.outgoingPhaseTwoUndoLastPickup.setOnClickListener(view ->
                phaseTwoViewModel.removeLastFromTempList());


        binding.outgoingPhaseTwoAddProdBtn.setOnClickListener(view ->
                phaseTwoViewModel.addProductBoxManually(currentProductBox,
                        binding.outgoingPhaseTwoSrNumberEt.getText().toString().trim(),
                        binding.outgoingPhaseTwoQtyEt.getText().toString().trim()));

        binding.outgoingPhaseTwoAddNoSrNumBtn.setOnClickListener(view ->
                phaseTwoViewModel.addProductBoxManually(currentProductBox,
                        "0",
                        binding.outgoingPhaseTwoQtyEt.getText().toString().trim()));

        binding.outgoingPhaseTwoLoadBtn.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                phaseTwoViewModel.pushTempListToFirebase();
            }
        });
    }

    private void setupGaugeAndNumbers(Outgoing outgoing, OutgoingGrouped outgoingGrouped, EnumOutgoingStyle enumOutgoingStyle) {
        totalProdNumber = enumOutgoingStyle == EnumOutgoingStyle.SINGLE ? outgoing.getTotalNumOfProd() : outgoingGrouped.getTotalNumOfProds();
        binding.productNumberGaugeView.setEndValue(totalProdNumber);
    }
}