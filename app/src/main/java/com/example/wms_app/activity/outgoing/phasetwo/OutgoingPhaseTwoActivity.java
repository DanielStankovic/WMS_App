package com.example.wms_app.activity.outgoing.phasetwo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.wms_app.R;
import com.example.wms_app.adapter.DocumentStyleAdapter;
import com.example.wms_app.adapter.outgoing.OutgoingAdapterGrouped;
import com.example.wms_app.adapter.outgoing.OutgoingAdapterSingle;
import com.example.wms_app.databinding.ActivityOutgoingPhaseTwoBinding;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.ConsumeResponse;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.mapper.AsyncGetGroupedOutgoing;
import com.example.wms_app.viewmodel.outgoing.phasetwo.OutgoingPhaseTwoActivityViewModel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OutgoingPhaseTwoActivity extends AppCompatActivity implements OutgoingAdapterSingle.OutgoingAdapterSingleListener, OutgoingAdapterGrouped.OutgoingAdapterGroupedListener {
    private ActivityOutgoingPhaseTwoBinding binding;
    private OutgoingPhaseTwoActivityViewModel outgoingPhaseTwoActivityViewModel;
    private AlertDialog errorDialog;
    private AlertDialog loadingDialog;
    private Date dateTo;
    private ArrayAdapter<String> cityAdapter;
    private List<String> cityList;
    private FilterDataHolder mFilterDataHolder;
    private OutgoingAdapterSingle adapterSingle;
    private OutgoingAdapterGrouped adapterGrouped;
    private List<String> warehouseList;
    private ArrayAdapter<String> warehouseAdapter;
    private EnumOutgoingStyle currentOutgoingStyle = EnumOutgoingStyle.SINGLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initToolbar();
        initViewModel();
        initDialog();
        initAdapter();
        initCityAdapter();
        setupOutgoingStyleSpinner();
        setupObservers();
        setupListeners();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterRealTimeFirestoreUpdates();
        binding = null;
    }

    private void initBinding() {
        binding = ActivityOutgoingPhaseTwoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void initViewModel() {
        outgoingPhaseTwoActivityViewModel = new ViewModelProvider(this).get(OutgoingPhaseTwoActivityViewModel.class);
    }

    private void initDialog() {
        loadingDialog = DialogBuilder.getLoadingDialog(OutgoingPhaseTwoActivity.this);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(OutgoingPhaseTwoActivity.this, getResources().getString(R.string.error_happened), "");
    }

    private void initAdapter() {
        adapterSingle = new OutgoingAdapterSingle(OutgoingPhaseTwoActivity.this, this);
        binding.outgoingRv.setHasFixedSize(true);
        binding.outgoingRv.setAdapter(adapterSingle);
    }

    private void initCityAdapter() {
        cityList = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(OutgoingPhaseTwoActivity.this, android.R.layout.simple_dropdown_item_1line, cityList);
        warehouseList = new ArrayList<>();
        warehouseAdapter = new ArrayAdapter<>(OutgoingPhaseTwoActivity.this, android.R.layout.simple_dropdown_item_1line, warehouseList);
    }

    private void setupOutgoingStyleSpinner() {
        String[] outgoingStyleArray = getResources().getStringArray(R.array.outgoing_style);
        DocumentStyleAdapter outgoingStyleAdapter = new DocumentStyleAdapter(OutgoingPhaseTwoActivity.this, R.layout.item_spinner_doc_type, outgoingStyleArray);
        binding.outgoingStyleSpinner.setAdapter(outgoingStyleAdapter);
    }

    private void setupObservers() {
        outgoingPhaseTwoActivityViewModel.getApiResponseLiveData().observe(this, apiResponse -> {
            if (apiResponse != null)
                ConsumeResponse.consumeResponse(apiResponse,
                        loadingDialog,
                        errorDialog,
                        getResources(),
                        binding,
                        OutgoingPhaseTwoActivity.this,
                        () -> outgoingPhaseTwoActivityViewModel.refreshApiResponseStatus()
                );
            //consumeResponse(apiResponse);
        });

        outgoingPhaseTwoActivityViewModel.getOutgoingMediatorLiveData().observe(this, outgoings -> {
            if (outgoings != null) {
                toggleRecyclerView(outgoings.size());
                //dapter.setOutgoingList(outgoings);
                if (currentOutgoingStyle == EnumOutgoingStyle.SINGLE) {
                    //Ako je pojedinacna otprema ide ovaj deo
                    binding.outgoingRv.setAdapter(adapterSingle);
                    adapterSingle.setOutgoingList(outgoings);
                } else {
                    //Ako je grupna otprema ide ovaj deo
                    if (adapterGrouped == null)
                        adapterGrouped = new OutgoingAdapterGrouped(OutgoingPhaseTwoActivity.this, this);
                    //Ovde ide kreiranje objekta za grupnu otpremu na osnovu dobijene liste
                    setGroupedOutgoing(outgoings);
                }

            }
        });

        outgoingPhaseTwoActivityViewModel.getFilterDateLiveData().observe(this, date -> {
            if (date != null) {
                dateTo = date;
                registerRealTimeFirestoreUpdates(date);
                // syncOutgoing(date);
            }
        });

        outgoingPhaseTwoActivityViewModel.getFilterDataHolderLiveData().observe(this, filterDataHolder -> {
            if (filterDataHolder != null) {
                mFilterDataHolder = filterDataHolder;
            }
        });

        outgoingPhaseTwoActivityViewModel.getCitiesLiveData().observe(this, cities -> {
            if (cities != null) {
                cityList.clear();
                cityList.addAll(cities);
                cityAdapter.notifyDataSetChanged();
            }
        });

        outgoingPhaseTwoActivityViewModel.getWarehousesLiveData().observe(this, warehouses -> {
            if (warehouses != null) {
                warehouseList.clear();
                warehouseList.addAll(warehouses);
                warehouseAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setGroupedOutgoing(List<Outgoing> outgoings) {

        if (!outgoings.isEmpty()) {
            new AsyncGetGroupedOutgoing(outgoingGrouped -> {
                try {
                    if (outgoingGrouped == null) {
                        throw new Exception(getResources().getString(R.string.no_prod_in_local_db));
                    }
                    adapterGrouped.setOutgoingGrouped(outgoingGrouped);
                    binding.outgoingRv.setAdapter(adapterGrouped);

                } catch (Exception ex) {
                    ErrorClass.handle(ex, OutgoingPhaseTwoActivity.this);
                }
            }, OutgoingPhaseTwoActivity.this, outgoings, true, dateTo);
        }
    }

    private void setupListeners() {
        binding.dateFilterBtn.setOnClickListener(view -> {
            showDateFilterDialog();
        });

        binding.filterBtn.setOnClickListener(view -> {
            showFilterDialog();
        });

        binding.outgoingStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentOutgoingStyle = i == 0 ? EnumOutgoingStyle.SINGLE : EnumOutgoingStyle.GROUPED;
                outgoingPhaseTwoActivityViewModel.setOutgoingStyle(currentOutgoingStyle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.sendAllBtn.setOnClickListener(view -> {
            outgoingPhaseTwoActivityViewModel.sendAllOutgoing();
        });
    }

    private void showDateFilterDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(OutgoingPhaseTwoActivity.this);
        Calendar calendar = Calendar.getInstance();
        Date tempDate = dateTo;
        calendar.setTime(dateTo);
        datePickerDialog.getDatePicker().init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getResources().getString(R.string.choose), datePickerDialog);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), datePickerDialog);
        datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            LocalDateTime localDateTime = cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().with(LocalTime.MAX);
            dateTo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            if (tempDate.compareTo(dateTo) != 0) {
                outgoingPhaseTwoActivityViewModel.setFilterDate(dateTo);
            }

        });


        datePickerDialog.show();
    }


    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OutgoingPhaseTwoActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_data_outgoing, null);
        builder.setView(view);
        Button filterDataCancelBtn = view.findViewById(R.id.filterDataCancelBtn);
        Button filterDataSubmitBtn = view.findViewById(R.id.filterDataSubmitBtn);
        TextInputEditText filterIncomingCodeEt = view.findViewById(R.id.filterIncomingCodeEt);
        TextInputEditText filterPartnerNameEt = view.findViewById(R.id.filterPartnerNameEt);
        TextInputEditText filterIncomingTransportEt = view.findViewById(R.id.filterIncomingTransportEt);
        MaterialAutoCompleteTextView filterIncomingWarehouseEt = view.findViewById(R.id.filterIncomingWarehouseEt);
        MaterialAutoCompleteTextView filterPartnerCityEt = view.findViewById(R.id.licencePlateEt);
        final AlertDialog filterDialog = builder.create();
        if (cityAdapter != null) {
            filterPartnerCityEt.setAdapter(cityAdapter);
        }
        if(warehouseAdapter != null){
            filterIncomingWarehouseEt.setAdapter(warehouseAdapter);
        }
        filterIncomingCodeEt.setText(mFilterDataHolder.getCode());
        filterPartnerNameEt.setText(mFilterDataHolder.getPartnerName());
        filterPartnerCityEt.setText(mFilterDataHolder.getPartnerCity());
        filterIncomingTransportEt.setText(mFilterDataHolder.getTransport());
        filterIncomingWarehouseEt.setText(mFilterDataHolder.getWarehouse());

        filterDataCancelBtn.setOnClickListener(view1 -> filterDialog.dismiss());

        filterDataSubmitBtn.setOnClickListener(view12 ->

                {
                    outgoingPhaseTwoActivityViewModel.setFilterDataHolder(new FilterDataHolder(
                            filterIncomingCodeEt.getText().toString().trim(),
                            filterPartnerNameEt.getText().toString().trim(),
                            filterPartnerCityEt.getText().toString().trim(),
                            filterIncomingTransportEt.getText().toString().trim(),
                            filterIncomingWarehouseEt.getText().toString().trim()
                    ));
                    filterDialog.dismiss();
                }

        );

        filterDialog.show();

    }

