package com.example.wms_app.utilities

import com.example.wms_app.enums.Status

data class Response<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?, message: String? = null): Response<T> {
            return Response(Status.SUCCESS, data, message)
        }

        fun <T> error(message: String?, data: T? = null): Response<T> {
            return Response(Status.ERROR, data, message)
        }

    }

    fun isError() = this.status == Status.ERROR
    fun isSuccessful() = this.status == Status.SUCCESS
}
