package com.example.wms_app.utilities.mapper;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingGrouped;
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

public class AsyncGetGroupedOutgoing extends AsyncTask<Void, Void, OutgoingGrouped> {

    private final Consumer mConsumer;
    private final AlertDialog loadingDialog;
    private final ProductBoxDao productBoxDao;
    private final List<Outgoing> outList;
    private final boolean mShowDialog;
    private final Date dateTo;

    public interface Consumer {
        void accept(OutgoingGrouped outgoingGrouped);
    }

    public AsyncGetGroupedOutgoing(Consumer consumer, Context context, List<Outgoing> outgoingList, boolean showLoadingDialog, Date dateTo) {
        productBoxDao = RoomDb.getDatabase(context).productBoxDao();
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        mConsumer = consumer;
        outList = outgoingList;
        this.dateTo = dateTo;
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
    protected OutgoingGrouped doInBackground(Void... voids) {
        try {
            //Izbacivanje otkazanih naloga iz liste

            outList.removeIf(x -> x.getOutgoingStatusCode().equals(Constants.OUTGOING_STATUS_MAP.get(Constants.OUTGOING_STATUS_CANCELED)));

            outList.sort(Comparator.comparing(Outgoing::getOutgoingDate).thenComparing(Outgoing::getOutgoingID));

            List<OutgoingDetails> outgoingDetailsList = outList.stream().flatMap(x -> x.getOutgoingDetails().stream()).collect(Collectors.toList());

            List<Integer> ids = outgoingDetailsList
                    .stream()
                    .map(OutgoingDetails::getProductBoxID)
                    .distinct()
                    .collect(Collectors.toList());

            List<List<Integer>> wrapperOutgoingDetailsIDList = Partition.ofSize(ids, 499);
            List<ProductBox> productBoxList = new ArrayList<>();
            for (List<Integer> listOfIDs : wrapperOutgoingDetailsIDList) {
                productBoxList.addAll(productBoxDao.getProductBoxesByID(listOfIDs));
            }
            List<String> outgoingIDList = outList.stream().map(Outgoing::getOutgoingID)
                    .distinct()
                    .collect(Collectors.toList());

            if (productBoxList == null || productBoxList.isEmpty()) {
                return null;
            }

            OutgoingGrouped outgoingGrouped = new OutgoingGrouped();
            outgoingGrouped.setTotalNumOfProds(outList.stream().mapToInt(Outgoing::getTotalNumOfProd).sum());
            outgoingGrouped.setTotalNumOfOutgoings(outgoingIDList.size());
            outgoingGrouped.setOutgoingDetailsList(outgoingDetailsList);
            outgoingGrouped.setPeriod(Utility.getStringFromDate(dateTo, true));
            outgoingGrouped.setOutgoingIDList(outgoingIDList);

            StringJoiner stringJoiner = new StringJoiner("\n* ", "* ", "");

            for (ProductBox prodBox : productBoxList) {
                int expectedQuantity = (int) outgoingDetailsList.stream().filter(x -> x.getProductBoxID() == prodBox.getProductBoxID())
                        .mapToDouble(OutgoingDetails::getQuantity)
                        .sum();
                String textToJoin = prodBox.getProductBoxName() + " " + "--- Kol: (" + expectedQuantity + ")";

                stringJoiner.add(textToJoin);
            }

//            String productNames = "* " + productBoxList
//                    .stream()
//                    .distinct()
//                    .map(ProductBox::getProductBoxName)
//                    .collect(Collectors.joining("\n* "));

            String outgoingCodes = "- " + outList.stream().map(Outgoing::getOutgoingCode)
                    .distinct()
                    .collect(Collectors.joining("\n- "));

            outgoingGrouped.setProductNames(stringJoiner.toString());
            outgoingGrouped.setOutgoingCodes(outgoingCodes);
            return outgoingGrouped;

        } catch (Exception e) {
            Utility.writeErrorToFile(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(OutgoingGrouped outgoingGrouped) {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        mConsumer.accept(outgoingGrouped);
    }
}
