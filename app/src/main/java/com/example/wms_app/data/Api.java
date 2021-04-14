package com.example.wms_app.data;

import com.example.wms_app.model.ApplicationVersion;
import com.example.wms_app.model.Employee;
import com.example.wms_app.model.GenericResponse;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.model.IncomingForServerWrapper;
import com.example.wms_app.model.IncomingProductionType;
import com.example.wms_app.model.IncomingType;
import com.example.wms_app.model.InventoryWrapper;
import com.example.wms_app.model.LoginModel;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.model.OutgoingForServerWrapper;
import com.example.wms_app.model.Partner;
import com.example.wms_app.model.PostRequest;
import com.example.wms_app.model.Product;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.model.ProductCategory;
import com.example.wms_app.model.ProductItemType;
import com.example.wms_app.model.ReturnReason;
import com.example.wms_app.model.Truck;
import com.example.wms_app.model.Warehouse;
import com.example.wms_app.model.WarehouseObject;
import com.example.wms_app.model.WarehousePosition;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {

    @POST("LoginUser")
    Call<GenericResponse<Employee>> loginUser(@Body LoginModel loginModel);

    @GET("GetApplicationVersion")
    Call<GenericResponse<ApplicationVersion>> getApplicationVersion();

    @POST("GetProductCategoryByModDate")
    Call<GenericResponse<List<ProductCategory>>> getProductCategoryByModDate(@Body PostRequest postRequest);

    @POST("GetProductByModDate")
    Call<GenericResponse<List<Product>>> getProductByModDate(@Body PostRequest postRequest);

    @POST("GetPartnerByModDate")
    Call<GenericResponse<List<Partner>>> getPartnerByModDate(@Body PostRequest postRequest);

    @POST("GetWarehouseByModDate")
    Call<GenericResponse<List<Warehouse>>> getWarehouseByModDate(@Body PostRequest postRequest);

    @POST("GetProductItemTypeByModDate")
    Call<GenericResponse<List<ProductItemType>>> getProductItemTypeByModDate(@Body PostRequest postRequest);

    @POST("GetWarehousePosByModDate")
    Call<GenericResponse<List<WarehousePosition>>> getWarehousePosByModDate(@Body PostRequest postRequest);

    @POST("GetWarehouseObjByModDate")
    Call<GenericResponse<List<WarehouseObject>>> getWarehouseObjByModDate(@Body PostRequest postRequest);

    @POST("GetTruckByModDate")
    Call<GenericResponse<List<Truck>>> getTruckByModDate(@Body PostRequest postRequest);

    @POST("GetProductBoxByModDate")
    Call<GenericResponse<List<ProductBox>>> getProductBoxByModDate(@Body PostRequest postRequest);

    @POST("GetReturnReasonByModDate")
    Call<GenericResponse<List<ReturnReason>>> getReturnReasonByModDate(@Body PostRequest postRequest);

    @POST("GetIncomingFirebase")
    Call<List<Incoming>> getIncomingFirebase(@Body PostRequest postRequest);

    @POST("GetIncomingTypeByModDate")
    Call<GenericResponse<List<IncomingType>>> getIncomingTypeByModDate(@Body PostRequest postRequest);

    @POST("GetIncomingProductionTypeByModDate")
    Call<GenericResponse<List<IncomingProductionType>>> getIncomingProductionTypeByModDate(@Body PostRequest postRequest);

    @POST("SendIncomingDetailsResultToServer")
    Call<GenericResponse<String>> sendIncomingDetailsResultToServer(@Body IncomingForServerWrapper incomingForServerWrapper);

    @POST("SendAllIncomingDetailsResultToServer")
    Call<GenericResponse<String>> sendAllIncomingDetailsResultToServer(@Body List<IncomingForServerWrapper> incomingForServerWrapperList);

    @POST("GetOutgoingFirebase")
    Call<List<Outgoing>> getOutgoingFirebase(@Body PostRequest postRequest);

    @POST("UpdatePdaArrivedOutgoing")
    Call<String> updatePdaArriveOutgoing(@Body PostRequest.PostRequestDate postRequest);

    @POST("SendOutgoingDetailsResultToServer")
    Call<GenericResponse<String>> sendOutgoingDetailsResultToServer(@Body OutgoingForServerWrapper outgoingForServerWrapper);

    @POST("SendInventoryResultToServer")
    Call<String> sendInventoryResultToServer(@Body InventoryWrapper inventoryWrapper);

    @POST("SendAllOutgoingToServer")
    Call<GenericResponse<String>> sendAllOutgoingDetailsResultToServer(@Body List<OutgoingForServerWrapper> outgoingForServerWrapperList);
}
