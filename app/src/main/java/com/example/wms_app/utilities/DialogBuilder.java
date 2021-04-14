package com.example.wms_app.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wms_app.R;

import androidx.appcompat.app.AlertDialog;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DialogBuilder {


    public static void showDialogWithYesCallback(Context context, String title, String message, final DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("DA", listener);
        builder.setNeutralButton("NE", null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        dialog.show();
    }

    public static AlertDialog showOkDialogWithoutCallback(Context context, String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(context,  R.style.myDialog);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        return dialog;
    }

    public static void showOkDialogWithCallback(Context context, String title, String message, DialogInterface.OnClickListener listener){

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("OK", listener);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME, "Profile0 (default)", context);
            }
        });

        dialog.show();
    }

    public static AlertDialog getOkDialogWithCallback(Context context, String title, String message, final DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("OK", okListener);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME, "silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME, "Profile0 (default)", context);
            }
        });

        return dialog;
    }

    public static void showDialogWithYesNoCallback(Context context, String title, String message, final DialogInterface.OnClickListener listener1, final DialogInterface.OnClickListener listener2) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("DA", listener1);
        builder.setNegativeButton("NE", listener2);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        dialog.show();
    }
    public static AlertDialog getDialogWithYesNoCallback(Context context, String title, String message, final DialogInterface.OnClickListener listener1, final DialogInterface.OnClickListener listener2){

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("DA", listener1);
        builder.setNegativeButton("NE", listener2);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        return dialog;
    }

    public static AlertDialog getLoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading_data, null);
        ImageView imageView = view.findViewById(R.id.loaderImageView);
        Glide.with(context).asGif().transition(withCrossFade()).diskCacheStrategy(DiskCacheStrategy.ALL).load(R.raw.kolibri_loader).into(imageView);
        builder.setView(view);
        AlertDialog loadingDialog = builder.create();
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        return loadingDialog;

    }

    public static void showNoInternetDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.myDialog);

        builder.setTitle("UPOZORENJE");
        builder.setMessage(context.getResources().getString(R.string.no_internet_msg));
        builder.setNegativeButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"silent", context);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendDataWedgeIntentWithExtra(Constants.ACTION_SWITCHTOPROFILE, Constants.EXTRA_PROFILENAME,"Profile0 (default)", context);
            }
        });

        dialog.show();
    }


    public static void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue, Context context)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        //if (bRequestSendResult)
        dwIntent.putExtra(Constants.EXTRA_SEND_RESULT, "true");
        context.sendBroadcast(dwIntent);
    }
}
