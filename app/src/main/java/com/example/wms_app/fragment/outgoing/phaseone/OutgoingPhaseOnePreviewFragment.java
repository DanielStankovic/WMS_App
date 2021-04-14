package com.example.wms_app.fragment.outgoing.phaseone;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.adapter.outgoing.PhaseOnePreviewOutgoingAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseOnePreviewBinding;
import com.example.wms_app.viewmodel.outgoing.phaseone.PhaseOneViewModel;

public class OutgoingPhaseOnePreviewFragment extends Fragment {

    private FragmentOutgoingPhaseOnePreviewBinding binding;
    private Context context;
    private PhaseOneViewModel phaseOneViewModel;
    private PhaseOnePreviewOutgoingAdapter adapter;

    public OutgoingPhaseOnePreviewFragment() {
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
        binding = FragmentOutgoingPhaseOnePreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        setupObservers();
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
        phaseOneViewModel = new ViewModelProvider(requireActivity()).get(PhaseOneViewModel.class);
    }

    private void setupAdapter() {
        adapter = new PhaseOnePreviewOutgoingAdapter(context);
        binding.phaseOnePreviewRv.setHasFixedSize(true);
        binding.phaseOnePreviewRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseOneViewModel.getOutgoingDetailsResultPreviewFromOutgoing().observe(getViewLifecycleOwner(),
                outgoingDetailsResultPreviews -> {
                    if (outgoingDetailsResultPreviews != null) {
                        adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviews);
                    }
                });
    }
}