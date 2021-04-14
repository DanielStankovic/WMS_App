package com.example.wms_app.utilities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.wms_app.activity.MainActivity;
import com.example.wms_app.application.WmsApplication;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Activity activity;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity a) {
        activity = a;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            Utility.writeErrorToFile((Exception) ex);

            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("error", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(WmsApplication.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) WmsApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
            activity.finish();
            System.exit(2);
        } catch (Exception e) {
            Utility.writeErrorToFile(e);
        }
    }
}
