package com.example.wms_app.activity.incoming.production;

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
import com.example.wms_app.databinding.ActivityIncomingProductionBinding;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.incoming.production.IncomingProductionViewModel;

import java.util.Objects;

public class IncomingProductionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityIncomingProductionBinding binding;
    private NavController navController;
    private String selectedProductionTypeCode;
    private IncomingProductionViewModel incomingProductionViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initDialogs(IncomingProductionActivity.this);
        initViewModel();
        getExtras();
        setupObserver();
        setUpToolbar();
        setupNavigation();
    }

    private void initDialogs(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

    }

    private void setupObserver() {

        incomingProductionViewModel.getApiResponseLiveData().observe(this, this::consumeResponse);
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
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                incomingProductionViewModel.refreshApiResponseStatus();

                break;

            case SUCCESS_WITH_EXIT_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Utility.showToast(IncomingProductionActivity.this, apiResponse.error);
//                Snackbar sb = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
//                sb.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                    @Override
//                    public void onDismissed(Snackbar transientBottomBar, int event) {
//                        super.onDismissed(transientBottomBar, event);
//                        IncomingProductionActivity.this.finish();
//                    }
//                });
//                sb.show();
                incomingProductionViewModel.refreshApiResponseStatus();
                finish();

                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                incomingProductionViewModel.refreshApiResponseStatus();
                break;

            case ERROR_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithCallback(IncomingProductionActivity.this, getResources().getString(R.string.error), apiResponse.error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        IncomingProductionActivity.this.finish();
                    }
                });
                incomingProductionViewModel.refreshApiResponseStatus();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(IncomingProductionActivity.this, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
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


    private void initViewModel() {

        incomingProductionViewModel = new ViewModelProvider(this).get(IncomingProductionViewModel.class);

    }

    private void getExtras() {
        selectedProductionTypeCode = getIntent().getStringExtra(Constants.SELECTED_PRODUCTION_TYPE_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.incomingProductionNavigationHostFragment), binding.activityIncomingProductionDrawer);
    }

    @Override
    public void onBackPressed() {
        if (binding.activityIncomingProductionDrawer.isDrawerOpen(GravityCompat.START))
            binding.activityIncomingProductionDrawer.closeDrawer(GravityCompat.START);
        else {
            super.onBackPressed();
        }

    }

    private void initBinding() {
        binding = ActivityIncomingProductionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.incomingProductionToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNavigation() {

        TextView menuDate, menuDescription, menuIncoming;

        //setovanje header menu textview
        View headerView = binding.incomingProductionNavigationView.getHeaderView(0);
        menuDate = headerView.findViewById(R.id.incomingDateTv);
        menuIncoming = headerView.findViewById(R.id.incomingCodeTv);
        menuDescription = headerView.findViewById(R.id.incomingDescriptionTv);

        setupDrawerHeader(menuDate, menuDescription, menuIncoming);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.SELECTED_PRODUCTION_TYPE_TAG, selectedProductionTypeCode);

        navController = Navigation.findNavController(this, R.id.incomingProductionNavigationHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.activityIncomingProductionDrawer);
        NavigationUI.setupWithNavController(binding.incomingProductionNavigationView, navController);
        binding.incomingProductionNavigationView.setNavigationItemSelectedListener(this);
        navController.setGraph(navController.getGraph(), bundle);
    }

    private void setupDrawerHeader(TextView menuDate, TextView menuDescription, TextView menuIncoming) {

        menuDate.setText("");
        menuIncoming.setText(selectedProductionTypeCode);
        menuDescription.setText(getResources().getString(R.string.matis_production_lbl));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        binding.activityIncomingProductionDrawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.incomingProductionOverview:
                navController.navigate(R.id.incomingProductionOverviewFragment);
                break;
            case R.id.tempListProduction:
                navController.navigate(R.id.incomingProductionTempListFragment);
                break;
            case R.id.incomingProductionSend:
                showSendIncomingDialog();
                break;
            default:

        }

        return true;
    }

    private void showSendIncomingDialog() {
        DialogBuilder.showDialogWithYesCallback(IncomingProductionActivity.this, getResources().getString(R.string.warning),
                getResources().getString(R.string.incomings_send_prompt),
                (dialogInterface, i) -> {
                    incomingProductionViewModel.sendIncomingToServerAndFirebase();
                });
    }
}