//    private void syncOutgoing(Date dateTo) {
//        outgoingPhaseTwoActivityViewModel.syncOutgoing(dateTo);
//    }

    private void toggleRecyclerView(int size) {

        if (size > 0) {
            binding.outgoingRv.setVisibility(View.VISIBLE);
            binding.noOutgoingForPeriodTv.setVisibility(View.GONE);
        } else {
            binding.outgoingRv.setVisibility(View.GONE);
            binding.noOutgoingForPeriodTv.setVisibility(View.VISIBLE);
        }
    }

    private void unregisterRealTimeFirestoreUpdates() {
        outgoingPhaseTwoActivityViewModel.unregisterRealTimeOutgoings();
    }

    private void registerRealTimeFirestoreUpdates(Date dateTo) {
        outgoingPhaseTwoActivityViewModel.registerRealTimeOutgoings(dateTo);
    }

    @Override
    public void onOutgoingClicked(Outgoing outgoing) {
        Intent i = new Intent(OutgoingPhaseTwoActivity.this, SingleOutgoingPhaseTwoActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(Constants.SELECTED_OUTGOING_ID_TAG, outgoing);
        startActivity(i);
    }

    @Override
    public void onOutgoingGroupedClicked(OutgoingGrouped outgoingGrouped) {
        Intent i = new Intent(OutgoingPhaseTwoActivity.this, SingleOutgoingPhaseTwoActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG, outgoingGrouped);
        startActivity(i);
    }
}