package com.example.wms_app.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.wms_app.model.ProductItemType;

import java.util.List;

@Dao
public interface ProductItemTypeDao {

    @Insert
    void insert(ProductItemType productItemType);

    @Delete
    void delete(ProductItemType productItemType);

    @Query("SELECT ModifiedDate FROM ProductItemType  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM ProductItemType WHERE ProductItemTypeID in (:idSet) ORDER BY Name ")
    List<ProductItemType> getProductItemTypeList(List<Integer> idSet);

}
