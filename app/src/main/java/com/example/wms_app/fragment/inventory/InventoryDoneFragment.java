package com.example.wms_app.fragment.inventory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.adapter.inventory.InventoryDoneAdapter;
import com.example.wms_app.databinding.FragmentInventoryCurrentListBinding;
import com.example.wms_app.databinding.FragmentInventoryDoneBinding;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.viewmodel.inventory.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;


public class InventoryDoneFragment extends Fragment implements InventoryDoneAdapter.InventoryDoneListener{

    private Context context;
    private FragmentInventoryDoneBinding binding;
    private InventoryViewModel inventoryViewModel;
    private RecyclerView rvCurrentList;
    private List<InventoryDetailsResult> idrLastList;
    private InventoryDoneAdapter currentListAdapter;
    private AlertDialog loadingDialog;




    public InventoryDoneFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryDoneBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        init();
        setAdapters();
        setObservers();

        return view;
    }

    private void init() {
        rvCurrentList = binding.rvInventoryDone;
        idrLastList = new ArrayList<InventoryDetailsResult>();
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        loadingDialog = DialogBuilder.getLoadingDialog(context);

    }

    private void setObservers(){

        inventoryViewModel.getApiResponseLiveData().observe(getViewLifecycleOwner(), new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {
                consumeResponse(apiResponse);
            }
        });

        inventoryViewModel.getInventoryDetailsResult().observe(getViewLifecycleOwner(), new Observer<List<InventoryDetailsResult>>() {
            @Override
            public void onChanged(List<InventoryDetailsResult> inventoryDetailsResults) {
                currentListAdapter.seIdrList(inventoryDetailsResults);
                currentListAdapter.notifyDataSetChanged();
                if(inventoryDetailsResults.size() == 0)
                    binding.tvEmptyTemporaryList.setVisibility(View.VISIBLE);
                else
                    binding.tvEmptyTemporaryList.setVisibility(View.INVISIBLE);
            }
        });

        inventoryViewModel.getAllProducts().observe(getViewLifecycleOwner(), new Observer<List<ProductBox>>() {
            @Override
            public void onChanged(List<ProductBox> products) {
                    if(products != null){
                        currentListAdapter.setProductList(products);
                    }
                }
        });

        inventoryViewModel.getCurrentInventoryID().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
              //  if(s != null)
                   // inventoryViewModel.syncInventoryDetailsResult(s);
            }
        });

    }

    private void setAdapters(){
        currentListAdapter = new InventoryDoneAdapter(context, this);
        currentListAdapter.seIdrList(idrLastList);
        rvCurrentList.setAdapter(currentListAdapter);
        rvCurrentList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onItemDeleted(InventoryDetailsResult idr) {
        inventoryViewModel.deleteProductFromPosition(idr);
        currentListAdapter.notifyDataSetChanged();
    }


    private void consumeResponse(ApiResponse apiResponse) {
        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                break;
            case SUCCESS_WITH_ACTION:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                break;
            case ERROR:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithoutCallback(getContext(), getResources().getString(R.string.error_happened), getResources().getString(R.string.error_string, apiResponse.error)).show();
                break;
            default:
                break;
        }
    }
}
