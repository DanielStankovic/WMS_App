package com.example.wms_app.utilities;

import android.content.DialogInterface;

import com.example.wms_app.enums.Status;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ApiResponse {

    public final Status status;

    public DialogInterface.OnClickListener yesListener;



    @Nullable
    public final String error;

    private ApiResponse(Status status ,@Nullable String error) {
        this.status = status;
        this.error = error;
    }

    private ApiResponse(Status status, @Nullable String error, DialogInterface.OnClickListener yesListener) {
        this.status = status;
        this.error = error;
        this.yesListener = yesListener;
    }

    public static ApiResponse loading() {
        return new ApiResponse(Status.LOADING, null);
    }

    public static ApiResponse success() {
        return new ApiResponse(Status.SUCCESS, null);
    }

    public static ApiResponse successWithAction(@NonNull String error) {
        return new ApiResponse(Status.SUCCESS_WITH_ACTION, error);
    }

    public static ApiResponse successWithExitAction(@NonNull String error) {
        return new ApiResponse(Status.SUCCESS_WITH_EXIT_ACTION, error);
    }

    public static ApiResponse error(@NonNull String error) {
        Utility.writeErrorToFile(new Exception(error));
        return new ApiResponse(Status.ERROR, error);
    }

    public static ApiResponse errorWithAction(@NonNull String error) {
        Utility.writeErrorToFile(new Exception(error));
        return new ApiResponse(Status.ERROR_WITH_ACTION, error);
    }

    public static ApiResponse idle() {
        return new ApiResponse(Status.IDLE, null);
    }

    public static ApiResponse prompt(String error, DialogInterface.OnClickListener yesListener) {
        Utility.writeErrorToFile(new Exception(error));
        return new ApiResponse(Status.PROMPT, error, yesListener);
    }
}
