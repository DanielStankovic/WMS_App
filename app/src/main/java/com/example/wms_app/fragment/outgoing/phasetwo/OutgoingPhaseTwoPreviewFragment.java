package com.example.wms_app.fragment.outgoing.phasetwo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.wms_app.R;
import com.example.wms_app.databinding.FragmentIncomingOverviewBinding;
import com.example.wms_app.databinding.FragmentOutgoingPhaseTwoPreviewBinding;


public class OutgoingPhaseTwoPreviewFragment extends Fragment {

    private FragmentOutgoingPhaseTwoPreviewBinding binding;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {

        Fragment fragment = null;

        if (item.getItemId() == R.id.navigation_left)
            fragment = new OutgoingPhaseTwoPrevLeftFragment();
        else if (item.getItemId() == R.id.navigation_done)
            fragment = new OutgoingPhaseTwoPrevDoneFragment();
        if (fragment != null) {
            getParentFragmentManager().beginTransaction().replace(R.id.outgoingOverviewFrameLayout, fragment).commit();
        }

        return true;
    };

    public OutgoingPhaseTwoPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOutgoingPhaseTwoPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        binding.outgoingOverviewBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        OutgoingPhaseTwoPrevLeftFragment outgoingPhaseTwoPrevLeftFragment = new OutgoingPhaseTwoPrevLeftFragment();
        getParentFragmentManager().beginTransaction().replace(R.id.outgoingOverviewFrameLayout, outgoingPhaseTwoPrevLeftFragment).commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Sklanjanje bindinga
        binding = null;
    }
}