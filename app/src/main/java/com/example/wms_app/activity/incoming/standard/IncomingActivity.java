package com.example.wms_app.activity.incoming.standard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.wms_app.R;
import com.example.wms_app.adapter.DocumentStyleAdapter;
import com.example.wms_app.adapter.incoming.IncomingAdapterGrouped;
import com.example.wms_app.adapter.incoming.IncomingAdapterSingle;
import com.example.wms_app.databinding.ActivityIncomingBinding;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.model.FilterDataHolder;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.IncomingType;
import com.example.wms_app.utilities.ConsumeResponse;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.mapper.AsyncGetGroupedIncoming;
import com.example.wms_app.viewmodel.incoming.standard.IncomingActivityViewModel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.wms_app.utilities.Constants.SELECTED_INCOMING_GROUPED_ID_TAG;
import static com.example.wms_app.utilities.Constants.SELECTED_INCOMING_ID_TAG;

public class IncomingActivity extends AppCompatActivity implements IncomingAdapterSingle.IncomingAdapterListener, IncomingAdapterGrouped.IncomingAdapterGroupedListener {

    private ActivityIncomingBinding binding;
    private IncomingActivityViewModel incomingActivityViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;
    private Date dateTo;
    private FilterDataHolder mFilterDataHolder;
    private IncomingAdapterSingle adapterSingle;
    private IncomingAdapterGrouped adapterGrouped;
    private ArrayAdapter<IncomingType> incomingTypeAdapter;
    private List<IncomingType> incomingTypeList;
    private ArrayAdapter<String> cityAdapter;
    private List<String> cityList;
    private List<String> warehouseList;
    private ArrayAdapter<String> warehouseAdapter;
    private EnumIncomingStyle currentIncomingStyle = EnumIncomingStyle.SINGLE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initToolbar();
        initViewModel();
        initDialog();
        initAdapter();
        initFilterSpinnerAdapter();
        initCityAdapter();
        setupIncomingStyleSpinner();
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
        binding = ActivityIncomingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void initViewModel() {
        incomingActivityViewModel = new ViewModelProvider(this).get(IncomingActivityViewModel.class);
    }

    private void initDialog() {
        loadingDialog = DialogBuilder.getLoadingDialog(IncomingActivity.this);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(IncomingActivity.this, getResources().getString(R.string.error_happened), "");
    }

    private void initAdapter() {
        adapterSingle = new IncomingAdapterSingle(IncomingActivity.this, this);
        binding.incomingRv.setHasFixedSize(true);
        binding.incomingRv.setAdapter(adapterSingle);
    }

