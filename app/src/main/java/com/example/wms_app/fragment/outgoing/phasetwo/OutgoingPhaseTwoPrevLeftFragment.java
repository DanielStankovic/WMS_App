package com.example.wms_app.fragment.outgoing.phasetwo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wms_app.R;
import com.example.wms_app.adapter.outgoing.PhaseOnePreviewOutgoingAdapter;
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoPrevLeftBinding;
import com.example.wms_app.model.ProductItemType;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class OutgoingPhaseTwoPrevLeftFragment extends Fragment {

    private FragmentOutgoingPhaseTwoPrevLeftBinding binding;
    private Context context;
    private PhaseTwoViewModel phaseTwoViewModel;
    //Koristim ovaj adapter iz faze 1 posto je skroz sve isto kao pregled u fazi 1
    private PhaseOnePreviewOutgoingAdapter adapter;


    public OutgoingPhaseTwoPrevLeftFragment() {
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
        binding = FragmentOutgoingPhaseTwoPrevLeftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        setupObservers();
        setupListeners();
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

    private void setupAdapter() {
        adapter = new PhaseOnePreviewOutgoingAdapter(context);
        binding.phaseTwoPreviewRv.setHasFixedSize(true);
        binding.phaseTwoPreviewRv.setAdapter(adapter);
    }

    private void setupObservers() {
        phaseTwoViewModel.getOutgoingDetailsResultPreviewFromOutgoing().observe(getViewLifecycleOwner(),
                outgoingDetailsResultPreviews -> {
                    if (outgoingDetailsResultPreviews != null) {
                        adapter.setOutgoingDetailsResultPreviewList(outgoingDetailsResultPreviews);
                    }
                });
    }


    private void setupListeners() {
        binding.filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(phaseTwoViewModel.getDistinctProductTypeID());
            }
        });
    }
    
    private void showFilterDialog(List<ProductItemType> productItemTypeList){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_product_item_type, null);
        builder.setView(view);
        final AlertDialog filterDialog = builder.create();
        final List<CheckBox> checkBoxList = new ArrayList<>();


        LinearLayout linear = view.findViewById (R.id.checkboxLL);
        ImageView selectAll = view.findViewById (R.id.selectAllBtn);
        ImageView deselect = view.findViewById (R.id.deselectBtn);
        Button apply = view.findViewById (R.id.applyBtn);

        LayoutParams lparams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


            for (ProductItemType productItemType: productItemTypeList) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setLayoutParams(lparams);
                checkBox.setChecked(true);
                checkBox.setTextSize(18);
                checkBox.setId(productItemType.getProductItemTypeID());
                checkBox.setText(productItemType.getName());
                linear.addView(checkBox);
                checkBoxList.add(checkBox);
            }

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxList.stream().forEach(checkBox -> checkBox.setChecked(true));
            }
        });

        deselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxList.stream().forEach(checkBox -> checkBox.setChecked(false));
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Integer> productTypeIDsList = checkBoxList.stream().filter(checkBox -> checkBox.isChecked()).map(checkBox -> checkBox.getId()).collect(Collectors.toSet());

                if(productTypeIDsList.size() > 0){
                    adapter.setOutgoingDetailsResultPreviewList(phaseTwoViewModel.getFilteredOutgoingDetailsResultPreview(productTypeIDsList));
                    filterDialog.dismiss();
                }
                else
                    Utility.showToast(context, getResources().getString(R.string.need_to_choose));
            }
        });

        filterDialog.show();
    }


}