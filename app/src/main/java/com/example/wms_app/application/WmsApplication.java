package com.example.wms_app.application;

import android.app.Application;
import android.content.Context;

public class WmsApplication extends Application {
    public static WmsApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    public static WmsApplication getInstance() {
        return instance;
    }
}
