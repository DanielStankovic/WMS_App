package com.example.wms_app.utilities.mapper;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetails;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.WarehouseStatusPosition;
import com.example.wms_app.model.WarehouseStatusPositionDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AsyncMapperPhaseOne {


//    public static MutableLiveData<List<ProductBox>> getProductBoxForSpinnerPhaseOne(
//            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source1,
//            @NonNull LiveData<List<OutgoingDetailsResult>> source2,
//            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source3,
//            List<WarehouseStatusPositionDetails> warehouseStatusPositionDetailsList,
//            List<OutgoingDetailsResult> mOutgoingDetailsResultList,
//            List<WarehouseStatusPositionDetails> mWarehouseStatusPositionDetailsPreloadingList,
//            List<OutgoingDetails> mOutgoingDetailsList,
//            Context context
//    ) {
//        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();
//
//        result.addSource(source1, x -> AsyncTask.execute(() -> {
//            try {
//                if (warehouseStatusPositionDetailsList != null) {
//                    ProductBoxDao productBoxDao = RoomDb.getDatabase(context).productBoxDao();
//                    List<Integer> mSet = warehouseStatusPositionDetailsList.stream().map(WarehouseStatusPositionDetails::getProductBoxID).distinct().collect(Collectors.toList());
//                    List<ProductBox> productBoxList = productBoxDao.getProductBoxesByID(mSet);
//                    for (ProductBox productBox : productBoxList) {
//                        int expectedQty = 0;
//                        if (mOutgoingDetailsList != null) {
//                            expectedQty = (int) mOutgoingDetailsList.stream()
//                                    .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
//                                    .mapToDouble(OutgoingDetails::getQuantity)
//                                    .sum();
//                        }
//                        productBox.setExpectedQuantity(expectedQty);
//                        productBox.setAddedQuantity(0);
//                        productBox.setColorStatus(0);
//                    }
//                    result.postValue(productBoxList);
//                }
//
//            } catch (Exception ex) {
//                Utility.writeErrorToFile(ex);
//            }
//        }));
//
//        result.addSource(source2, x -> AsyncTask.execute(() -> {
//            try {
//
//              if(result.getValue() != null){
//                  changeColorAndQuantities(result,mOutgoingDetailsResultList,
//                          mWarehouseStatusPositionDetailsPreloadingList,mOutgoingDetailsList);
//              }
//
//            } catch (Exception ex) {
//                Utility.writeErrorToFile(ex);
//            }
//        }));
//
//        result.addSource(source3, x -> AsyncTask.execute(() -> {
//            try {
//
//                if(result.getValue() != null){
//                    changeColorAndQuantities(result,mOutgoingDetailsResultList,
//                            mWarehouseStatusPositionDetailsPreloadingList,mOutgoingDetailsList);
//                }
//
//            } catch (Exception ex) {
//                Utility.writeErrorToFile(ex);
//            }
//        }));
//
//        return result;
//    }

    public static MutableLiveData<List<ProductBox>> getProductBoxForSpinnerPhaseOne(
            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source1,
            @NonNull LiveData<List<OutgoingDetailsResult>> source2,
            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction1
            // @NonNull final Function<List<ProductBox>, List<ProductBox>> mapFunction2
    ) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction1.get());
                return null;
            }

        }
        ;

        result.addSource(source1, x -> {
            AsyncExecutor task1 = new AsyncExecutor();
            task1.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        });

        result.addSource(source2, x -> {
            AsyncExecutor task1 = new AsyncExecutor();
            task1.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        });

        result.addSource(source3, x -> {
            AsyncExecutor task1 = new AsyncExecutor();
            task1.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        });

//        result.addSource(source2, x -> AsyncTask.execute(() -> {
//            if (result.getValue() != null) {
//                result.postValue(mapFunction2.apply(result.getValue()));
//            }
//        }));
//
//        result.addSource(source3, x -> AsyncTask.execute(() -> {
//            if (result.getValue() != null) {
//                result.postValue(mapFunction2.apply(result.getValue()));
//            }
//        }));

        List<ProductBox> productBoxList = new ArrayList<>();
        productBoxList.add(ProductBox.newPlaceHolderInstance());
        result.setValue(productBoxList);
        return result;
    }

    private static void changeColorAndQuantities(MediatorLiveData<List<ProductBox>> result,
                                                 List<OutgoingDetailsResult> mOutgoingDetailsResultList,
                                                 List<WarehouseStatusPositionDetails> mWarehouseStatusPositionDetailsPreloadingList,
                                                 List<OutgoingDetails> mOutgoingDetailsList) {
        List<ProductBox> productBoxList = result.getValue();

        for (ProductBox productBox : productBoxList) {

            if (productBox.getProductBoxID() == -1) {
                productBox.setColorStatus(0);
                productBox.setExpectedQuantity(0);
                productBox.setAddedQuantity(0);
                continue;
            }

            int addedQuantityInTemp;
            int addedQuantityOnPreloadingPos;
            int expectedQuantity;

            addedQuantityInTemp = mOutgoingDetailsResultList == null ? 0 :
                    (int) mOutgoingDetailsResultList.stream()
                            .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(OutgoingDetailsResult::getQuantity)
                            .sum();

            addedQuantityOnPreloadingPos = mWarehouseStatusPositionDetailsPreloadingList == null ? 0 :
                    (int) mWarehouseStatusPositionDetailsPreloadingList.stream()
                            .filter(m -> m.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(WarehouseStatusPositionDetails::getQuantity)
                            .sum();

            expectedQuantity = mOutgoingDetailsList == null ? 0 :
                    (int) mOutgoingDetailsList.stream()
                            .filter(k -> k.getProductBoxID() == productBox.getProductBoxID())
                            .mapToDouble(OutgoingDetails::getQuantity)
                            .sum();

            productBox.setAddedQuantity(addedQuantityInTemp + addedQuantityOnPreloadingPos);
            productBox.setExpectedQuantity(expectedQuantity);

            //Postavljanje colorstatus zbog bojenja u spinneru
            if (addedQuantityInTemp + addedQuantityOnPreloadingPos >= expectedQuantity) {
                productBox.setColorStatus(2);
            } else {
                productBox.setColorStatus(0);
            }

        }
        result.postValue(productBoxList);
    }


    public static LiveData<List<OutgoingDetailsResultPreview>> getTempListPreview(
            @NonNull LiveData<List<OutgoingDetailsResult>> source1,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction) {
        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();
        result.addSource(source1, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

    public static LiveData<List<OutgoingDetailsResultPreview>> getOutgoingPhaseOnePreview(
            @NonNull LiveData<Outgoing> source1,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction) {
        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();
        result.addSource(source1, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

    public static LiveData<List<OutgoingDetailsResultPreview>> getPreloadingPositionPreview(
            @NonNull LiveData<List<WarehouseStatusPosition>> source1,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction) {
        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();
        result.addSource(source1, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

}
