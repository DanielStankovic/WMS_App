package com.example.wms_app.activity.incoming.standard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.R;
import com.example.wms_app.databinding.ActivitySingleIncomingBinding;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.incoming.standard.SingleIncomingViewModel;

import java.util.Objects;

public class SingleIncomingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivitySingleIncomingBinding binding;
    private NavController navController;
    private Incoming currentIncoming;
    private IncomingGrouped currentIncomingGrouped;
    private SingleIncomingViewModel singleIncomingViewModel;
    private EnumIncomingStyle enumIncomingStyle;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initViewModel();
        initDialogs(SingleIncomingActivity.this);
        setUpToolbar();
        initIncomingArgs();
        setupNavigationDrawerHeaderView();
        initNavigationComponents();
        setupObserver();
    }


    private void initViewModel() {
        singleIncomingViewModel = new ViewModelProvider(this).get(SingleIncomingViewModel.class);
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
        binding = ActivitySingleIncomingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setupObserver() {

        singleIncomingViewModel.getApiResponseLiveData().observe(this, this::consumeResponse);
    }

    private void initIncomingArgs() {
        currentIncomingGrouped = (IncomingGrouped) getIntent().getSerializableExtra(Constants.SELECTED_INCOMING_GROUPED_ID_TAG);
        currentIncoming = (Incoming) getIntent().getSerializableExtra(Constants.SELECTED_INCOMING_ID_TAG);
        enumIncomingStyle = Utility.getCurrentIncomingType(currentIncoming, currentIncomingGrouped);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.incomingToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNavigationDrawerHeaderView() {
        TextView menuDate, menuDescription, menuIncoming;
        //setovanje header menu textview
        View headerView = binding.incomingNavigationView.getHeaderView(0);
        MenuItem transportMenuItem = binding.incomingNavigationView.getMenu().getItem(0);
        menuDate = headerView.findViewById(R.id.incomingDateTv);
        menuIncoming = headerView.findViewById(R.id.incomingCodeTv);
        menuDescription = headerView.findViewById(R.id.incomingDescriptionTv);
        setupDrawerHeader(menuDate, menuDescription, menuIncoming, transportMenuItem);
    }

    private void setupDrawerHeader(TextView menuDate, TextView menuDescription, TextView menuIncoming, MenuItem transportMenuItem) {
        //Ako je jedan prijem
        if (enumIncomingStyle == EnumIncomingStyle.SINGLE) {
            menuDate.setText(getResources().getString(R.string.incoming_expected_date, Utility.getStringFromDate(currentIncoming.getIncomingDate(), true)));
            menuIncoming.setText(currentIncoming.getIncomingCode());
            menuDescription.setText(getResources().getString(R.string.partner_name_and_address, currentIncoming.getPartnerName(), currentIncoming.getPartnerAddress()));
            transportMenuItem.setVisible(true);
        } else {
            menuIncoming.setText(getResources().getString(R.string.incoming_grouped_lbl));
            menuDate.setText(getResources().getString(R.string.up_to_date, currentIncomingGrouped.getPeriod()));
            menuDescription.setText("");
            transportMenuItem.setVisible(false);
        }
    }

    private void initNavigationComponents() {
        //prosledjivanje podataka u graph
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SELECTED_INCOMING_ID_TAG, currentIncoming);
        bundle.putSerializable(Constants.SELECTED_INCOMING_GROUPED_ID_TAG, currentIncomingGrouped);

        navController = Navigation.findNavController(this, R.id.incomingNavigationHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.activitySingleIncomingDrawer);
        NavigationUI.setupWithNavController(binding.incomingNavigationView, navController);
        binding.incomingNavigationView.setNavigationItemSelectedListener(this);
        navController.setGraph(navController.getGraph(), bundle);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        binding.activitySingleIncomingDrawer.closeDrawers();

        if (item.getItemId() == R.id.addTruck)
            navController.navigate(R.id.incomingTransportFragment);
        else if (item.getItemId() == R.id.incomingOverview)
            navController.navigate(R.id.incomingOverviewFragment);
        else if (item.getItemId() == R.id.tempList)
            navController.navigate(R.id.incomingTempListPreviewFragment);
        else if (item.getItemId() == R.id.incomingSend) {
            if (enumIncomingStyle == EnumIncomingStyle.SINGLE)
                singleIncomingViewModel.sendIncomingToServerAndFirebase();
            else
                singleIncomingViewModel.sendIncomingToServerAndFirebaseGrouped();
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.incomingNavigationHostFragment), binding.activitySingleIncomingDrawer);
    }

    @Override
    public void onBackPressed() {
        if (binding.activitySingleIncomingDrawer.isDrawerOpen(GravityCompat.START))
            binding.activitySingleIncomingDrawer.closeDrawer(GravityCompat.START);
        else {
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
                singleIncomingViewModel.refreshApiResponseStatus();
                // Utility.showToast(context, getResources().getString(R.string.successString));
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                singleIncomingViewModel.refreshApiResponseStatus();

                break;

            case SUCCESS_WITH_EXIT_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Utility.showToast(SingleIncomingActivity.this, apiResponse.error);
//                Snackbar sb = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
//                sb.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                    @Override
//                    public void onDismissed(Snackbar transientBottomBar, int event) {
//                        super.onDismissed(transientBottomBar, event);
//                        SingleIncomingActivity.this.finish();
//                    }
//                });
//                sb.show();
                singleIncomingViewModel.refreshApiResponseStatus();
                finish();


                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                singleIncomingViewModel.refreshApiResponseStatus();
                break;

            case ERROR_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithCallback(SingleIncomingActivity.this, getResources().getString(R.string.error), apiResponse.error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SingleIncomingActivity.this.onBackPressed();
                    }
                });
                singleIncomingViewModel.refreshApiResponseStatus();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(SingleIncomingActivity.this, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
                singleIncomingViewModel.refreshApiResponseStatus();
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
