package com.example.wms_app.dao;

import com.example.wms_app.model.Employee;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface EmployeeDao {

    @Insert
    long insert(Employee employee);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Employee employee);

    @Delete
    void delete(Employee employee);

    @Query("SELECT ModifiedDate FROM Employee  ORDER BY ModifiedDate DESC LIMIT 1")
    String getModifiedDate();

    @Query("DELETE FROM Employee")
    void deleteAllEmployee();

    @Query("SELECT EmployeeID FROM Employee LIMIT 1")
    int getEmployeeID();
}
