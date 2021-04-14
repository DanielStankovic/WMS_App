package com.example.wms_app.fragment.incoming.standard;

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

import com.example.wms_app.adapter.incoming.IncomingOverviewDoneAdapter;
import com.example.wms_app.databinding.FragmentIncomingOverviewDoneBinding;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.viewmodel.incoming.standard.SingleIncomingViewModel;


public class IncomingOverviewDoneFragment extends Fragment implements IncomingOverviewDoneAdapter.IncomingOverviewDoneAdapterListener {

    private FragmentIncomingOverviewDoneBinding binding;
    private Context context;
    private SingleIncomingViewModel singleIncomingViewModel;
    private IncomingOverviewDoneAdapter adapter;
    private OnBackPressedCallback callback;
    private boolean isIncomingCanceled;

    public IncomingOverviewDoneFragment() {
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
            isIncomingCanceled = getArguments().getBoolean("IsIncomingCanceled");
        }

        if (isIncomingCanceled) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (isIncomingCanceled) {
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
        binding = FragmentIncomingOverviewDoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapter();
        setupObservers();
    }


    private void setupAdapter() {
        adapter = new IncomingOverviewDoneAdapter(context, this);
        binding.incomingOverviewDoneRv.setHasFixedSize(true);
        binding.incomingOverviewDoneRv.setAdapter(adapter);
    }

    private void setupObservers() {
        singleIncomingViewModel.getIncomingDetailsResultLocalListLiveData().observe(getViewLifecycleOwner(), incomingDetailsResultLocals -> {
            if (incomingDetailsResultLocals != null) {
                adapter.setProductBoxList(incomingDetailsResultLocals);
                toggleRecyclerView(incomingDetailsResultLocals.size());
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

    @Override
    public void onDeleteProductClicked(IncomingDetailsResultLocal incomingDetailsResultLocal) {
        if (incomingDetailsResultLocal != null) {
            singleIncomingViewModel.deleteIncomingDetailsResultFromFirebase(incomingDetailsResultLocal);
        }
    }

    private void initViewModels() {
        singleIncomingViewModel = new ViewModelProvider(requireActivity()).get(SingleIncomingViewModel.class);
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.incomingOverviewDoneRv.setVisibility(View.VISIBLE);
            binding.incomingOverviewDoneNoProdTv.setVisibility(View.GONE);
        } else {
            binding.incomingOverviewDoneRv.setVisibility(View.GONE);
            binding.incomingOverviewDoneNoProdTv.setVisibility(View.VISIBLE);
        }
    }

}
