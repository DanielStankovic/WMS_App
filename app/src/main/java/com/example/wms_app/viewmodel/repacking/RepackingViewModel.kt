package com.example.wms_app.viewmodel.repacking

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.example.wms_app.R
import com.example.wms_app.model.*
import com.example.wms_app.repository.data.ProductBoxRepository
import com.example.wms_app.repository.data.WarehousePositionRepository
import com.example.wms_app.repository.repacking.RepackingRepository
import com.example.wms_app.utilities.ApiResponseEvent
import com.example.wms_app.utilities.extensions.default
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class RepackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repackingRepository = RepackingRepository(FirebaseFirestore.getInstance())
    private val warehousePositionRepository = WarehousePositionRepository(application.applicationContext)
    private val productBoxRepository = ProductBoxRepository(application.applicationContext)
    private val resources = application.resources

    private val _tempList = MutableLiveData<MutableList<OutgoingDetailsResult>>().default(mutableListOf())
    val tempList: LiveData<MutableList<OutgoingDetailsResult>> get() = _tempList

    private val _warehouseStatusPositionDetails = MutableLiveData<MutableList<WarehouseStatusPositionDetails>>().default(mutableListOf())
    val productBoxListForSpinner: LiveData<List<ProductBox>> = _warehouseStatusPositionDetails.switchMap { mutableList ->
        liveData {
//            if(mutableList != null)
//            val productBoxList: List<ProductBox> = mapProductBoxFromWspDetails(mutableList)
        }
    }

    private fun mapProductBoxFromWspDetails(mutableList: MutableList<WarehouseStatusPositionDetails>?): List<ProductBox> {

        val productBoxList: List<ProductBox> = mutableList!!.mapNotNull { warehouseStatusPositionDetails -> getProductBoxDataFromDb(warehouseStatusPositionDetails) }
        return productBoxList

    }

    private fun getProductBoxDataFromDb(warehouseStatusPositionDetails: WarehouseStatusPositionDetails): ProductBox? {
        val pb = productBoxRepository.getProductBoxByID(warehouseStatusPositionDetails.productBoxID)
        pb?.let {
            val prodBox = ProductBox()
        }
        return null
    }


    private val _apiResponseEvent = Channel<ApiResponseEvent>()
    val apiResponseEvent = _apiResponseEvent.receiveAsFlow()

    val isTempListEmpty = _tempList.map { value -> value.isNullOrEmpty() }

    fun codeScanned(barcode: String) {


    }

    fun positionFromSelected(fromBarcode: String) = viewModelScope.launch {
        val warehousePosition = warehousePositionRepository.getWarehousePositionByBarcode(fromBarcode)
        if (warehousePosition == null) {
            _apiResponseEvent.send(ApiResponseEvent.WarningEvent(resources.getString(R.string.position_not_exist)))
            //TODO resetProductSpinner
            return@launch
        }

        _apiResponseEvent.send(ApiResponseEvent.LoadingEvent)
        val result = repackingRepository.getProductBoxesOnPosition(fromBarcode)
        if (result.isError()) {
            _apiResponseEvent.send(ApiResponseEvent.ErrorEvent(result.message!!))
            return@launch
        }
        result.data?.let {
            val warehouseStatusPosition = it.toObjects(WarehouseStatusPosition::class.java).firstOrNull()


            if (warehouseStatusPosition == null) {
                _apiResponseEvent.send(ApiResponseEvent.WarningEvent(resources.getString(R.string.position_not_exist_on_firebase)))
                return@launch
            }
            if (warehouseStatusPosition.isLocked) {
                _apiResponseEvent.send(ApiResponseEvent.WarningEvent(resources.getString(R.string.pos_locked_error)))
                //TODO resetProductSpinner
                return@launch
            }
            Log.i("AAA11", Thread.currentThread().name)
            val test = warehouseStatusPosition.wspDetails.forEach { warehouseStatusPositionDetails ->
                run {
                    Log.i("AAA", "${Thread.currentThread()}")
                    Log.i("AAAAA", warehouseStatusPositionDetails.productBoxID.toString())
                }
            }
            _apiResponseEvent.send(ApiResponseEvent.SuccessWithActionEvent("PREUZETI PODACI"))
        }


    }


    fun positionToSelected(toBarcode: String) {


    }

    fun removeLastFromTempList() {
        val tempList = _tempList.value
        if (tempList != null && tempList.isNotEmpty()) {
            tempList.removeLast()
            _tempList.value = tempList
        }
    }

    fun removeAllFromTempList() {
        val tempList = _tempList.value
        if (tempList != null) {
            tempList.clear()
            _tempList.value = tempList
        }
    }

}