package com.example.wms_app.utilities;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import androidx.appcompat.app.AlertDialog;

public class InternetCheck extends AsyncTask<Void,Void,Boolean> {

    private Consumer mConsumer;
    private AlertDialog loadingDialog;
    public  interface Consumer { void accept(Boolean internet); }

    public  InternetCheck(Consumer consumer, Context context) {
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        mConsumer = consumer; execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadingDialog.show();
    }

    @Override protected Boolean doInBackground(Void... voids) { try {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
        sock.close();
        return true;
    } catch (IOException e) { return false; } }

    @Override protected void onPostExecute(Boolean internet) {
        if(loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        mConsumer.accept(internet);
    }
}
