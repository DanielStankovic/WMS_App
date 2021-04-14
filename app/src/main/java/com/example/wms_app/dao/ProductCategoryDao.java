package com.example.wms_app.dao;

import com.example.wms_app.model.ProductCategory;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ProductCategoryDao {

    @Insert
    void insert(ProductCategory truck);

    @Delete
    void delete(ProductCategory truck);

    @Query("SELECT ModifiedDate FROM ProductCategory  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM ProductCategory ORDER BY ProductCategoryName")
    List<ProductCategory> getProductCategoryList();
}
