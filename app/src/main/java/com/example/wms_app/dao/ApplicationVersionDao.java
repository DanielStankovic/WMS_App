package com.example.wms_app.dao;

import com.example.wms_app.model.ApplicationVersion;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface ApplicationVersionDao {

    @Insert
    void insert(ApplicationVersion applicationVersion);

    @Delete
    void delete(ApplicationVersion applicationVersion);


}
