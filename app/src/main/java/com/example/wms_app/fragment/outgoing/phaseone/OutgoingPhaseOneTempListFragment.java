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

import com.example.wms_app.adapter.outgoing.PhaseOneTempListAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseOneTempListBinding;
import com.example.wms_app.viewmodel.outgoing.phaseone.PhaseOneViewModel;


public class OutgoingPhaseOneTempListFragment extends Fragment implements PhaseOneTempListAdapter.PhaseOneTempListAdapterListener {

    private FragmentOutgoingPhaseOneTempListBinding binding;
    private Context context;
    private PhaseOneViewModel phaseOneViewModel;
    private PhaseOneTempListAdapter adapter;

    public OutgoingPhaseOneTempListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        //Dobijanje viewmodela
        initViewModel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOutgoingPhaseOneTempListBinding.inflate(inflater, container, false);
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

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.outgoingPhaseOneTempListRv.setVisibility(View.VISIBLE);
            binding.outgoingPhaseOneTempListNoProdTv.setVisibility(View.GONE);
        } else {
            binding.outgoingPhaseOneTempListRv.setVisibility(View.GONE);
            binding.outgoingPhaseOneTempListNoProdTv.setVisibility(View.VISIBLE);
        }
    }

    private void initViewModel() {
        phaseOneViewModel = new ViewModelProvider(requireActivity()).get(PhaseOneViewModel.class);
    }

    private void setupAdapter() {
        adapter = new PhaseOneTempListAdapter(context, this);
        binding.outgoingPhaseOneTempListRv.setHasFixedSize(true);
        binding.outgoingPhaseOneTempListRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseOneViewModel.getOutgoingDetailsResultPreviewFromTempList().observe(getViewLifecycleOwner(),
                outgoingDetailsResultPreviews -> {
                    if (outgoingDetailsResultPreviews != null) {
                        adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviews);
                        toggleRecyclerView(outgoingDetailsResultPreviews.size());
                    }
                });
    }

    @Override
    public void onDeleteProductBoxClicked(int position) {
        phaseOneViewModel.deleteProductBoxFromTempList(position);
    }
}