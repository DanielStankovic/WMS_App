package com.example.wms_app.utilities

import android.content.DialogInterface

sealed class ApiResponseEvent {
    object SuccessEvent : ApiResponseEvent()
    object LoadingEvent : ApiResponseEvent()
    data class SuccessWithActionEvent(val message: String) : ApiResponseEvent()
    data class WarningEvent(val message: String) : ApiResponseEvent()
    data class ErrorEvent(val message: String) : ApiResponseEvent()
    data class PromptEvent(val message: String, val yesListener: DialogInterface.OnClickListener) : ApiResponseEvent()
}
