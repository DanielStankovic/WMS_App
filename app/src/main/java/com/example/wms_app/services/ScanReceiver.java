package com.example.wms_app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.wms_app.R;

public class ScanReceiver extends BroadcastReceiver {


    private OnScanReceivedListener listener = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle b = intent.getExtras();

        if (action.equals(context.getResources().getString(R.string.activity_intent_filter_action))) {
            //  Ovde se pribavlja barkod
            try {
                //  stavljamo vrednost u interfejs
                if (listener != null) {
                    listener.onScanReceived(intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data)));
                }
            } catch (Exception e) {
                //Ukoliko je doslo do greske ili je skeniran kod koji nije citljiv
            }
        }
    }


    //ova metoda sluzi za setovanje Receivera stavlja se u klijentsku klasu
    public void registerScanReceiver(ScanReceiver scanReceiver, Context context){

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(context.getResources().getString(R.string.activity_intent_filter_action));
        context.registerReceiver(scanReceiver, filter);
    }


    public interface OnScanReceivedListener {
        public void onScanReceived(String barcode);
    }

    public void setOnScanReceivedListener(Context context) {
        this.listener = (OnScanReceivedListener) context;
    }
}
