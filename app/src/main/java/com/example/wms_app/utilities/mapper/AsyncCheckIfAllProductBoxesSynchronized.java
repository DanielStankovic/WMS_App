package com.example.wms_app.utilities.mapper;

import android.os.AsyncTask;

import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.utilities.Partition;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

public class AsyncCheckIfAllProductBoxesSynchronized extends AsyncTask<Void, Void, Boolean> {

    private final Consumer mConsumer;
    private final ProductBoxDao productBoxDao;
    private final List<Integer> listOfArticlesToCheck;
    boolean allExist = false;

    public interface Consumer {
        void accept(boolean allExist);
    }

    public AsyncCheckIfAllProductBoxesSynchronized(ProductBoxDao productBoxDao, List<Integer> idSet, Consumer consumer) {
        this.productBoxDao = productBoxDao;
        this.mConsumer = consumer;
        this.listOfArticlesToCheck = idSet;
        execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {

            List<List<Integer>> wrapperProductBoxIDList = Partition.ofSize(listOfArticlesToCheck, 499);
            List<Integer> productBoxIDList = new ArrayList<>();

            for (List<Integer> listOfIDs : wrapperProductBoxIDList) {
                productBoxIDList.addAll(productBoxDao.checkIfAllProductBoxesSynchronized(listOfIDs));
            }

//            List<Integer> listOfArticleIDsFromDb = productBoxDao.checkIfAllProductBoxesSynchronized(listOfArticlesToCheck);

            if (productBoxIDList.size() == listOfArticlesToCheck.size()) {

                allExist = productBoxIDList.containsAll(listOfArticlesToCheck);

            } else {
                allExist = false;
            }

        } catch (Exception e) {
            Utility.writeErrorToFile(e);
        }
        return allExist;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mConsumer.accept(allExist);
    }
}
