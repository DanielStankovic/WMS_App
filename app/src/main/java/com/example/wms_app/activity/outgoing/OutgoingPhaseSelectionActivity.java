package com.example.wms_app.activity.outgoing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.wms_app.activity.outgoing.phasetwo.OutgoingPhaseTwoActivity;
import com.example.wms_app.activity.outgoing.phaseone.OutgoingPhaseOneActivity;
import com.example.wms_app.databinding.ActivityOutgoingPhaseSelectionBinding;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.InternetCheck;

public class OutgoingPhaseSelectionActivity extends AppCompatActivity {

    private ActivityOutgoingPhaseSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        setupListeners();
    }

    private void initBinding() {
        binding = ActivityOutgoingPhaseSelectionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setupListeners() {

        binding.outgoingPhaseOneBtn.setOnClickListener(view -> {
            //Otvara se aktivnost za fazu 1
            new InternetCheck(internet -> {
                if (internet)
                    startActivity(new Intent(OutgoingPhaseSelectionActivity.this, OutgoingPhaseOneActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                else
                    DialogBuilder.showNoInternetDialog(OutgoingPhaseSelectionActivity.this);
            }, OutgoingPhaseSelectionActivity.this);
        });

        binding.outgoingPhaseTwoBtn.setOnClickListener(view -> {
            //Otvara se aktivnost za fazu 2
            new InternetCheck(internet -> {
                if (internet)
                    startActivity(new Intent(OutgoingPhaseSelectionActivity.this, OutgoingPhaseTwoActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                else
                    DialogBuilder.showNoInternetDialog(OutgoingPhaseSelectionActivity.this);
            }, OutgoingPhaseSelectionActivity.this);
        });
    }
}