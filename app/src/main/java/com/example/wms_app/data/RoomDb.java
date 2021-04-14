package com.example.wms_app.data;

import android.content.Context;

import com.example.wms_app.dao.ApplicationVersionDao;
import com.example.wms_app.dao.EmployeeDao;
import com.example.wms_app.dao.IncomingProductionTypeDao;
import com.example.wms_app.dao.IncomingTypeDao;
import com.example.wms_app.dao.PartnerDao;
import com.example.wms_app.dao.ProductBoxDao;
import com.example.wms_app.dao.ProductCategoryDao;
import com.example.wms_app.dao.ProductDao;
import com.example.wms_app.dao.ProductItemTypeDao;
import com.example.wms_app.dao.ReturnReasonDao;
import com.example.wms_app.dao.TruckDao;
import com.example.wms_app.dao.WarehouseDao;
import com.example.wms_app.dao.WarehouseObjectDao;
import com.example.wms_app.dao.WarehousePositionDao;
import com.example.wms_app.model.ApplicationVersion;
import com.example.wms_app.model.Employee;
import com.example.wms_app.model.IncomingProductionType;
import com.example.wms_app.model.IncomingType;
import com.example.wms_app.model.Partner;
import com.example.wms_app.model.Product;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ProductCategory;
import com.example.wms_app.model.ProductItemType;
import com.example.wms_app.model.ReturnReason;
import com.example.wms_app.model.Truck;
import com.example.wms_app.model.Warehouse;
import com.example.wms_app.model.WarehouseObject;
import com.example.wms_app.model.WarehousePosition;
import com.example.wms_app.utilities.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Employee.class, ApplicationVersion.class, ProductCategory.class,
        Product.class, Partner.class, Warehouse.class, WarehousePosition.class, WarehouseObject.class,
        Truck.class, ProductBox.class, ReturnReason.class, IncomingType.class, IncomingProductionType.class, ProductItemType.class}, version = Constants.DB_VERSION, exportSchema = false)
public abstract class RoomDb extends RoomDatabase {

    public abstract EmployeeDao employeeDao();
    public abstract ApplicationVersionDao applicationVersionDao();
    public abstract ProductCategoryDao productCategoryDao();
    public abstract ProductDao productDao();
    public abstract PartnerDao partnerDao();
    public abstract WarehouseDao warehouseDao();
    public abstract WarehousePositionDao warehousePositionDao();
    public abstract WarehouseObjectDao warehouseObjectDao();
    public abstract TruckDao truckDao();
    public abstract ProductBoxDao productBoxDao();
    public abstract ReturnReasonDao returnReasonDao();
    public abstract IncomingTypeDao incomingTypeDao();
    public abstract IncomingProductionTypeDao incomingProductionTypeDao();
    public abstract ProductItemTypeDao productItemTypeDao();

    private static volatile RoomDb INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static RoomDb getDatabase(Context context) {
        if (INSTANCE == null) {
            try {
                synchronized (RoomDb.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                RoomDb.class, Constants.DB_NAME)
                                .fallbackToDestructiveMigration()
                                .allowMainThreadQueries()
                                .build();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }
}
