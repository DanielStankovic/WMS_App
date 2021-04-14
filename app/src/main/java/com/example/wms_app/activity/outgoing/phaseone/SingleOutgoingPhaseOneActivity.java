package com.example.wms_app.activity.outgoing.phaseone;

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
import com.example.wms_app.databinding.ActivitySingleOutgoingPhaseOneBinding;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.outgoing.phaseone.PhaseOneViewModel;

import java.util.Objects;

public class SingleOutgoingPhaseOneActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivitySingleOutgoingPhaseOneBinding binding;
    private PhaseOneViewModel phaseOneViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;
    private OutgoingGrouped outgoingGrouped;
    private Outgoing outgoing;
    private EnumOutgoingStyle enumOutgoingStyle;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initViewModel();
        initDialogs(SingleOutgoingPhaseOneActivity.this);
        setupToolbar();
        initOutgoingArgs();
        setupNavigationDrawerHeaderView();
        initNavigationComponents();
        setupObservers();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.outgoingToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNavigationDrawerHeaderView() {
        TextView menuDate, menuDescription, menuOutgoing;
        //setovanje header menu textview
        View headerView = binding.outgoingNavigationView.getHeaderView(0);
        menuDate = headerView.findViewById(R.id.phaseOneDateTv);
        menuDescription = headerView.findViewById(R.id.phaseOneDescriptionTv);
        menuOutgoing = headerView.findViewById(R.id.phaseOneOutgoingTv);
        setupDrawerHeader(menuDate, menuDescription, menuOutgoing);
    }

    private void initDialogs(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

    }

    private void setupDrawerHeader(TextView menuDate, TextView menuDescription, TextView menuOutgoing) {
        //Ako je jedna otprema
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            menuOutgoing.setText(getResources().getString(R.string.incoming_code_short, outgoing.getOutgoingCode()));
            menuDate.setText(Utility.getStringFromDate(outgoing.getOutgoingDate(), true));
            menuDescription.setText(getResources().getString(R.string.partner_name_and_address, outgoing.getPartnerName(), outgoing.getPartnerAddress()));
        }
        //Ako je grupna otprema
        else {
            menuOutgoing.setText(getResources().getString(R.string.outgoing_grouped_lbl));
            menuDate.setText(getResources().getString(R.string.up_to_date, outgoingGrouped.getPeriod()));
            menuDescription.setText("");
        }
    }

    private void initViewModel() {
        phaseOneViewModel = new ViewModelProvider(this).get(PhaseOneViewModel.class);
    }

    private void initNavigationComponents() {

        //prosledjivanje podataka u graph
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SELECTED_OUTGOING_ID_TAG, outgoing);
        bundle.putSerializable(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG, outgoingGrouped);


        navController = Navigation.findNavController(this, R.id.outgoingNavigationHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.outgoingDrawerLayout);
        NavigationUI.setupWithNavController(binding.outgoingNavigationView, navController);
        binding.outgoingNavigationView.setNavigationItemSelectedListener(this);
        navController.setGraph(navController.getGraph(), bundle);
    }


    private void initOutgoingArgs() {
        outgoingGrouped = (OutgoingGrouped) getIntent().getSerializableExtra(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG);
        outgoing = (Outgoing) getIntent().getSerializableExtra(Constants.SELECTED_OUTGOING_ID_TAG);
        enumOutgoingStyle = Utility.getCurrentOutgoingType(outgoing, outgoingGrouped);
    }


    private void initBinding() {
        binding = ActivitySingleOutgoingPhaseOneBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }


    private void setupObservers() {
        phaseOneViewModel.getApiResponseLiveData().observe(this, this::consumeResponse);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        binding.outgoingDrawerLayout.closeDrawers();
        if (item.getItemId() == R.id.outgoingPhaseOneCurrList)
            navController.navigate(R.id.outgoingPhaseOneTempListFragment);
        else if (item.getItemId() == R.id.outgoingPhaseOneForPreList)
            navController.navigate(R.id.outgoingPhaseOnePrealoadingFragment);
        else if (item.getItemId() == R.id.outgoingPhaseOnePreview)
            navController.navigate(R.id.outgoingPhaseOnePreviewFragment);

//        switch (item.getItemId()) {
//            case R.id.outgoingPhaseOneCurrList:
//                navController.navigate(R.id.outgoingPhaseOneTempListFragment);
//                break;
//            case R.id.outgoingPhaseOneForPreList:
//                navController.navigate(R.id.outgoingPhaseOnePrealoadingFragment);
//                break;
//            case R.id.outgoingPhaseOnePreview:
//                navController.navigate(R.id.outgoingPhaseOnePreviewFragment);
//                break;
//
//            default:
//
//        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.outgoingNavigationHostFragment), binding.outgoingDrawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (binding.outgoingDrawerLayout.isDrawerOpen(GravityCompat.START))
            binding.outgoingDrawerLayout.closeDrawer(GravityCompat.START);
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
                phaseOneViewModel.refreshApiResponseStatus();
                // Utility.showToast(context, getResources().getString(R.string.successString));
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                phaseOneViewModel.refreshApiResponseStatus();

                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                phaseOneViewModel.refreshApiResponseStatus();
                break;

            case ERROR_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithCallback(SingleOutgoingPhaseOneActivity.this, getResources().getString(R.string.error), apiResponse.error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SingleOutgoingPhaseOneActivity.this.onBackPressed();
                    }
                });
                phaseOneViewModel.refreshApiResponseStatus();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(SingleOutgoingPhaseOneActivity.this, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
                phaseOneViewModel.refreshApiResponseStatus();
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