package com.example.wms_app.fragment.incoming.standard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.R;
import com.example.wms_app.adapter.incoming.IncomingTempListPreviewAdapter;
import com.example.wms_app.databinding.FragmentIncomingTempListPreviewBinding;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.viewmodel.incoming.standard.SingleIncomingViewModel;


public class IncomingTempListPreviewFragment extends Fragment implements IncomingTempListPreviewAdapter.IncomingTempListPreviewAdapterListener {
    private FragmentIncomingTempListPreviewBinding binding;
    private Context context;
    private SingleIncomingViewModel singleIncomingViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;
    private IncomingTempListPreviewAdapter adapter;


    public IncomingTempListPreviewFragment() {
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
        binding = FragmentIncomingTempListPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObjectsViewsAndLists(context);
        setupAdapter();
        setupObservers();
    }

    private void setupObservers() {
        singleIncomingViewModel.getIncomingDetailsFromTempList().observe(getViewLifecycleOwner(), incomingDetailsResultLocals -> {
            if (incomingDetailsResultLocals != null) {
                adapter.setProductBoxList(incomingDetailsResultLocals);
                toggleRecyclerView(incomingDetailsResultLocals.size());
            }
        });

    }

    private void setupAdapter() {
        adapter = new IncomingTempListPreviewAdapter(context, this);
        binding.incomingTempListRv.setHasFixedSize(true);
        binding.incomingTempListRv.setAdapter(adapter);
    }

    private void initObjectsViewsAndLists(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

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
        singleIncomingViewModel = new ViewModelProvider(requireActivity()).get(SingleIncomingViewModel.class);
    }

    private void toggleRecyclerView(int size) {
        if (size > 0) {
            binding.incomingTempListRv.setVisibility(View.VISIBLE);
            binding.incomingTempListNoProdTv.setVisibility(View.GONE);
        } else {
            binding.incomingTempListRv.setVisibility(View.GONE);
            binding.incomingTempListNoProdTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteProductClicked(int position) {
        singleIncomingViewModel.deleteProductFromTempList(position);
    }
}
