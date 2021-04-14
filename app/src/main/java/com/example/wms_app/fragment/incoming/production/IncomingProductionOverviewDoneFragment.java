package com.example.wms_app.fragment.incoming.production;

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

import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.R;
import com.example.wms_app.adapter.incoming.IncomingProductionOverviewDoneAdapter;
import com.example.wms_app.databinding.FragmentIncomingProductionOverviewDoneBinding;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.viewmodel.incoming.production.IncomingProductionViewModel;


public class IncomingProductionOverviewDoneFragment extends Fragment implements IncomingProductionOverviewDoneAdapter.IncomingProductionOverviewDoneAdapterListener {

    private FragmentIncomingProductionOverviewDoneBinding binding;
    private Context context;
    private IncomingProductionViewModel incomingProductionViewModel;
    private IncomingProductionOverviewDoneAdapter adapter;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;

    public IncomingProductionOverviewDoneFragment() {
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
        binding = FragmentIncomingProductionOverviewDoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObjectsViewsAndLists(context);
        setupAdapter();
        setupObservers();
    }

    private void initObjectsViewsAndLists(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

    }

    private void setupAdapter() {
        adapter = new IncomingProductionOverviewDoneAdapter(context, this);
        binding.incomingOverviewDoneRv.setHasFixedSize(true);
        binding.incomingOverviewDoneRv.setAdapter(adapter);
    }

    private void setupObservers() {
        // incomingProductionViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(), this::consumeResponse);
        incomingProductionViewModel.getIncomingDetailsResultLocalListLiveData().observe(getViewLifecycleOwner(), incomingDetailsResultLocals -> {
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
            incomingProductionViewModel.deleteIncomingDetailsResultFromFirebase(incomingDetailsResultLocal);
        }
    }

    private void initViewModels() {
        incomingProductionViewModel = new ViewModelProvider(requireActivity()).get(IncomingProductionViewModel.class);
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

    private void consumeResponse(ApiResponse apiResponse) {
        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                incomingProductionViewModel.refreshApiResponseStatus();
                // Utility.showToast(context, getResources().getString(R.string.successString));
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(binding.getRoot().findViewById(android.R.id.content), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                incomingProductionViewModel.refreshApiResponseStatus();

                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                incomingProductionViewModel.refreshApiResponseStatus();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(context, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
                incomingProductionViewModel.refreshApiResponseStatus();
                break;

            case IDLE:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                break;

            default:
                break;
        }
    }
}
