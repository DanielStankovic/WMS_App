package com.example.wms_app.repository.repacking

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.wms_app.utilities.Response
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class RepackingRepository(private val firebaseFirestore: FirebaseFirestore) {

    suspend fun getProductBoxesOnPosition(fromBarcode: String): Response<QuerySnapshot> {
        return try {
            val wspReference = firebaseFirestore.collection("WarehouseStatusPos")
            val query = wspReference.whereEqualTo("warehousePositionBarcode", fromBarcode)
            val data = query.get().await()
            Response.success(data)
        } catch (e: Exception) {
            Response.error(e.message)
        }


    }
}