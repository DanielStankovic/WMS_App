package com.example.wms_app.fragment.outgoing.phasetwo;

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
import com.example.wms_app.adapter.outgoing.OutgoingTransportAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoTransportBinding;
import com.example.wms_app.model.OutgoingTruckResult;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;

import java.util.ArrayList;
import java.util.List;

public class OutgoingPhaseTwoTransportFragment extends Fragment implements OutgoingTransportAdapter.OutgoingTransportAdapterListener {

    private PhaseTwoViewModel phaseTwoViewModel;
    private Context context;
    private FragmentOutgoingPhaseTwoTransportBinding binding;
    private OutgoingTransportAdapter adapter;
    private ArrayAdapter<String> licencePlateAdapter;
    private List<String> licencePlateList;
    private int employeeIDDb = -1;


    public OutgoingPhaseTwoTransportFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOutgoingPhaseTwoTransportBinding.inflate(inflater, container, false);
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

    private void init() {
        adapter = new OutgoingTransportAdapter(this);
        binding.outgoingPhaseTwoTransportRv.setHasFixedSize(true);
        binding.outgoingPhaseTwoTransportRv.setAdapter(adapter);

    }

    private void initLicencePlateAdapter() {
        licencePlateList = new ArrayList<>();
        licencePlateAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, licencePlateList);
        binding.licencePlateEt.setAdapter(licencePlateAdapter);
    }

    private void initViewModels() {
        phaseTwoViewModel = new ViewModelProvider(requireActivity()).get(PhaseTwoViewModel.class);
    }

    private void setupListeners() {
        binding.addTruckBtn.setOnClickListener(view -> {
            String truckDriver = binding.truckDriverEt.getText().toString().trim();
            String licencePlate = binding.licencePlateEt.getText().toString().trim();
            if (truckDriver.isEmpty()) {
                binding.truckDriverEt.setError(getResources().getString(R.string.driver_not_inserted));
                return;
            }
            if (licencePlate.isEmpty()) {
                binding.licencePlateEt.setError(getResources().getString(R.string.licence_plate_not_inserted));
                return;
            }

            phaseTwoViewModel.insertTruckToFirebase(truckDriver, licencePlate, Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDDb));
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
        phaseTwoViewModel.getOutgoingTruckResultLiveData().observe(getViewLifecycleOwner(), outgoingTruckResults -> {
            if (outgoingTruckResults != null) {
                adapter.setOutgoingTruckResultList(outgoingTruckResults);
                toggleRecyclerView(outgoingTruckResults.size());
            }
        });

        phaseTwoViewModel.getLicencePlateListLiveData().observe(getViewLifecycleOwner(), strings -> {
            if (strings != null) {
                licencePlateList.clear();
                licencePlateList.addAll(strings);
                licencePlateAdapter.notifyDataSetChanged();
            }
        });

        phaseTwoViewModel.getEmployeeIDLiveData().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                employeeIDDb = integer;
            }
        });
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.outgoingPhaseTwoTransportRv.setVisibility(View.VISIBLE);
            binding.outgoingPhaseTwoNoTransportsTv.setVisibility(View.GONE);
        } else {
            binding.outgoingPhaseTwoTransportRv.setVisibility(View.GONE);
            binding.outgoingPhaseTwoNoTransportsTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteTruckClicked(OutgoingTruckResult outgoingTruckResult) {
        if (outgoingTruckResult != null) {
            phaseTwoViewModel.deleteTruckFromFirebase(outgoingTruckResult);
        }
    }
}