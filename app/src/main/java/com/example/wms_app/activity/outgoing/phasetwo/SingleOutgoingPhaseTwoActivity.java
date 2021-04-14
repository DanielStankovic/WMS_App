package com.example.wms_app.activity.outgoing.phasetwo;

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
import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.R;
import com.example.wms_app.databinding.ActivitySingleOutgoingPhaseTwoBinding;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.outgoing.phasetwo.PhaseTwoViewModel;

import java.util.Objects;

public class SingleOutgoingPhaseTwoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivitySingleOutgoingPhaseTwoBinding binding;
    private PhaseTwoViewModel phaseTwoViewModel;
    private AlertDialog loadingDialog;
    private AlertDialog errorDialog;
    private Outgoing currentOutgoing;
    private OutgoingGrouped currentOutgoingGrouped;
    private EnumOutgoingStyle enumOutgoingStyle;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        initBinding();
        initViewModel();
        initDialogs(SingleOutgoingPhaseTwoActivity.this);
        setupToolbar();
        initOutgoingArgs();
        setupNavigationDrawerHeaderView();
        initNavigationComponents();
        setupObservers();
    }

    private void initBinding() {
        binding = ActivitySingleOutgoingPhaseTwoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void initViewModel() {
        phaseTwoViewModel = new ViewModelProvider(this).get(PhaseTwoViewModel.class);
    }

    private void initDialogs(Context context) {

        //inicijalizacija Dijaloga za loading
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        errorDialog = DialogBuilder.showOkDialogWithoutCallback(context, getResources().getString(R.string.error_happened), "");

    }

    private void setupToolbar() {
        setSupportActionBar(binding.outgoingPhaseTwoToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initOutgoingArgs() {
        currentOutgoingGrouped = (OutgoingGrouped) getIntent().getSerializableExtra(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG);
        currentOutgoing = (Outgoing) getIntent().getSerializableExtra(Constants.SELECTED_OUTGOING_ID_TAG);
        enumOutgoingStyle = Utility.getCurrentOutgoingType(currentOutgoing, currentOutgoingGrouped);
    }

    private void setupNavigationDrawerHeaderView() {
        TextView menuDate, menuDescription, menuOutgoing;
        //setovanje header menu textview
        View headerView = binding.outgoingPhaseTwoNavigationView.getHeaderView(0);
        MenuItem transportMenuItem = binding.outgoingPhaseTwoNavigationView.getMenu().getItem(0);
        menuDate = headerView.findViewById(R.id.phaseTwoDateTv);
        menuDescription = headerView.findViewById(R.id.phaseTwoDescriptionTv);
        menuOutgoing = headerView.findViewById(R.id.phaseTwoOutgoingTv);
        setupDrawerHeader(menuDate, menuDescription, menuOutgoing, transportMenuItem);
    }

    private void setupDrawerHeader(TextView menuDate, TextView menuDescription, TextView menuOutgoing, MenuItem transportMenuItem) {
        //Ako je jedna otprema
        if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE) {
            menuOutgoing.setText(getResources().getString(R.string.incoming_code_short, currentOutgoing.getOutgoingCode()));
            menuDate.setText(Utility.getStringFromDate(currentOutgoing.getOutgoingDate(), true));
            menuDescription.setText(getResources().getString(R.string.partner_name_and_address, currentOutgoing.getPartnerName(), currentOutgoing.getPartnerAddress()));
            transportMenuItem.setVisible(true);
        } else {
            menuOutgoing.setText(getResources().getString(R.string.outgoing_grouped_lbl));
            menuDate.setText(getResources().getString(R.string.up_to_date, currentOutgoingGrouped.getPeriod()));
            menuDescription.setText("");
            transportMenuItem.setVisible(false);
        }
    }

    private void initNavigationComponents() {

        //prosledjivanje podataka u graph
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SELECTED_OUTGOING_ID_TAG, currentOutgoing);
        bundle.putSerializable(Constants.SELECTED_OUTGOING_GROUPED_ID_TAG, currentOutgoingGrouped);

        navController = Navigation.findNavController(this, R.id.outgoingPhaseTwoNavigationHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.outgoingPhaseTwoDrawerLayout);
        NavigationUI.setupWithNavController(binding.outgoingPhaseTwoNavigationView, navController);
        binding.outgoingPhaseTwoNavigationView.setNavigationItemSelectedListener(this);
        navController.setGraph(navController.getGraph(), bundle);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        binding.outgoingPhaseTwoDrawerLayout.closeDrawers();

        if (item.getItemId() == R.id.addTruck)
            navController.navigate(R.id.outgoingPhaseTwoTransportFragment);
        else if (item.getItemId() == R.id.tempList)
            navController.navigate(R.id.outgoingPhaseTwoTempListFragment);
        else if (item.getItemId() == R.id.outgoingOverview)
            navController.navigate(R.id.outgoingPhaseTwoPreviewFragment);
        else if (item.getItemId() == R.id.outgoingSend) {
            if (enumOutgoingStyle == EnumOutgoingStyle.SINGLE)
                phaseTwoViewModel.sendOutgoingToServer();
            else
                phaseTwoViewModel.sendOutgoingToServerGrouped();

        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.outgoingPhaseTwoNavigationHostFragment), binding.outgoingPhaseTwoDrawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (binding.outgoingPhaseTwoDrawerLayout.isDrawerOpen(GravityCompat.START))
            binding.outgoingPhaseTwoDrawerLayout.closeDrawer(GravityCompat.START);
        else {
            super.onBackPressed();
        }

    }

    private void setupObservers() {
        phaseTwoViewModel.getApiResponseLiveData().observe(this, this::consumeResponse);
    }

    private void consumeResponse(ApiResponse apiResponse) {

        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                phaseTwoViewModel.refreshApiResponseStatus();
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                phaseTwoViewModel.refreshApiResponseStatus();

                break;

            case SUCCESS_WITH_EXIT_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Utility.showToast(SingleOutgoingPhaseTwoActivity.this, apiResponse.error);
//                Snackbar sb = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
//                sb.show();
                phaseTwoViewModel.refreshApiResponseStatus();
                finish();

                break;


            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(getResources().getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                phaseTwoViewModel.refreshApiResponseStatus();
                break;

            case ERROR_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithCallback(SingleOutgoingPhaseTwoActivity.this, getResources().getString(R.string.error), apiResponse.error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SingleOutgoingPhaseTwoActivity.this.onBackPressed();
                    }
                });
                phaseTwoViewModel.refreshApiResponseStatus();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(SingleOutgoingPhaseTwoActivity.this, getResources().getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
                phaseTwoViewModel.refreshApiResponseStatus();
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