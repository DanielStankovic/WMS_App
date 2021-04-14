package com.example.wms_app.utilities.mapper;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.WarehouseStatusPositionDetails;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.Partition;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsyncGetProductBoxesByIDsPhaseOne extends AsyncTask<Void, Void, List<ProductBox>> {

    private final Consumer mConsumer;
    private final AlertDialog loadingDialog;
    private final ProductBoxDao productBoxDao;
    private final List<WarehouseStatusPositionDetails> mWarehouseStatusPositionDetailsList;
    private final List<OutgoingDetailsResult> mOutgoingDetailsResultList;
    private final List<WarehouseStatusPositionDetails> mWarehouseStatusPositionDetailsPreloadingList;
    private final List<OutgoingDetails> mOutgoingDetailsList;
    private final boolean mShowDialog;

    public interface Consumer {
        void accept(List<ProductBox> productBoxList);
    }

    public AsyncGetProductBoxesByIDsPhaseOne(Consumer consumer,
                                             Context context,
                                             List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList,
                                             boolean showLoadingDialog,
                                             List<OutgoingDetailsResult> outgoingDetailsResultList,
                                             List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsPreloadingList,
                                             List<OutgoingDetails> outgoingDetailsList) {
        productBoxDao = RoomDb.getDatabase(context).productBoxDao();
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        mConsumer = consumer;
        mWarehouseStatusPositionDetailsList = warehouseStatusPositionDetailsList;
        mOutgoingDetailsResultList = outgoingDetailsResultList;
        mWarehouseStatusPositionDetailsPreloadingList = warehouseStatusPositionDetailsPreloadingList;
        mOutgoingDetailsList = outgoingDetailsList;
        mShowDialog = showLoadingDialog;
        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mShowDialog)
            loadingDialog.show();
    }

    @Override
    protected List<ProductBox> doInBackground(Void... voids) {
        try {
            List<Integer> mSet = mWarehouseStatusPositionDetailsList.stream().map(WarehouseStatusPositionDetails::getProductBoxID).distinct().collect(Collectors.toList());
            List<List<Integer>> wrapperProductBoxIDList = Partition.ofSize(mSet, 499);
            List<ProductBox> productBoxList = new ArrayList<>();
            for (List<Integer> listOfIDs : wrapperProductBoxIDList) {
                productBoxList.addAll(productBoxDao.getProductBoxesByID(listOfIDs));
            }

            for (ProductBox productBox : productBoxList) {
                if (
                        (mOutgoingDetailsResultList == null ? 0 :
                                (int) mOutgoingDetailsResultList.stream()
                                        .filter(x -> x.getProductBoxID() == productBox.getProductBoxID())
                                        .mapToDouble(OutgoingDetailsResult::getQuantity)
                                        .sum())
                                +
                                (mWarehouseStatusPositionDetailsPreloadingList == null ? 0 :
                                        (int) mWarehouseStatusPositionDetailsPreloadingList.stream()
                                                .filter(m -> m.getProductBoxID() == productBox.getProductBoxID())
                                                .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                                                .sum())
                                >=
                                (mOutgoingDetailsList == null ? 0 :
                                        (int) mOutgoingDetailsList.stream()
                                                .filter(x -> x.getProductBoxID() == productBox.getProductBoxID())
                                                .mapToDouble(OutgoingDetails::getQuantity)
                                                .sum())

                ) {
                    productBox.setColorStatus(2);
                } else {
                    productBox.setColorStatus(0);
                }
            }

            return productBoxList;

        } catch (Exception e) {
            Utility.writeErrorToFile(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<ProductBox> productBoxList) {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        mConsumer.accept(productBoxList);
    }
}
