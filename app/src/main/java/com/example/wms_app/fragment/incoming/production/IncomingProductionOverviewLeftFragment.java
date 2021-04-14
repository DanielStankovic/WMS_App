package com.example.wms_app.fragment.incoming.production;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.adapter.incoming.IncomingOverviewLeftAdapter;
import com.example.wms_app.databinding.FragmentIncomingProductionOverviewLeftBinding;
import com.example.wms_app.viewmodel.incoming.production.IncomingProductionViewModel;

public class IncomingProductionOverviewLeftFragment extends Fragment {

    private FragmentIncomingProductionOverviewLeftBinding binding;
    private Context context;
    private IncomingProductionViewModel incomingProductionViewModel;
    private IncomingOverviewLeftAdapter adapter;

    public IncomingProductionOverviewLeftFragment() {
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
        // Inflate the layout for this fragment
        binding = FragmentIncomingProductionOverviewLeftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        setupObservers();
    }

    private void setupObservers() {

        incomingProductionViewModel.getProductBoxListLeftMediatorLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.setProductBoxList(list);
            }
        });
    }

    private void setupAdapter() {
        adapter = new IncomingOverviewLeftAdapter(context);
        binding.incomingOverviewLeftRv.setHasFixedSize(true);
        binding.incomingOverviewLeftRv.setAdapter(adapter);
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

    private void initViewModels() {
        incomingProductionViewModel = new ViewModelProvider(requireActivity()).get(IncomingProductionViewModel.class);
    }
}
