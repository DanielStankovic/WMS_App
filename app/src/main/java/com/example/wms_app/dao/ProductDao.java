package com.example.wms_app.dao;

import com.example.wms_app.model.Product;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ProductDao {

    @Insert
    void insert(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT ModifiedDate FROM Product  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM Product ORDER BY ProductName")
    LiveData<List<Product>> getProductList();

    @Query("SELECT * FROM Product WHERE ProductID = :productID LIMIT 1")
    LiveData<Product> getProductByID(int productID);

}
