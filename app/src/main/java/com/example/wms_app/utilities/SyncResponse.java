package com.example.wms_app.utilities;

import com.example.wms_app.enums.SyncStatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SyncResponse {

    public final SyncStatus status;
    @Nullable
    public final String error;

    public SyncResponse(SyncStatus status, @Nullable String error) {
        this.status = status;
        this.error = error;
    }

    public static SyncResponse started(){
        return new SyncResponse(SyncStatus.SYNC_START,null);
    }

    public static SyncResponse increase(){
        return new SyncResponse(SyncStatus.SYNC_INCREASE,null);
    }

    public static SyncResponse error(@NonNull String error){
        return new SyncResponse(SyncStatus.SYNC_ERROR,error);
    }

    public static SyncResponse finished(){
        return new SyncResponse(SyncStatus.SYNC_FINISHED,null);
    }
}
