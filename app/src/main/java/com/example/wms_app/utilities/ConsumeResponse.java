package com.example.wms_app.utilities;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.snackbar.Snackbar;
import com.example.wms_app.R;

public class ConsumeResponse {

    public static void consumeResponse(ApiResponse apiResponse, AlertDialog loadingDialog,
                                       AlertDialog errorDialog, Resources resources,
                                       ViewBinding binding, Context context, Runnable refreshApiFunction) {

        switch (apiResponse.status) {

            case LOADING:
                loadingDialog.show();
                break;

            case SUCCESS:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                refreshApiFunction.run();
                // Utility.showToast(context, getResources().getString(R.string.successString));
                break;

            case SUCCESS_WITH_ACTION:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(binding.getRoot(), apiResponse.error, Snackbar.LENGTH_LONG);
                snackbar.show();
                refreshApiFunction.run();

                break;

            case ERROR:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                errorDialog.setMessage(resources.getString(R.string.error_string, apiResponse.error));
                errorDialog.show();
                refreshApiFunction.run();
                break;

            case PROMPT:
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                DialogBuilder.showDialogWithYesCallback(context, resources.getString(R.string.warning), apiResponse.error, apiResponse.yesListener);
                refreshApiFunction.run();
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
