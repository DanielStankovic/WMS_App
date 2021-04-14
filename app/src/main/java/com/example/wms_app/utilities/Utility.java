package com.example.wms_app.utilities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.firestore.QuerySnapshot;
import com.example.wms_app.enums.EnumIncomingStyle;
import com.example.wms_app.enums.EnumOutgoingStyle;
import com.example.wms_app.enums.EnumSoftKeyboard;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.model.ProductBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import retrofit2.Response;

public class Utility {

    public static boolean isOnline(Context context) {
        boolean connected = false;
        try {

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();


        } catch (Exception e) {

        }
        return connected;
    }

    public static String getErrorLogPath() {
        String root = Environment.getExternalStorageDirectory().toString();
        root = root + "/ErrorLog/";
        File myDir = new File(root);
        myDir.mkdirs();
        return root;
    }

    public static String getDatabasePath() {
        String root = Environment.getExternalStorageDirectory().toString();
        root = root + "/DatabaseFile/";
        File myDir = new File(root);
        myDir.mkdirs();
        return root;
    }

    private static String getServiceAddressPath() {
        String root = Environment.getExternalStorageDirectory().toString();
        root = root + "/wcf/";
        File myDir = new File(root);
        myDir.mkdirs();
        return root;
    }

    public static boolean isErrorLogFileExists() {
        File file = new File(getErrorLogPath(), Constants.ERROR_FILE);
        return file.exists();
    }

    public static void writeErrorToFile(Exception e) {


        StringBuilder text = new StringBuilder();
        try {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("dd. MMM yyyy. HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            File file = new File(getErrorLogPath(), Constants.ERROR_FILE);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            writer.append("\n---------------------------" + formattedDate + "--------------------------------\n");
            e.printStackTrace(writer);
            writer.append(text.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean checkResponseFromServer(Response response){
        return response.isSuccessful() && response.body() != null;
    }

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public static String getStringFromDate(Date date, boolean isSerbian){
        String currentResult = "";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatGmt.setTimeZone(TimeZone.getDefault());
        currentResult = dateFormatGmt.format(date.getTime()).toString();

        if(isSerbian){
            try
            {
                if (currentResult.contains("-"))
                {
                    currentResult = currentResult.split(" ")[0];
                    String day = currentResult.split("-")[2];
                    String month = currentResult.split("-")[1];
                    String year = currentResult.split("-")[0];
                    return day + "." + month + "." + year + ".";
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                writeErrorToFile(e);
            }
        }


        return currentResult;
    }


    public static String getStringFromDateForServer(Date date){
        String currentResult = "";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormatGmt.setTimeZone(TimeZone.getDefault());
        currentResult = dateFormatGmt.format(date.getTime()).toString();

        return currentResult;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public static void toggleSoftKeyboard(Context context, View view, EnumSoftKeyboard enumSoftKeyboard){
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(enumSoftKeyboard == EnumSoftKeyboard.HIDE) {
            view.clearFocus();
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }else{
            view.requestFocus();
            if(imm != null)
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    public static  <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static int getSpinnerSelectionByValue(List<ProductBox> productList, ProductBox product) {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getProductBoxID() == product.getProductBoxID()) {
                return i;
            }
        }
        return 0;
    }


    public static EnumOutgoingStyle getCurrentOutgoingType(Outgoing outgoing, OutgoingGrouped outgoingGrouped) {

        if (outgoingGrouped != null && outgoing == null)
            return EnumOutgoingStyle.GROUPED;
        else
            return EnumOutgoingStyle.SINGLE;
    }

    public static EnumIncomingStyle getCurrentIncomingType(Incoming incoming, IncomingGrouped incomingGrouped) {

        if (incomingGrouped != null && incoming == null)
            return EnumIncomingStyle.GROUPED;
        else
            return EnumIncomingStyle.SINGLE;
    }

    public static void deleteErrorLogFile() {
        File file = new File(getErrorLogPath(), Constants.ERROR_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String readFile(Context context, String filename) {
        try {
            File file = new File(getServiceAddressPath(), Constants.SERVICE_ADDRESS_FILE);
            if (!file.exists()) {
                return "NOT FOUND";
            }
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }


    public static boolean exportDB(String dbPath) {
        boolean dbExported = false;
        try {

            File dbFile = new File(dbPath);
            if (!dbFile.exists())
                return false;

            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = getDatabasePath() +
                    Constants.DB_NAME + ".db";

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();
            dbExported = true;
        } catch (Exception ex) {
            writeErrorToFile(ex);
        }

        return dbExported;
    }

    public static boolean isFirebaseSourceFromServer(QuerySnapshot snapshots) {
        return !snapshots.getMetadata().hasPendingWrites();
    }

    public static int getEmployeeIDFromInput(int constantsEmployeeID, int dbEmployeeID) {
        if (constantsEmployeeID > 0)
            return constantsEmployeeID;
        if (dbEmployeeID > 0)
            return dbEmployeeID;

        return -1;
    }
}
