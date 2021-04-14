package com.example.wms_app.fragment.inventory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wms_app.adapter.inventory.InventoryCurrentListAdapter;
import com.example.wms_app.databinding.FragmentInventoryCurrentListBinding;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.viewmodel.inventory.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;


public class InventoryCurrentListFragment extends Fragment implements InventoryCurrentListAdapter.CurrentListListener {

    private Context context;
    private FragmentInventoryCurrentListBinding binding;
    private InventoryViewModel inventoryViewModel;
    private RecyclerView rvCurrentList;
    private List<InventoryDetailsResult> idrLastList;
    private InventoryCurrentListAdapter currentListAdapter;



    public InventoryCurrentListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryCurrentListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        init();
        setAdapters();
        setObservers();

        return view;
    }

    private void init() {
        rvCurrentList = binding.rvCurrentList;
        idrLastList = new ArrayList<InventoryDetailsResult>();
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
    }

    private void setObservers(){
        inventoryViewModel.getIdrLastList().observe(getViewLifecycleOwner(), new Observer<List<InventoryDetailsResult>>() {
            @Override
            public void onChanged(List<InventoryDetailsResult> inventoryDetailsResults) {
                currentListAdapter.setIdrLastList(inventoryDetailsResults);
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

    }

    private void setAdapters(){
        currentListAdapter = new InventoryCurrentListAdapter(context, idrLastList, this);
        currentListAdapter.setIdrLastList(idrLastList);
        rvCurrentList.setAdapter(currentListAdapter);
        rvCurrentList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onItemDeleted(int index) {
        inventoryViewModel.deleteIdrLastListItem(index);
        currentListAdapter.notifyDataSetChanged();
    }
}
