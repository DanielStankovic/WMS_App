package com.example.wms_app.repository.data;

import android.content.Context;

import com.example.wms_app.dao.ProductDao;
import com.example.wms_app.data.RoomDb;
import com.example.wms_app.model.Product;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProductRepository {

    private LiveData<List<Product>> allProducts;
    private ProductDao productDao;

    public ProductRepository(Context context){
        RoomDb db = RoomDb.getDatabase(context);
        productDao = db.productDao();
        allProducts = productDao.getProductList();
    }

    public LiveData<List<Product>> getAllProducts() {
        if(allProducts == null)
            allProducts = new MutableLiveData<>();
        return allProducts;
    }


}
