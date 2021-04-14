package com.example.wms_app.utilities.mapper;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingDetailsResult;
import com.example.wms_app.model.OutgoingDetailsResultPreview;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.WarehouseStatusPositionDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class AsyncMapperPhaseTwo {

    public static MutableLiveData<List<ProductBox>> getProductBoxForSpinnerPhaseTwo(
            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source1,
            @NonNull LiveData<List<OutgoingDetailsResult>> source2,
            @NonNull LiveData<List<OutgoingDetailsResult>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction1
    ) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction1.get());
                return null;
            }
        }
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

        List<ProductBox> productBoxList = new ArrayList<>();
        productBoxList.add(ProductBox.newPlaceHolderInstance());
        result.setValue(productBoxList);
        return result;
    }

    public static MutableLiveData<List<ProductBox>> getProductBoxForSpinnerPhaseTwoGrouped(
            @NonNull LiveData<List<WarehouseStatusPositionDetails>> source1,
            @NonNull LiveData<List<OutgoingDetailsResult>> source2,
            @NonNull LiveData<HashMap<String, List<OutgoingDetailsResult>>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction1
    ) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction1.get());
                return null;
            }
        }
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

        List<ProductBox> productBoxList = new ArrayList<>();
        productBoxList.add(ProductBox.newPlaceHolderInstance());
        result.setValue(productBoxList);
        return result;
    }


    public static LiveData<List<OutgoingDetailsResultPreview>> getOutgoingPhaseTwoLeftPreview(
            @NonNull LiveData<Outgoing> source1,
            @NonNull MutableLiveData<List<OutgoingDetailsResult>> source2,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction
    ) {
        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();

        result.addSource(source1, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));

        result.addSource(source2, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));
        return result;
    }

    public static LiveData<List<OutgoingDetailsResultPreview>> getOutgoingPhaseTwoLeftPreviewGrouped(
            @NonNull LiveData<Outgoing> source1,
            @NonNull MutableLiveData<HashMap<String, List<OutgoingDetailsResult>>> source2,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction
    ) {
        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();

        result.addSource(source1, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));

        result.addSource(source2, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));
        return result;
    }


    public static LiveData<List<OutgoingDetailsResultPreview>> getOutgoingPhaseTwoDonePreview(
            @NonNull MutableLiveData<List<OutgoingDetailsResult>> source1,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction) {

        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();

        result.addSource(source1, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));

        return result;
    }

    public static LiveData<List<OutgoingDetailsResultPreview>> getOutgoingPhaseTwoDonePreviewGrouped(
            @NonNull MutableLiveData<HashMap<String, List<OutgoingDetailsResult>>> source1,
            @NonNull final Supplier<List<OutgoingDetailsResultPreview>> mapFunction) {

        final MediatorLiveData<List<OutgoingDetailsResultPreview>> result = new MediatorLiveData<>();

        result.addSource(source1, x -> AsyncTask.execute(() -> {
            result.postValue(mapFunction.get());
        }));

        return result;
    }

}
