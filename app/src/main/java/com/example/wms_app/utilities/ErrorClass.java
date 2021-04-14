package com.example.wms_app.utilities;

import android.app.Activity;
import android.database.SQLException;

import org.json.JSONException;

import java.io.IOException;

public class ErrorClass {
    public static void handle(Exception ex, Activity context)
    {
        Utility.writeErrorToFile(ex);
        ex.printStackTrace();
        if(ex != null)
        {
            if(ex instanceof IOException)
            {
                DialogBuilder.showOkDialogWithoutCallback(context, "Nema interneta", ex.getMessage()).show();
            }
            else if(ex instanceof SQLException)
            {
                DialogBuilder.showOkDialogWithoutCallback(context, "Greška u lokalnoj bazi!", ex.getMessage()).show();
            }
            else if(ex instanceof JSONException)
            {
                DialogBuilder.showOkDialogWithoutCallback(context, "Greška pri parsiranju jsona!", ex.getMessage()).show();
            }
            else
            {
                DialogBuilder.showOkDialogWithoutCallback(context, "Exception", ex.getMessage()).show();
            }
        }


    }
}
