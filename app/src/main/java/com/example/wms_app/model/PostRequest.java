package com.example.wms_app.model;

import com.google.gson.annotations.SerializedName;

public class PostRequest {
    @SerializedName("ModifiedDate")
    private String modifiedDate;
    @SerializedName("EmployeeID")
    private int employeeID;



    public PostRequest(String modifiedDate, int employeeID) {
        this.modifiedDate = modifiedDate;
        this.employeeID = employeeID;

    }


    public static class PostRequestDate {

        @SerializedName("ModifiedDate")
        private String modifiedDate;
        @SerializedName("ArrivedDate")
        private String arrivedDate;


        public PostRequestDate(String modifiedDate, String arrivedDate) {
            this.modifiedDate = modifiedDate;
            this.arrivedDate = arrivedDate;
        }
    }
}
