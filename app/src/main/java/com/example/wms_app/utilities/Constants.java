package com.example.wms_app.utilities;

import com.example.wms_app.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    //SWITCH DATA WEDGE PROFIL
    public static final String ACTION_SWITCHTOPROFILE = "com.symbol.datawedge.api.ACTION_SWITCHTOPROFILE";
    public static final String EXTRA_PROFILENAME = "com.symbol.datawedge.api.EXTRA_PROFILENAME";
    public static final String EXTRA_SEND_RESULT = "SEND_RESULT";

    //BAZA
    public static final int DB_VERSION = 1; // AKTIVNA:1;
    public static final String DB_NAME = "WMS_APP_db";
    public static final int WRITE_BATCH_LIMIT = 495;

    //ERORLOG
    public static String ERROR_FILE = "errorLog.txt";

    //ERORLOG
    public static String SERVICE_ADDRESS_FILE = "serviceAddress.txt";

    //MAGACIN
    public static String global_warehouse_code = "";

    //POZICIJE
    public static int POSITION_BARCODE_LENGTH = 6;
    public static int SUB_POSITION_BARCODE_LENGTH = 8;

    //FIREBASE
    public static final int FIRESTORE_IN_QUERY_LIMIT = 9;

    public final static String[] imeNivoaSinhronizacije = {

            "Provera verzije",
            //  "Rampe za utovar",
            "Kategorije proizvoda",
            //    "Proizvodi",
            //  "Partneri",
            "Magacini",
            "Magacini - Pozicija",
            "Magacini - Objekat",
            "Kamioni",
            "Kutije proizvoda",
            //    "Razlozi za povrat",
            "Tipovi prijema",
            "Tipovi prijema iz proizvodnje",
            "Tipovi proizvoda",
            //   "Prijemi",
//            "Magacini - Podpozicija",
            //  "Otprema",
            "Uspešno logovanje"};


    public final static String[] sendError = {
//            "START-STOP"
    };

    public static int EMPLOYEE_ID = -1;
    public static Integer nivoSinhronizacije = 0;
    public static int versionCode = BuildConfig.VERSION_CODE;
    public static int newVersionCode = 0;
    public static String newVersionName = "";
    public static String newVersionDownloadLink = "";
    public static Integer nivoSlanja = 0;

    public static String SELECTED_PRODUCTION_TYPE_TAG = "SelectedProductionTypeTag";

    public static int INCOMING_STATUS_ACTIVE = 0;
    public static int INCOMING_STATUS_FINISHED_PARTIALLY = 1;
    public static int INCOMING_STATUS_FINISHED_COMPLETELY = 2;
    public static int INCOMING_STATUS_FINISHED_WITH_SURPLUS = 3;
    public static int INCOMING_STATUS_CANCELED = 4;

    public static Map<Integer, String> STATUS_MAP = new HashMap<Integer, String>() {{
        put(INCOMING_STATUS_ACTIVE, "02");
        put(INCOMING_STATUS_FINISHED_PARTIALLY, "03");
        put(INCOMING_STATUS_FINISHED_COMPLETELY, "04");
        put(INCOMING_STATUS_CANCELED, "07");
        put(INCOMING_STATUS_FINISHED_WITH_SURPLUS, "08");
    }};

    public static int OUTGOING_STATUS_ACTIVE = 0;
    public static int OUTGOING_STATUS_FINISHED_PARTIALLY = 1;
    public static int OUTGOING_STATUS_FINISHED_COMPLETELY = 2;
    public static int OUTGOING_STATUS_CANCELED = 3;

    public static Map<Integer, String> OUTGOING_STATUS_MAP = new HashMap<Integer, String>() {{
        put(OUTGOING_STATUS_ACTIVE, "02");
        put(OUTGOING_STATUS_FINISHED_PARTIALLY, "03");
        put(OUTGOING_STATUS_FINISHED_COMPLETELY, "04");
        put(OUTGOING_STATUS_CANCELED, "07");
    }};

    public static final String SELECTED_OUTGOING_ID_TAG = "SelectedOutgoingID";
    public static final String SELECTED_OUTGOING_GROUPED_ID_TAG = "SelectedOutgoingGroupedID";

    public static final String SELECTED_INCOMING_ID_TAG = "SelectedIncomingID";
    public static final String SELECTED_INCOMING_GROUPED_ID_TAG = "SelectedIncomingGroupedID";
}
