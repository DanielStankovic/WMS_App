package com.example.wms_app.activity.inventory;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.example.wms_app.R;
import com.example.wms_app.databinding.ActivityInventoryBinding;
import com.example.wms_app.databinding.ActivitySingleIncomingBinding;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.inventory.InventoryViewModel;

import java.util.Date;
import java.util.Objects;

public class InventoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityInventoryBinding binding;
    private NavController navController;
    private InventoryViewModel inventoryViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initViewModel();
        initDialogs(InventoryActivity.this);
        setUpToolbar();
        setupObserver();
        setupNavigation();
    }

    private void initViewModel() {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
    }

    private void initDialogs(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void initBinding() {
        binding = ActivityInventoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setupObserver() {

        inventoryViewModel.getApiResponseLiveData().observe(this, this::consumeResponse);
        inventoryViewModel.getEmployeeIDLiveData().observe(InventoryActivity.this, integer -> {
            if (integer != null) {
                inventoryViewModel.syncInventory(Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, integer));
            }
        });

    }

    private void setUpToolbar() {
        setSupportActionBar(binding.inventoryToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNavigation() {

        TextView menuDate, menuDescription, menuIncoming;

        //setovanje header menu textview
        View headerView = binding.inventoryNavigationView.getHeaderView(0);
        menuDate = headerView.findViewById(R.id.incomingDateTv);
        menuIncoming = headerView.findViewById(R.id.incomingCodeTv);
        menuDescription = headerView.findViewById(R.id.incomingDescriptionTv);

        setupDrawerHeader(menuDate, menuDescription, menuIncoming);


        navController = Navigation.findNavController(this, R.id.inventoryNavGraph);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.activityInventoryDrawer);
        NavigationUI.setupWithNavController(binding.inventoryNavigationView, navController);
        binding.inventoryNavigationView.setNavigationItemSelectedListener(this);
        navController.setGraph(navController.getGraph());
    }

    private void setupDrawerHeader(TextView menuDate, TextView menuDescription, TextView menuIncoming) {

        menuDate.setText(getResources().getString(R.string.inventory_expected_date, Utility.getStringFromDate(new Date(), true)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        binding.activityInventoryDrawer.closeDrawers();

        if (item.getItemId() == R.id.inventoryTempList)
            navController.navigate(R.id.inventoryTempList);
        else if (item.getItemId() == R.id.inventoryOverview)
            navController.navigate(R.id.inventoryOverviewFragment);
        else if (item.getItemId() == R.id.inventorySend)
            DialogBuilder.showDialogWithYesNoCallback(InventoryActivity.this, getResources().getString(R.string.warning), getResources().getString(R.string.sent_result_to_server), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //slanje na server
                    inventoryViewModel.sendInventoryToServer();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

//        switch (item.getItemId()) {
//            case R.id.addTruck:
//                navController.navigate(R.id.incomingTransportFragment);
//                break;
//            case R.id.incomingOverview:
//                navController.navigate(R.id.incomingOverviewFragment);
//                break;
//            case R.id.tempList:
//                navController.navigate(R.id.incomingTempListPreviewFragment);
//                break;
//            case R.id.incomingSend:
////
//                singleIncomingViewModel.sendIncomingToServerAndFirebase();
//                break;
//            default:
//
//        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.inventoryNavGraph), binding.activityInventoryDrawer);
    }

    @Override
    public void onBackPressed() {
        if(binding.activityInventoryDrawer.isDrawerOpen(GravityCompat.START))
            binding.activityInventoryDrawer.closeDrawer(GravityCompat.START);
        else{
            super.onBackPressed();
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
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                if(apiResponse.error.equals("success_send")){
                    Utility.showToast(this, "USPEŠNO POSLAT POPIS!");
                    finish();
                }

                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                break;

            case ERROR_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithCallback(InventoryActivity.this, getResources().getString(R.string.error), apiResponse.error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        InventoryActivity.this.onBackPressed();
                    }
                });
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(InventoryActivity.this, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
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
