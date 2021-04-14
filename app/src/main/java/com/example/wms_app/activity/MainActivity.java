package com.example.wms_app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.BuildConfig;
import com.example.wms_app.R;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.data.NetworkClass;
import com.example.wms_app.databinding.ActivityMainBinding;
import com.example.wms_app.enums.EnumMailContentType;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.ErrorClass;
import com.example.wms_app.utilities.ExceptionHandler;
import com.example.wms_app.utilities.InternetCheck;
import com.example.wms_app.utilities.SendMail;
import com.example.wms_app.utilities.Utility;
import com.example.wms_app.viewmodel.MainActivityViewModel;



public class MainActivity extends AppCompatActivity implements NetworkClass.increaseDialogProgressInterface {


    private ActivityMainBinding binding;
    public String DEVICE_SERIAL_NUMBER = null;
    private MainActivityViewModel mainActivityViewModel;
    //Filip: test
    private AlertDialog progressDialog;
    private ProgressBar progressBar;
    private TextView progressBarTv;
    public int progress = 0;
    private String unsentData = "";
    private NetworkClass nc;
    private int employeeID = -1;


    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        checkErrorHappened();
        init();
        initBinding();
        DialogBuilder.sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME, "Profile0 (default)", this);

        verifyPermissions();
    }

    private boolean checkFirestoreValues() {
//        //TODO Ovde doradtiti metodu kada dobijemo adresu servera kod njih
//        if (ApiClient.SERVICE_ADDRESS.equals("http://89.216.113.44:8228/") && getResources().getString(R.string.project_id).equals("kolibriwms-matis")) {
//            return true;
//        } else if (!ApiClient.SERVICE_ADDRESS.equals("http://89.216.113.44:8228/") && getResources().getString(R.string.project_id).equals("matis-wms"))
//            return true;
//        else
//            return false;

        return true;
    }

    private void setupLocalLogin() {
        if (ApiClient.SERVICE_ADDRESS.equals("http://89.216.113.44:8228/") || ApiClient.SERVICE_ADDRESS.equals("http://localhost:53852/")) {
            binding.username.setText("RM");
            binding.password.setText("0000");
            binding.appVersionTv.setText("LOCAL - " + getResources().getString(R.string.version, BuildConfig.VERSION_NAME));

        } else
            binding.appVersionTv.setText(getResources().getString(R.string.version, BuildConfig.VERSION_NAME));


    }

    private void init() {
        loadingDialog = DialogBuilder.getLoadingDialog(MainActivity.this);
        mainActivityViewModel = new ViewModelProvider(MainActivity.this).get(MainActivityViewModel.class);
    }

    private void setupObservers() {
        mainActivityViewModel.getApiResponseLiveData().observe(MainActivity.this, apiResponse -> {
            if (apiResponse != null)
                consumeResponse(apiResponse);
        });

        mainActivityViewModel.getEmployeeIDLiveData().observe(MainActivity.this, integer -> {
            if (integer != null) {
                employeeID = integer;
            }
        });

    }


    private void initBinding() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void checkErrorHappened() {
        if (getIntent().getBooleanExtra("error", false)) {
            DialogBuilder.showDialogWithYesCallback(this, getResources().getString(R.string.system_error), getResources().getString(R.string.system_error_happened), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new InternetCheck(internet -> {
                        if (internet) {

                            int employeeIDToSend = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeID);
                            Log.d("AAAA", String.valueOf(Constants.EMPLOYEE_ID));
                            String emplID = employeeIDToSend == -1 ? "EmployeeID ne postoji ni u konstanti ni u bazi" : String.valueOf(employeeIDToSend);

                            StringBuilder sb = new StringBuilder();
                            sb.append("App Version: ").append(BuildConfig.VERSION_NAME);
                            sb.append("\n");
                            sb.append("EmployeeID: ").append(emplID);
                            sb.append("\n");

                            new SendMail(
                                    MainActivity.this,
                                    getResources().getString(R.string.mail_subject),
                                    sb.toString(),
                                    getResources().getString(R.string.mail_to),
                                    true,
                                    EnumMailContentType.MAIL_TEXT_TYPE,
                                    sent -> {
                                        if (sent) {
                                            Utility.showToast(MainActivity.this, getResources().getString(R.string.mail_sent));
                                        } else {
                                            Utility.showToast(MainActivity.this, getResources().getString(R.string.mail_not_sent));
                                        }
                                    }
                            );
                        } else {
                            DialogBuilder.showNoInternetDialog(MainActivity.this);
                        }
                    }, MainActivity.this);

                }
            });
        }
    }

    private void sendError(){
        DialogBuilder.showDialogWithYesCallback(this, getResources().getString(R.string.system_error), getResources().getString(R.string.send_error), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new InternetCheck(internet -> {
                    if (internet) {

                        int employeeIDToSend = Utility.getEmployeeIDFromInput(Constants.EMPLOYEE_ID, employeeID);
                        String emplID = employeeIDToSend == -1 ? "EmployeeID ne postoji ni u konstanti ni u bazi" : String.valueOf(employeeIDToSend);

                        StringBuilder sb = new StringBuilder();
                        sb.append("App Version: ").append(BuildConfig.VERSION_NAME);
                        sb.append("\n");
                        sb.append("EmployeeID: ").append(emplID);
                        sb.append("\n");

                        new SendMail(
                                MainActivity.this,
                                getResources().getString(R.string.mail_subject),
                                sb.toString(),
                                getResources().getString(R.string.mail_to),
                                true,
                                EnumMailContentType.MAIL_TEXT_TYPE,
                                sent -> {
                                    if (sent) {
                                        Utility.showToast(MainActivity.this, getResources().getString(R.string.mail_sent));
                                    } else {
                                        Utility.showToast(MainActivity.this, getResources().getString(R.string.mail_not_sent));
                                    }
                                }
                        );
                    } else {
                        DialogBuilder.showNoInternetDialog(MainActivity.this);
                    }
                }, MainActivity.this);

            }
        });
    }

    private void verifyPermissions() {

        String[] permissions = {
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[3]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[4]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[5]) == PackageManager.PERMISSION_GRANTED
        ) {
            DEVICE_SERIAL_NUMBER = Build.getSerial();
//            String serviceAddress = Utility.readFile(MainActivity.this, "serviceAddress.txt");
//            if (serviceAddress.isEmpty()) {
//                DialogBuilder.showOkDialogWithCallback(MainActivity.this, getResources().getString(R.string.error),
//                        getResources().getString(R.string.service_file_parse_error),
//                        (dialogInterface, i) -> finish());
//                return;
//            } else if (serviceAddress.equals("NOT FOUND")) {
//                DialogBuilder.showOkDialogWithCallback(MainActivity.this, getResources().getString(R.string.error),
//                        getResources().getString(R.string.service_file_not_found),
//                        (dialogInterface, i) -> finish());
//                return;
//            } else {
//                ApiClient.SERVICE_ADDRESS = serviceAddress;
//            }
            if (checkFirestoreValues()) {
                setupListeners();
                setupObservers();
                setupLocalLogin();
            } else {
                DialogBuilder.showOkDialogWithCallback(MainActivity.this,
                        getResources().getString(R.string.error),
                        getResources().getString(R.string.invalid_firestore),
                        (dialogInterface, i) -> {
                            MainActivity.this.finish();
                        });
            }

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            verifyPermissions();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorClass.handle(e, MainActivity.this);
        }
    }

    private void setupListeners() {

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employeeLogin();
            }
        });

        binding.sendErrorTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendError();
            }
        });
    }

    private void employeeLogin() {

        String userName = binding.username.getText().toString();
        String password = binding.password.getText().toString();

        if (TextUtils.isEmpty(userName))
            binding.username.setError(getString(R.string.error_message));
        else if (TextUtils.isEmpty(password))
            binding.password.setError(getString(R.string.error_message));
        else if(TextUtils.isEmpty(DEVICE_SERIAL_NUMBER))
            DialogBuilder.showOkDialogWithoutCallback(MainActivity.this, getResources().getString(R.string.error),getResources().getString(R.string.no_device_no) );
        else
            mainActivityViewModel.loginUser(userName, password, DEVICE_SERIAL_NUMBER);

    }

    private void showProgressDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_sync_layout, null);
        builder.setView(view);

        progressBar = view.findViewById(R.id.progressBar);
        progressBarTv = view.findViewById(R.id.progressBarTv);

        progressDialog = builder.create();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressBar.setMax(Constants.imeNivoaSinhronizacije.length * 5 + Constants.sendError.length * 5);

        progressBar.setProgress(0);
        progressDialog.show();


    }

    private void closeProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.cancel();
    }

    private void consumeResponse(ApiResponse apiResponse) {

        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
               // Utility.showToast(MainActivity.this,getResources().getString(R.string.successString));
                break;

            case ERROR:
                if(loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showOkDialogWithoutCallback(MainActivity.this, getResources().getString(R.string.error_happened_main), getResources().getString(R.string.error_string, apiResponse.error)).show();
                //   new SyncAsync().execute();
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                //   Utility.showToast(context, getResources().getString(R.string.successStringSend));
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
               new SyncAsync().execute();
                break;

            default:
                break;
        }
    }

    private void setProgress(final int set, final String string) {

        progress = set;

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressBar.setProgress(progress);
                if (string != null && !string.isEmpty()) {
                    progressBarTv.setText(string);
                }
            }
        });
    }

    public void incProgress(final int inc, final String string) {

        progress = progress + inc;

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressBar.setProgress(progress);
                if (string != null && !string.isEmpty()) {
                    progressBarTv.setText(string);
                }
            }
        });
    }

    @Override
    public void increaseProgress(int progress, String title) {
        incProgress(progress, title);
    }

    private class SyncAsync extends AsyncTask<Void, Void, Void>{

        private boolean errorHappened;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                nc = new NetworkClass(MainActivity.this, true, MainActivity.this);


                setProgress(0, "");

                nc.syncData(progress);
                incProgress(5, getResources().getString(R.string.sync_finished));


            } catch (Exception ex) {

                Utility.writeErrorToFile(ex);
                errorHappened = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        if (Constants.EMPLOYEE_ID > 0) {
                            StringBuilder dialogMessage = new StringBuilder();
                            if (Constants.nivoSinhronizacije == -1) // sinhronizacija je prosla, puklo je slanje
                            {

                                // Prikaz liste gresaka na osnovu nivoa tabele
                                dialogMessage.append(getResources().getString(R.string.data_not_sent));
                                // ukoliko je MP (greske)

                                for (int i = Constants.nivoSlanja; i < Constants.sendError.length; i++) {

                                    dialogMessage.append("\t - ").append(Constants.sendError[i]).append("\n");
                                }

                            } else {

                                // Prikaz liste gresaka na osnovu nivoa tabele
                                dialogMessage.append(getResources().getString(R.string.data_not_sync, ex.getMessage()));
                                // Sinhronizacija za MP (greske)

                                for (int i = Constants.nivoSinhronizacije; i < Constants.imeNivoaSinhronizacije.length; i++) {
                                    dialogMessage.append("\t - ").append(Constants.imeNivoaSinhronizacije[i]).append("\n");
                                }

                            }
                            dialogMessage.append(getResources().getString(R.string.try_sync_again_prompt));


                            DialogBuilder.showDialogWithYesNoCallback(MainActivity.this, getResources().getString(R.string.sync_mandatory_lbl), dialogMessage.toString()
                                    , new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Constants.nivoSinhronizacije = 0;
                                            SyncAsync ws = new SyncAsync();
                                            ws.execute();
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            nc.isLogin = false;
                                        }
                                    });
                        } else {
                            Utility.showToast(MainActivity.this, getResources().getString(R.string.error_login_try_again));

                        }
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.cancel();
            }

            if (!errorHappened) {
                if (Constants.EMPLOYEE_ID > 0) {
                    if (Constants.newVersionCode != Constants.versionCode) {
                        DialogBuilder.showOkDialogWithCallback(MainActivity.this, getResources().getString(R.string.new_version_warning), getResources().getString(R.string.download_version_prompt), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = Constants.newVersionDownloadLink;
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                finish();
                            }
                        });
                    } else {
                        // ukoliko nisu svi podaci posalti kako treba ispisi
                        // samo da nisu i nastavi
                        if (unsentData.trim().isEmpty()) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.succes_login), Toast.LENGTH_LONG).show();
                            Intent i = new Intent();
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            i.setClass(MainActivity.this, DashboardActivity.class);
                            startActivity(i);
                            // setLoginDate();
                            nc.isLogin = false;
                        } else {
                            DialogBuilder.showOkDialogWithCallback(MainActivity.this, getResources().getString(R.string.all_data_not_sent), unsentData, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.succes_login), Toast.LENGTH_LONG)
                                            .show();
                                    Intent i = new Intent();
                                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    i.setClass(MainActivity.this, DashboardActivity.class);
                                    startActivity(i);
                                    // setLoginDate();
                                    nc.isLogin = false;
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.error_login_try_again), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }
}