    private void initFilterSpinnerAdapter() {
        incomingTypeList = new ArrayList<>();
        incomingTypeAdapter = new ArrayAdapter<IncomingType>(IncomingActivity.this, android.R.layout.simple_selectable_list_item, incomingTypeList){

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                IncomingType incomingType = super.getItem(position);
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setMaxLines(1);
                textView.setMaxEms(30);
                textView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setText(Objects.requireNonNull(incomingType).getIncomingTypeName());
                return textView;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                IncomingType incomingType = super.getItem(position);
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setMaxLines(1);
                textView.setMaxEms(30);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                textView.setText(Objects.requireNonNull(incomingType).getIncomingTypeName());
                return textView;
            }
        };

    }

    private void initCityAdapter() {
        cityList = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(IncomingActivity.this, android.R.layout.simple_dropdown_item_1line, cityList);
        warehouseList = new ArrayList<>();
        warehouseAdapter = new ArrayAdapter<>(IncomingActivity.this, android.R.layout.simple_dropdown_item_1line, warehouseList);
    }

    private void setupIncomingStyleSpinner() {
        String[] incomingStyleArray = getResources().getStringArray(R.array.incoming_style);
        DocumentStyleAdapter incomingStyleAdapter = new DocumentStyleAdapter(IncomingActivity.this, R.layout.item_spinner_doc_type, incomingStyleArray);
        binding.incomingStyleSpinner.setAdapter(incomingStyleAdapter);
    }

    private void setFilterDate() {
        incomingActivityViewModel.setFilterDate(dateTo);
    }

    private void setupObservers() {
        incomingActivityViewModel.getApiResponseLiveData().observe(this, apiResponse -> {
            if (apiResponse != null)
                ConsumeResponse.consumeResponse(apiResponse,
                        loadingDialog,
                        errorDialog,
                        getResources(),
                        binding,
                        IncomingActivity.this,
                        () -> incomingActivityViewModel.refreshApiResponseStatus()
                );
            //consumeResponse(apiResponse);
        });

        incomingActivityViewModel.getFilterDateLiveData().observe(this, date -> {
            if (date != null) {
                dateTo = date;
                registerRealTimeFirestoreUpdates(date);
            }
        });

        incomingActivityViewModel.getIncomingMediatorLiveData().observe(this, incomings -> {
            if (incomings != null) {
                toggleRecyclerView(incomings.size());
                if (currentIncomingStyle == EnumIncomingStyle.SINGLE) {
                    //Ako je pojedinacni prijem ide ovaj deo
                    binding.incomingRv.setAdapter(adapterSingle);
                    adapterSingle.setIncomingList(incomings);
                } else {
                    //Ako je grupni prijem ide ovaj deo
                    if (adapterGrouped == null)
                        adapterGrouped = new IncomingAdapterGrouped(IncomingActivity.this, this);
                    //Ovde ide kreiranje objekta za grupni prijem na osnovu dobijene liste
                    setGroupedIncoming(incomings);
                }
            }
        });

        incomingActivityViewModel.getFilterDataHolderLiveData().observe(this, filterDataHolder -> {
            if (filterDataHolder != null) {
                mFilterDataHolder = filterDataHolder;
            }
        });

        incomingActivityViewModel.getIncomingTypeListLiveData().observe(this, incomingTypes -> {
            if (incomingTypes != null) {
                incomingTypeList.clear();
                incomingTypeList.addAll(incomingTypes);
                incomingTypeAdapter.notifyDataSetChanged();
            }
        });

        incomingActivityViewModel.getCitiesLiveData().observe(this, cities -> {
            if(cities != null){
                cityList.clear();
                cityList.addAll(cities);
                cityAdapter.notifyDataSetChanged();
            }
        });

        incomingActivityViewModel.getWarehousesLiveData().observe(this, warehouses -> {
            if (warehouses != null) {
                warehouseList.clear();
                warehouseList.addAll(warehouses);
                warehouseAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setGroupedIncoming(List<Incoming> incomings) {
        if (!incomings.isEmpty()) {
            new AsyncGetGroupedIncoming(incomingGrouped -> {
                try {
                    if (incomingGrouped == null) {
                        throw new Exception(getResources().getString(R.string.no_prod_in_local_db_for_this_inc));
                    }
                    adapterGrouped.setIncomingGrouped(incomingGrouped);
                    binding.incomingRv.setAdapter(adapterGrouped);

                } catch (Exception ex) {
                    ErrorClass.handle(ex, IncomingActivity.this);
                }
            }, IncomingActivity.this, incomings, true, dateTo);
        }
    }

    private void setupListeners() {
        binding.dateFilterBtn.setOnClickListener(view -> showDateFilterDialog());
        binding.filterBtn.setOnClickListener(view -> showFilterDialog());
        binding.sendIncomingBtn.setOnClickListener(view -> showSendIncomingDialog());
        binding.incomingStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentIncomingStyle = i == 0 ? EnumIncomingStyle.SINGLE : EnumIncomingStyle.GROUPED;
                incomingActivityViewModel.setOutgoingStyle(currentIncomingStyle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showDateFilterDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(IncomingActivity.this);
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
                incomingActivityViewModel.setFilterDate(dateTo);
            }

        });


        datePickerDialog.show();
    }

    private void showFilterDialog() {
        final int[] incomingTypeID = new int[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(IncomingActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_data_incoming, null);
        builder.setView(view);
        Button filterDataCancelBtn = view.findViewById(R.id.filterDataCancelBtn);
        Button filterDataSubmitBtn = view.findViewById(R.id.filterDataSubmitBtn);
        TextInputEditText filterIncomingCodeEt = view.findViewById(R.id.filterIncomingCodeEt);
        TextInputEditText filterPartnerNameEt = view.findViewById(R.id.filterPartnerNameEt);
        TextInputEditText filterIncomingTransportEt = view.findViewById(R.id.filterIncomingTransportEt);
        MaterialAutoCompleteTextView filterIncomingWarehouseEt = view.findViewById(R.id.filterIncomingWarehouseEt);
        MaterialAutoCompleteTextView filterPartnerCityEt = view.findViewById(R.id.licencePlateEt);
        Spinner incomingTypeSpinner = view.findViewById(R.id.incomingTypeSpinner);
        final AlertDialog filterDialog = builder.create();
        if (incomingTypeAdapter != null) {
            incomingTypeSpinner.setAdapter(incomingTypeAdapter);
        }
        if (cityAdapter != null) {
            filterPartnerCityEt.setAdapter(cityAdapter);
        }

        if(warehouseAdapter != null){
            filterIncomingWarehouseEt.setAdapter(warehouseAdapter);
        }
        incomingTypeSpinner.setSelection(((ArrayAdapter<IncomingType>) incomingTypeSpinner.getAdapter()).getPosition(incomingTypeList.stream().filter(x -> x.getIncomingTypeID() == mFilterDataHolder.getTypeID()).findAny().get()));
        filterIncomingCodeEt.setText(mFilterDataHolder.getCode());
        filterPartnerNameEt.setText(mFilterDataHolder.getPartnerName());
        filterPartnerCityEt.setText(mFilterDataHolder.getPartnerCity());
        filterIncomingTransportEt.setText(mFilterDataHolder.getTransport());
        filterIncomingWarehouseEt.setText(mFilterDataHolder.getWarehouse());
        incomingTypeID[0] = mFilterDataHolder.getTypeID();

        filterDataCancelBtn.setOnClickListener(view1 -> filterDialog.dismiss());

        filterDataSubmitBtn.setOnClickListener(view12 ->

                {
                    //TODO Ovde promeniti ovu nulu da ide u skladu sa spinerom
                    incomingActivityViewModel.setFilterDataHolder(new FilterDataHolder(
                            incomingTypeID[0],
                            filterIncomingCodeEt.getText().toString().trim(),
                            filterPartnerNameEt.getText().toString().trim(),
                            filterPartnerCityEt.getText().toString().trim(),
                            filterIncomingTransportEt.getText().toString().trim(),
                            filterIncomingWarehouseEt.getText().toString().trim()
                    ));
                    filterDialog.dismiss();
                }

        );

        incomingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                incomingTypeID[0] = incomingTypeList.get(position).getIncomingTypeID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        filterDialog.show();

    }

    private void showSendIncomingDialog() {
        DialogBuilder.showDialogWithYesCallback(IncomingActivity.this, getResources().getString(R.string.warning),
                getResources().getString(R.string.incomings_finished_send_prompt),
                (dialogInterface, i) -> incomingActivityViewModel.sendIncomingToServer());
    }

    private void registerRealTimeFirestoreUpdates(Date dateTo) {
        incomingActivityViewModel.registerRealTimeIncomings(dateTo);
    }

    private void unregisterRealTimeFirestoreUpdates() {
        incomingActivityViewModel.unregisterRealTimeIncomings();
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.incomingRv.setVisibility(View.VISIBLE);
            binding.noIncomingForPeriodTv.setVisibility(View.GONE);
        } else {
            binding.incomingRv.setVisibility(View.GONE);
            binding.noIncomingForPeriodTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onIncomingClicked(Incoming incoming) {
        Intent i = new Intent(IncomingActivity.this, SingleIncomingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(SELECTED_INCOMING_ID_TAG, incoming);
        startActivity(i);
    }

    @Override
    public void onIncomingGroupedClicked(IncomingGrouped incomingGrouped) {
        Intent i = new Intent(IncomingActivity.this, SingleIncomingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(SELECTED_INCOMING_GROUPED_ID_TAG, incomingGrouped);
        startActivity(i);
    }
}
