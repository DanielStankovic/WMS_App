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

import com.example.wms_app.adapter.outgoing.PhaseOneTempListAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoTempListBinding;
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;


public class OutgoingPhaseTwoTempListFragment extends Fragment implements PhaseOneTempListAdapter.PhaseOneTempListAdapterListener {

    private FragmentOutgoingPhaseTwoTempListBinding binding;
    private Context context;
    private PhaseTwoViewModel phaseTwoViewModel;
    private PhaseOneTempListAdapter adapter;

    public OutgoingPhaseTwoTempListFragment() {
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
        binding = FragmentOutgoingPhaseTwoTempListBinding.inflate(inflater, container, false);
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

    private void setupAdapter() {
        adapter = new PhaseOneTempListAdapter(context, this);
        binding.outgoingPhaseTwoTempListRv.setHasFixedSize(true);
        binding.outgoingPhaseTwoTempListRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseTwoViewModel.getOutgoingDetailsResultPreviewFromTempList().observe(getViewLifecycleOwner(),
                outgoingDetailsResultPreviews -> {
                    if (outgoingDetailsResultPreviews != null) {
                        adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviews);
                        toggleRecyclerView(outgoingDetailsResultPreviews.size());
                    }
                });
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.outgoingPhaseTwoTempListRv.setVisibility(View.VISIBLE);
            binding.outgoingPhaseTwoTempListNoProdTv.setVisibility(View.GONE);
        } else {
            binding.outgoingPhaseTwoTempListRv.setVisibility(View.GONE);
            binding.outgoingPhaseTwoTempListNoProdTv.setVisibility(View.VISIBLE);
        }
    }

    private void initViewModel() {
        phaseTwoViewModel = new ViewModelProvider(requireActivity()).get(PhaseTwoViewModel.class);
    }

    @Override
    public void onDeleteProductBoxClicked(int position) {
        phaseTwoViewModel.deleteProductBoxFromTempList(position);
    }
}