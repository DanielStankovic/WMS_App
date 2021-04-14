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

import com.example.wms_app.adapter.outgoing.PhaseOnePreloadingAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseOnePrealoadingBinding;
import com.example.wms_app.viewmodel.outgoing.phaseone.PhaseOneViewModel;


public class OutgoingPhaseOnePreloadingFragment extends Fragment {

    private FragmentOutgoingPhaseOnePrealoadingBinding binding;
    private Context context;
    private PhaseOneViewModel phaseOneViewModel;
    private PhaseOnePreloadingAdapter adapter;

    public OutgoingPhaseOnePreloadingFragment() {
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
        binding = FragmentOutgoingPhaseOnePrealoadingBinding.inflate(inflater, container, false);
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
        adapter = new PhaseOnePreloadingAdapter(context);
        binding.outgoingPhaseOnePreloadingRv.setHasFixedSize(true);
        binding.outgoingPhaseOnePreloadingRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseOneViewModel.getOutgoingDetailsResultPreviewFromPreloading().observe(getViewLifecycleOwner(),
                outgoingDetailsResultPreviews -> {
                    if (outgoingDetailsResultPreviews != null) {
                        adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviews);
                        toggleRecyclerView(outgoingDetailsResultPreviews.size());
                    }
                });
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.outgoingPhaseOnePreloadingRv.setVisibility(View.VISIBLE);
            binding.phaseOneNoPreloadingProdTv.setVisibility(View.GONE);
        } else {
            binding.outgoingPhaseOnePreloadingRv.setVisibility(View.GONE);
            binding.phaseOneNoPreloadingProdTv.setVisibility(View.VISIBLE);
        }
    }

}