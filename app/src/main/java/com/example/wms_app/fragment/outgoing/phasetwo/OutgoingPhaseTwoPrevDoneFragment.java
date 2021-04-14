package com.example.wms_app.fragment.outgoing.phasetwo;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.adapter.outgoing.PhaseTwoPreviewDoneAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoPrevDoneBinding;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;

public class OutgoingPhaseTwoPrevDoneFragment extends Fragment implements PhaseTwoPreviewDoneAdapter.PhaseTwoPreviewDoneAdapterListener {

    private FragmentOutgoingPhaseTwoPrevDoneBinding binding;
    private Context context;
    private PhaseTwoViewModel phaseTwoViewModel;
    private PhaseTwoPreviewDoneAdapter adapter;
    private OnBackPressedCallback callback;
    private boolean isOutgoingCanceled;


    public OutgoingPhaseTwoPrevDoneFragment() {
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

        if (getArguments() != null) {
            isOutgoingCanceled = getArguments().getBoolean("IsOutgoingCanceled");
        }

        if (isOutgoingCanceled) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (isOutgoingCanceled) {
                    getActivity().finish();
                } else {
                    callback.setEnabled(false);
                    getActivity().onBackPressed();
                }

            }
        };

        getActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOutgoingPhaseTwoPrevDoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        setupObservers();
    }

    private void setupAdapter() {
        adapter = new PhaseTwoPreviewDoneAdapter(context, this);
        binding.outgoingPhaseTwoOverviewDoneRv.setHasFixedSize(true);
        binding.outgoingPhaseTwoOverviewDoneRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseTwoViewModel.getOutgoingDetailsResultPreviewListLiveData().observe(getViewLifecycleOwner(), outgoingDetailsResultPreviewList -> {
            if (outgoingDetailsResultPreviewList != null) {
                adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviewList);
                toggleRecyclerView(outgoingDetailsResultPreviewList.size());
            }
        });
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
        phaseTwoViewModel = new ViewModelProvider(requireActivity()).get(PhaseTwoViewModel.class);
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.outgoingPhaseTwoOverviewDoneRv.setVisibility(View.VISIBLE);
            binding.outgoingPhaseTwoOverviewDoneNoProdTv.setVisibility(View.GONE);
        } else {
            binding.outgoingPhaseTwoOverviewDoneRv.setVisibility(View.GONE);
            binding.outgoingPhaseTwoOverviewDoneNoProdTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteProductClicked(OutgoingDetailsResultPreview outgoingDetailsResultPreview) {
        if (outgoingDetailsResultPreview != null) {
            phaseTwoViewModel.deleteOutgoingDetailsResultFromFirebase(outgoingDetailsResultPreview);
        }
    }
}