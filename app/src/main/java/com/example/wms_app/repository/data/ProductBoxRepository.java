package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.ProductBox;

import java.util.List;

import androidx.lifecycle.LiveData;

public class ProductBoxRepository {

//    private MutableLiveData<List<ProductBox>> allProductBoxes;
private final ProductBoxDao productBoxDao;

    public ProductBoxRepository(Context context) {
        RoomDb db = RoomDb.getDatabase(context);
        productBoxDao = db.productBoxDao();
//        allProductBoxes = new MutableLiveData<>();
//        allProductBoxes.setValue(productBoxDao.getProductBoxList());
    }

//    public MutableLiveData<List<ProductBox>> getAllProductBoxes() {
//        return allProductBoxes;
//    }


    public ProductBoxDao getProductBoxDao() {
        return productBoxDao;
    }

    public ProductBox getProductBoxByID(int productBoxID) {
        return productBoxDao.getProductBoxByID(productBoxID);
    }

    public LiveData<List<ProductBox>> getAllProductBox() {
        return productBoxDao.getProductBoxList();
    }

    public List<Integer> checkIfAllProductBoxesSynchronized(List<Integer> idSet) {
        return productBoxDao.checkIfAllProductBoxesSynchronized(idSet);
    }
}
