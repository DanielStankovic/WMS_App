package com.example.wms_app.utilities.mapper;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetails;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.Constants;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.Partition;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AsyncGetGroupedIncoming extends AsyncTask<Void, Void, IncomingGrouped> {

    private final Consumer mConsumer;
    private final AlertDialog loadingDialog;
    private final ProductBoxDao productBoxDao;
    private final List<Incoming> incList;
    private final boolean mShowDialog;
    private final Date dateTo;

    public interface Consumer {
        void accept(IncomingGrouped incomingGrouped);
    }

    public AsyncGetGroupedIncoming(Consumer mConsumer, Context context, List<Incoming> incomingList, boolean showLoadingDialog, Date dateTo) {
        this.mConsumer = mConsumer;
        this.loadingDialog = DialogBuilder.getLoadingDialog(context);
        ;
        this.productBoxDao = RoomDb.getDatabase(context).productBoxDao();
        ;
        this.incList = incomingList;
        this.mShowDialog = showLoadingDialog;
        this.dateTo = dateTo;
        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mShowDialog)
            loadingDialog.show();
    }

    @Override
    protected IncomingGrouped doInBackground(Void... voids) {
        try {
            //Izbacivanje otkazanih naloga iz liste
            incList.removeIf(x -> x.getIncomingStatusCode().equals(Constants.STATUS_MAP.get(Constants.INCOMING_STATUS_CANCELED)));
            //Sortiranje da bi se videli onim redom kojim ce se vezivati skenirani artikli za njih posle
            incList.sort(Comparator.comparing(Incoming::getIncomingDate).thenComparing(Incoming::getIncomingID));
            List<IncomingDetails> incomingDetailsList = incList.stream().flatMap(x -> x.getIncomingDetails().stream()).collect(Collectors.toList());
            List<Integer> ids = incomingDetailsList
                    .stream()
                    .map(IncomingDetails::getProductBoxID)
                    .distinct()
                    .collect(Collectors.toList());

            List<List<Integer>> wrapperIncomingDetailsIDList = Partition.ofSize(ids, 499);
            List<ProductBox> productBoxList = new ArrayList<>();
            for (List<Integer> listOfIDs : wrapperIncomingDetailsIDList) {
                productBoxList.addAll(productBoxDao.getProductBoxesByID(listOfIDs));
            }

            List<String> incomingIDList = incList.stream().map(Incoming::getIncomingID)
                    .distinct()
                    .collect(Collectors.toList());

            // List<ProductBox> productBoxList = productBoxDao.getProductBoxesByID(ids);
            if (productBoxList == null || productBoxList.isEmpty()) {
                return null;
            }

            IncomingGrouped incomingGrouped = new IncomingGrouped();
            incomingGrouped.setTotalNumOfProds(incList.stream().mapToInt(Incoming::getTotalNumOfProd).sum());
            incomingGrouped.setTotalNumOfIncomings(incomingIDList.size());
            incomingGrouped.setUniqueProductBoxIDList(incList.stream()
                    .flatMap(incoming -> incoming.getIncomingDetails().stream())
                    .map(IncomingDetails::getProductBoxID)
                    .distinct()
                    .collect(Collectors.toList()));
            incomingGrouped.setIncomingDetailsList(incomingDetailsList);
            incomingGrouped.setPeriod(Utility.getStringFromDate(dateTo, true));
            incomingGrouped.setIncomingIDList(incomingIDList);


            StringJoiner stringJoiner = new StringJoiner("\n* ", "* ", "");

            for (ProductBox prodBox : productBoxList) {
                int expectedQuantity = (int) incomingDetailsList.stream().filter(x -> x.getProductBoxID() == prodBox.getProductBoxID())
                        .mapToDouble(IncomingDetails::getQuantity)
                        .sum();
                String textToJoin = prodBox.getProductBoxName() + " " + "--- Kol: (" + expectedQuantity + ")";

                stringJoiner.add(textToJoin);
            }


            String incomingCodes = "- " + incList.stream().map(Incoming::getIncomingCode)
                    .distinct()
                    .collect(Collectors.joining("\n- "));
            incomingGrouped.setProductNames(stringJoiner.toString());
            incomingGrouped.setIncomingCodes(incomingCodes);
            return incomingGrouped;

        } catch (Exception e) {
            Utility.writeErrorToFile(e);
            return null;
        }

    }

    @Override
    protected void onPostExecute(IncomingGrouped incomingGrouped) {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        mConsumer.accept(incomingGrouped);
    }
}
