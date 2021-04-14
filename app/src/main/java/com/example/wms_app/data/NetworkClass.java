package com.example.wms_app.data;

import android.content.Context;

import com.example.wms_app.repository.MainActivityRepository;
import com.example.wms_app.utilities.Constants;

import java.io.IOException;

public class NetworkClass {

    private int progress = 0;
    public boolean isLogin;
    private increaseDialogProgressInterface mInterface = null;
    private MainActivityRepository mainActivityRepository;


    public NetworkClass(Context context, Boolean _isLogin, increaseDialogProgressInterface mInterface) {
        this.isLogin = _isLogin;
        this.mInterface = mInterface;
        this.progress = 0;
        mainActivityRepository = new MainActivityRepository(context);
        //apiInterface = ApiClient.getApiClient().create(Api.class);
    }

    public int syncData(int progress) throws Exception {

        try {

            this.progress = progress;
            int i = 0;


            mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
            mainActivityRepository.syncApplicationVersion();

            // ukoliko postoji nova app da se prekine sync
            if (Constants.newVersionCode != Constants.versionCode) {
                mainActivityRepository.logoutEmployeeFromFirebase(Constants.EMPLOYEE_ID);
                return i;
            }

//            if (isLogin) {
//
//                if (MainActivity.EMPLOYEE_ID > 0) {
//                    //TODO Brisanje baze
//                    deleteData();
//                }
//            }

            syncAllData(i);

            // sve je proslo kako treba
            Constants.nivoSinhronizacije = -1;

        }catch (Exception e) {
            throw e;
        }

        return this.progress;
    }

    private void syncAllData(int i) throws IOException {

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncProductCategory();

//        Constants.nivoSinhronizacije = ++i;
//        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
//        mainActivityRepository.syncProduct();
//
//        Constants.nivoSinhronizacije = ++i;
//        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
//        mainActivityRepository.syncPartner();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncWarehouse();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncWarehousePosition();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncWarehouseObject();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncTruck();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncProductBox();

//        Constants.nivoSinhronizacije = ++i;
//        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
//        mainActivityRepository.syncReturnReason();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncIncomingType();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncIncomingProductionType();

        Constants.nivoSinhronizacije = ++i;
        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
        mainActivityRepository.syncProductItemType();


//        Constants.nivoSinhronizacije = ++i;
//        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
//        mainActivityRepository.syncIncoming();

//        Constants.nivoSinhronizacije = ++i;
//        mInterface.increaseProgress(5, Constants.imeNivoaSinhronizacije[i]);
//        mainActivityRepository.syncOutgoing();


    }
    public interface increaseDialogProgressInterface {
        void increaseProgress(int progress, String title);
    }
}
