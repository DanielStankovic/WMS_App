package com.example.wms_app.dao;

import com.example.wms_app.model.ProductBox;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ProductBoxDao {

    @Insert
    void insert(ProductBox productBox);

    @Delete
    void delete(ProductBox productBox);

    @Query("SELECT ModifiedDate FROM ProductBox  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("SELECT * FROM ProductBox ORDER BY ProductBoxName")
    LiveData<List<ProductBox>> getProductBoxList();

    @Query("SELECT * FROM ProductBox WHERE ProductBoxID = :productBoxID LIMIT 1")
    ProductBox getProductBoxByID(int productBoxID);

    @Query("SELECT * FROM ProductBox WHERE ProductBoxID in (:idSet) ORDER BY ProductBoxName")
    List<ProductBox> getProductBoxesByID(List<Integer> idSet);

    @Query("SELECT ProductItemTypeID FROM ProductBox WHERE ProductBoxID in (:idSet)")
    List<Integer> getProductItemTypeByID(List<Integer> idSet);

    @Query("SELECT ProductBoxID FROM ProductBox WHERE ProductItemTypeID in (:idSet) ORDER BY ProductBoxName")
    List<Integer> getProductBoxesByProductItemTypeID(List<Integer> idSet);

    @Query("SELECT ProductBoxID FROM ProductBox WHERE ProductBoxID in (:idSet)")
    List<Integer> checkIfAllProductBoxesSynchronized(List<Integer> idSet);
}
