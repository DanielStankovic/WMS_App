package com.example.wms_app.fragment.incoming.standard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.wms_app.R;
import com.example.wms_app.adapter.incoming.IncomingTransportAdapter;
import com.example.wms_app.databinding.FragmentIncomingTransportBinding;
import com.example.wms_app.model.IncomingTruckResult;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.incoming.standard.SingleIncomingViewModel;

import java.util.ArrayList;
import java.util.List;


public class IncomingTransportFragment extends Fragment implements IncomingTransportAdapter.IncomingTransportAdapterListener {

    private SingleIncomingViewModel singleIncomingViewModel;
    private Context context;
    private FragmentIncomingTransportBinding binding;
    private IncomingTransportAdapter adapter;
    private ArrayAdapter<String> licencePlateAdapter;
    private List<String> licencePlateList;
    private int employeeIDDb = -1;

    public IncomingTransportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        //Dobijanje viewmodela
        initViewModels();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIncomingTransportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();

        //Postavljanje observera
        setupObservers();

        //Postavljanje listenera
        setupListeners();

        initLicencePlateAdapter();
    }

    private void initLicencePlateAdapter() {
        licencePlateList = new ArrayList<>();
        licencePlateAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, licencePlateList);
        binding.licencePlateEt.setAdapter(licencePlateAdapter);
    }


    private void init() {

        adapter = new IncomingTransportAdapter(this);
        binding.incomingTransportRv.setHasFixedSize(true);
        binding.incomingTransportRv.setAdapter(adapter);

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
        this.context = null;
    }

    private void setupListeners() {
        binding.addTruckBtn.setOnClickListener(view -> {
            String truckDriver = binding.truckDriverEt.getText().toString().trim();
            String licencePlate = binding.licencePlateEt.getText().toString().trim();
            //Izmena 11-Dec-2020 Vozac vise nije obavezan, samo registracija
//            if (truckDriver.isEmpty()) {
//                binding.truckDriverEt.setError(getResources().getString(R.string.driver_not_inserted));
//                return;
//            }
            if (licencePlate.isEmpty()) {
                binding.licencePlateEt.setError(getResources().getString(R.string.licence_plate_not_inserted));
                return;
            }

            singleIncomingViewModel.insertTruckToFirebase(truckDriver, licencePlate, Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb));
        });

        binding.truckDriverEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.truckDriverEt.setError(null);
        });

        binding.licencePlateEt.setOnFocusChangeListener((view, isFocused) -> {
            if (!isFocused)
                binding.truckDriverEt.setError(null);
        });

    }

    private void setupObservers() {
        singleIncomingViewModel.getIncomingTruckResultLiveData().observe(getViewLifecycleOwner(), incomingTruckResults -> {
            if (incomingTruckResults != null) {
                adapter.setIncomingTruckResultList(incomingTruckResults);
                toggleRecyclerView(incomingTruckResults.size());
            }
        });

        // singleIncomingViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(), this::consumeResponse);

        singleIncomingViewModel.getLicencePlateListLiveData().observe(getViewLifecycleOwner(), strings -> {
            if (strings != null) {
                licencePlateList.clear();
                licencePlateList.addAll(strings);
                licencePlateAdapter.notifyDataSetChanged();
            }
        });

        singleIncomingViewModel.getEmployeeIDLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                employeeIDDb = integer;
            }
        });
    }

    private void initViewModels() {
        singleIncomingViewModel = new ViewModelProvider(requireActivity()).get(SingleIncomingViewModel.class);
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.incomingTransportRv.setVisibility(View.VISIBLE);
            binding.incomingNoTransportsTv.setVisibility(View.GONE);
        } else {
            binding.incomingTransportRv.setVisibility(View.GONE);
            binding.incomingNoTransportsTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteTruckClicked(IncomingTruckResult incomingTruckResult) {
        if (incomingTruckResult != null) {
            singleIncomingViewModel.deleteTruckFromFirebase(incomingTruckResult);
        }
    }
}
