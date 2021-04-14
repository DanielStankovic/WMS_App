package com.example.wms_app.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.wms_app.R;
import com.example.wms_app.activity.incoming.production.IncomingProductionActivity;
import com.example.wms_app.activity.incoming.standard.IncomingActivity;
import com.example.wms_app.activity.inventory.InventoryActivity;
import com.example.wms_app.activity.outgoing.OutgoingPhaseSelectionActivity;
import com.example.wms_app.activity.repacking.RepackingActivity;
import com.example.wms_app.adapter.incoming.ProductionTypeSelectionAdapter;
import com.example.wms_app.databinding.ActivityDashboardBinding;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.DashboardViewModel;

public class DashboardActivity extends AppCompatActivity implements ProductionTypeSelectionAdapter.ProductionTypeSelectionAdapterListener {
    private ActivityDashboardBinding binding;
    private ProductionTypeSelectionAdapter productionTypeSelectionAdapter;
    private DashboardViewModel dashboardViewModel;
    private AlertDialog productionDialog;
    private int employeeIDFromDb = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initViewModel();
        initProductionAdapter();
        initProductionDialog();
        setupObservers();
        setupListeners();
    }

    private void initProductionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_production_type_selection, null);
        builder.setView(view);
        RecyclerView productionTypeSelectionRv = view.findViewById(R.id.productionTypeSelectionRv);
        productionTypeSelectionRv.setHasFixedSize(true);
        productionTypeSelectionRv.setAdapter(productionTypeSelectionAdapter);
        productionDialog = builder.create();
    }

    private void initViewModel() {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    private void initProductionAdapter() {

        productionTypeSelectionAdapter = new ProductionTypeSelectionAdapter(this);
    }

    private void initBinding() {
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setupListeners() {

        binding.btnIncoming.setOnClickListener(view -> startActivity(new Intent(DashboardActivity.this, IncomingActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        binding.btnIncomingProduction.setOnClickListener(view -> showProductionSelectionDialog());

        binding.btnOutgoing.setOnClickListener(view -> startActivity(new Intent(DashboardActivity.this, OutgoingPhaseSelectionActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        binding.btnInventory.setOnClickListener(view -> startActivity(new Intent(DashboardActivity.this, InventoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        binding.btnRepacking.setOnClickListener(view -> startActivity(new Intent(DashboardActivity.this, RepackingActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)));
    }

    private void showProductionSelectionDialog() {
        if (productionDialog != null)
            productionDialog.show();
    }

    private void hideProductionSelectionDialog() {
        if (productionDialog != null && productionDialog.isShowing())
            productionDialog.dismiss();
    }

    private void setupObservers() {
        dashboardViewModel.getProductionTypeListLiveData().observe(DashboardActivity.this, strings -> {
            if (strings != null) {
                productionTypeSelectionAdapter.setProductionTypeCodeList(strings);
            }
        });

        dashboardViewModel.getEmployeeIDLiveData().observe(DashboardActivity.this, integer -> {
            if (integer != null) {
                employeeIDFromDb = integer;
            }
        });
    }


    @Override
    public void onBackPressed() {

        DialogBuilder.showDialogWithYesCallback(DashboardActivity.this, getResources().getString(R.string.attention), getResources().getString(R.string.logout_prompt),
                (dialogInterface, i) -> {
                    int employeeID = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeIDFromDb);
                    if (employeeID == -1) {
                        super.onBackPressed();
                        return;
                    }
                    FirebaseFirestore.getInstance().collection("employee").document(String.valueOf(employeeID)).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Utility.showToast(DashboardActivity.this, getResources().getString(R.string.logout_success));
                                    finish();
                                }
                            }).addOnFailureListener(e -> DialogBuilder.showOkDialogWithoutCallback(DashboardActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.logout_error, e.getMessage())).show());
                });
    }

    @Override
    public void onProductTypeClicked(String productTypeCode) {
        hideProductionSelectionDialog();
        Intent i = new Intent(DashboardActivity.this, IncomingProductionActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(Constants.SELECTED_PRODUCTION_TYPE_TAG, productTypeCode);
        startActivity(i);
    }
}
