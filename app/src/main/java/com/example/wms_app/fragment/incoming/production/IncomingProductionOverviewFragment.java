package com.example.wms_app.fragment.incoming.production;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.wms_app.R;
import com.example.wms_app.databinding.FragmentIncomingProductionOverviewBinding;


public class IncomingProductionOverviewFragment extends Fragment {

    private FragmentIncomingProductionOverviewBinding binding;
    private Context context;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_left: {
                fragment = new IncomingProductionOverviewLeftFragment();
                break;
            }
            case R.id.navigation_done: {
                fragment = new IncomingProductionOverviewDoneFragment();
                break;
            }

        }
        if (fragment != null) {
            getParentFragmentManager().beginTransaction().replace(R.id.incomingOverviewFrameLayout, fragment).commit();
        }

        return true;
    };

    public IncomingProductionOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentIncomingProductionOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        binding.incomingOverviewBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        IncomingProductionOverviewLeftFragment incomingProductionOverviewLeftFragment = new IncomingProductionOverviewLeftFragment();
        getParentFragmentManager().beginTransaction().replace(R.id.incomingOverviewFrameLayout, incomingProductionOverviewLeftFragment).commit();
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
}
