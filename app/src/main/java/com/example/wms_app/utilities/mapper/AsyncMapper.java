package com.example.wms_app.utilities.mapper;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingDetailsResult;
import com.example.wms_app.model.IncomingDetailsResultLocal;
import com.example.wms_app.model.ProductBox;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class AsyncMapper {

    public static LiveData<List<ProductBox>> getProductBoxFromResult(
            @NonNull LiveData<Incoming> source1,
            @NonNull LiveData<List<IncomingDetailsResult>> source2,
            @NonNull LiveData<List<IncomingDetailsResult>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction) {


        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction.get());
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
        return result;
    }

    public static LiveData<List<ProductBox>> getProductBoxFromResultGrouped(
            @NonNull LiveData<Incoming> source1,
            @NonNull LiveData<List<IncomingDetailsResult>> source2,
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction) {


        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction.get());
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
        return result;
    }

    public static LiveData<List<ProductBox>> getProductBoxFromResult(
            @NonNull LiveData<Incoming> source1,
            @NonNull LiveData<List<IncomingDetailsResult>> source2,
            @NonNull final Supplier<List<ProductBox>> mapFunction) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        result.addSource(source2, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));

        return result;
    }

    public static LiveData<List<ProductBox>> getProductBoxFromResultGrouped(
            @NonNull LiveData<Incoming> source1,
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source2,
            @NonNull final Supplier<List<ProductBox>> mapFunction) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        result.addSource(source2, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));

        return result;
    }

    public static LiveData<List<IncomingDetailsResultLocal>> getProductBoxFromResult(
            @NonNull LiveData<List<IncomingDetailsResult>> source1,
            @NonNull final Supplier<List<IncomingDetailsResultLocal>> mapFunction) {
        final MediatorLiveData<List<IncomingDetailsResultLocal>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

    public static LiveData<List<IncomingDetailsResultLocal>> getProductBoxFromResultGrouped(
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source1,
            @NonNull final Supplier<List<IncomingDetailsResultLocal>> mapFunction) {
        final MediatorLiveData<List<IncomingDetailsResultLocal>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

    public static LiveData<List<ProductBox>> getProductionProductBoxFromResult(
            @NonNull LiveData<List<Integer>> source1,
            @NonNull LiveData<List<IncomingDetailsResult>> source2,
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source3,
            @NonNull final Supplier<List<ProductBox>> mapFunction
//           LoadingDialogInterface loadingDialogInterface
//            @NonNull final Function<List<ProductBox>, List<ProductBox>> mapFunction2
    ) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();

        class AsyncExecutor extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                loadingDialogInterface.showLoadingDialog();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                result.postValue(mapFunction.get());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
//                loadingDialogInterface.hideLoadingDialog();
            }
        }


//        List<AsyncExecutor> taskList = new ArrayList<>();

        result.addSource(source1, x -> {

            AsyncExecutor task1 = new AsyncExecutor();
            task1.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//            taskList.add(task1);
        });
        result.addSource(source2, x -> {
            AsyncExecutor task2 = new AsyncExecutor();
            task2.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//            taskList.add(task2);
        });
        result.addSource(source3, x -> {
            AsyncExecutor task3 = new AsyncExecutor();
            task3.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//            taskList.add(task3);
        });

//        result.addSource(source2, x -> AsyncTask.execute(() -> {
//            if (result.getValue() != null) {
//                result.postValue(mapFunction2.apply(result.getValue()));
//            }
//        }));
//        result.addSource(source3, x -> AsyncTask.execute(() -> {
//            if (result.getValue() != null) {
//                result.postValue(mapFunction2.apply(result.getValue()));
//            }
//        }));

        return result;
    }

    public static LiveData<List<ProductBox>> getProductionProductBoxFromResult(
            @NonNull LiveData<List<Integer>> source1,
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source2,
            @NonNull final Supplier<List<ProductBox>> mapFunction) {
        final MediatorLiveData<List<ProductBox>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        result.addSource(source2, x -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));

        return result;
    }

    public static LiveData<List<IncomingDetailsResultLocal>> getIncomingDetailsLocalFromResult(
            @NonNull LiveData<HashMap<String, List<IncomingDetailsResult>>> source1,
            @NonNull final Supplier<List<IncomingDetailsResultLocal>> mapFunction) {
        final MediatorLiveData<List<IncomingDetailsResultLocal>> result = new MediatorLiveData<>();
        result.addSource(source1, incomingDetails -> AsyncTask.execute(() -> result.postValue(mapFunction.get())));
        return result;
    }

}
