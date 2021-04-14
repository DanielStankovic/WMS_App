package com.example.wms_app.repository.inventory;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.example.wms_app.R;
import com.example.wms_app.data.Api;
import com.example.wms_app.data.ApiClient;
import com.example.wms_app.model.Inventory;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.InventoryWrapper;
import com.example.wms_app.utilities.ApiResponse;
import com.example.wms_app.utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryActivityRepository {
    private MutableLiveData<ApiResponse> responseMutableLiveData; //Objekat koji sluzi za hendlovanje responsa sa servera i ispisvanje greski u dijalogu
    private MutableLiveData<String> inventoryIDLiveData;

    private final FirebaseFirestore firebaseFirestore;
   // private final Api apiReference; //Retrofit objekat za komunikaciju sa serverom
    private final Resources resources; //resursi za stringove
    private MutableLiveData<List<InventoryDetailsResult>> mInventoryDetailsResultMutLiveData; //Lista u koju se dodaju skenirani/dodati artikli




    public InventoryActivityRepository(Context context) {
        resources = context.getResources();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //  apiReference = ApiClient.getApiClient().create(Api.class);
    }

    public MutableLiveData<ApiResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null)
            responseMutableLiveData = new MutableLiveData<>();

        return responseMutableLiveData;
    }

    public MutableLiveData<List<InventoryDetailsResult>> getInventoryDetailsResultMutLiveData() {
        if (mInventoryDetailsResultMutLiveData == null) {
            mInventoryDetailsResultMutLiveData = new MutableLiveData<>();
            List<InventoryDetailsResult> list = new ArrayList<>();
            mInventoryDetailsResultMutLiveData.setValue(list);
        }
        return mInventoryDetailsResultMutLiveData;
    }

    public MutableLiveData<String> getInventoryIDLiveData(){
        if(inventoryIDLiveData == null)
            inventoryIDLiveData = new MutableLiveData<>();
        return inventoryIDLiveData;
    }

    public void checkInventoryOpen(int employeeID) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        firebaseFirestore.collection("inventory").whereEqualTo("employeeID", employeeID).whereEqualTo("finished", false).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0) {
                    String inventoryID = queryDocumentSnapshots.getDocuments().get(0).getId();
                    firebaseFirestore.collection("inventory").document(inventoryID).collection("InventoryDetailsResult").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<InventoryDetailsResult> inventoryDetailsResultList = queryDocumentSnapshots.toObjects(InventoryDetailsResult.class);
                            getInventoryIDLiveData().setValue(inventoryID);
                            getInventoryDetailsResultMutLiveData().setValue(inventoryDetailsResultList);
                           getResponseMutableLiveData().setValue(ApiResponse.successWithAction("existingInventory"));

                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));

                       }
                   });

               }else {
                    String inventoryID = firebaseFirestore.collection("inventory").document().getId();
                    Inventory newInventory = new Inventory(employeeID, false, new Date(), inventoryID, 2);
                   firebaseFirestore.collection("inventory").document(inventoryID).set(newInventory).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           getInventoryIDLiveData().setValue(inventoryID);
                           getResponseMutableLiveData().setValue(ApiResponse.successWithAction("newInventory"));
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
                       }
                   });
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));
            }
        });
    }

    //postavljanje rezultata popisa na firebase
    public void pushInventoryResult(List<InventoryDetailsResult> idrList, String inventoryID){
        getResponseMutableLiveData().setValue(ApiResponse.loading());
        WriteBatch batch = firebaseFirestore.batch();
        CollectionReference currentInventory = firebaseFirestore.collection("inventory").document(inventoryID).collection("InventoryDetailsResult");

        for (InventoryDetailsResult idr : idrList) {
            batch.set(currentInventory.document(), idr);
        }

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getResponseMutableLiveData().setValue(ApiResponse.successWithAction("successPush"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getResponseMutableLiveData().setValue(ApiResponse.error(e.getMessage()));

            }
        });
    }

    public void syncInventoryDetailsResult(String inventoryID){
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        CollectionReference currentInventory = firebaseFirestore.collection("inventory").document(inventoryID).collection("InventoryDetailsResult");
        Query query = currentInventory.orderBy("createDate", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    getResponseMutableLiveData().setValue(ApiResponse.error(error.getMessage()));
                }else{
                    getInventoryDetailsResultMutLiveData().setValue(value.toObjects(InventoryDetailsResult.class));
                    getResponseMutableLiveData().setValue(ApiResponse.success());
                }
            }
        });
    }


    public void deleteProductFromPosition(InventoryDetailsResult idr){
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        CollectionReference idrRef = firebaseFirestore.collection("inventory").document(idr.getInventoryID())
                .collection("InventoryDetailsResult");

        Query deleteIDR = idrRef.whereEqualTo("createDate", idr.getCreateDate());

            deleteIDR.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            idrRef.document(document.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    getResponseMutableLiveData().setValue(ApiResponse.successWithAction("success_delete"));
                                }
                            });
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    getResponseMutableLiveData().setValue(ApiResponse.error(e.toString()));
                }
            });
    }

    public void sendInventoryToServer(String inventoryID) {
        getResponseMutableLiveData().setValue(ApiResponse.loading());

        InventoryWrapper inventoryWrapper = new InventoryWrapper();
        /////////////////////// SLANJE REZULTATA POPISA ////////////////
        CollectionReference allIncomingResult = firebaseFirestore.collection("inventory").document(inventoryID).collection("InventoryDetailsResult");
        Query queryIncoming = allIncomingResult.whereEqualTo("sent", false);
        queryIncoming.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty()) {
                                    sendAndUpdateInventoryDetailsResult(querySnapshot, inventoryWrapper);
                                } else {
                                    // svi rezultati prijema su vec poslati
                                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_succ_sending_no_product)));

                                }
                            } else {
                                //doslo do greske nije dohvatio rezultate prijema
                                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_succ_sending_inc_failed)));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //doslo do greske nije uopste uspostavio vezu sa firebase
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_failed_sending_failed)));
                        }
                    });

        /////////////////////// SLANJE HEADERA POPISA ////////////////
        firebaseFirestore.collection("inventory").document(inventoryID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Inventory inventory = task.getResult().toObject(Inventory.class);
                            sendAndUpdateInventoryHeader(inventory, inventoryWrapper);
                        }else{
                            getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_succ_sending_inc_failed)));

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //doslo do greske nije uopste uspostavio vezu sa firebase
                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_failed_sending_failed)));
            }
        });

    }

    private void sendAndUpdateInventoryDetailsResult (QuerySnapshot querySnapshot, InventoryWrapper inventoryWrapper) {
        inventoryWrapper.setInventoryDetailsResults(setInventoryStringDate(querySnapshot));
        sendCompleteInventory(inventoryWrapper);
    }

    private void sendAndUpdateInventoryHeader (Inventory inventory, InventoryWrapper inventoryWrapper) {
        inventoryWrapper.setInventory(inventory);
        sendCompleteInventory(inventoryWrapper);
    }


    private void sendCompleteInventory(InventoryWrapper inventoryWrapper) {
        if(inventoryWrapper.getInventory() != null && inventoryWrapper.getInventoryDetailsResults().size() > 0){
            Call<String> call = ApiClient.getApiClient().create(Api.class).sendInventoryResultToServer(inventoryWrapper);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.body().equals("OK")) {

                        //UPDATE INVENTORY DETAILS SEND
                        firebaseFirestore.collection("inventory").document(inventoryWrapper.getInventory().getInventoryID()).collection("InventoryDetailsResult").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //lista svih IDeva dokumenata u IDR
                                    List<String> listIdrIDs = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        listIdrIDs.add(document.getId());
                                    }
                                    updateSent(listIdrIDs, inventoryWrapper.getInventory().getInventoryID(), "inventory", "InventoryDetailsResult");
                                } else {
                                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_details_check_failed)));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_details_check_failed)));

                            }
                        });
                    } else {
                        getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.incoming_sending_failed)));
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Utility.writeErrorToFile(new Exception(t.getMessage()));
                    //nije uopste uspostavio vezu sa serverom
                    getResponseMutableLiveData().setValue(ApiResponse.error(resources.getString(R.string.connection_failed_sending_failed)));
                }
            });
        }
    }

    private List<InventoryDetailsResult> setInventoryStringDate(QuerySnapshot querySnapshot) {
        List<InventoryDetailsResult> incList = new ArrayList<InventoryDetailsResult>();
        incList.addAll(querySnapshot.toObjects(InventoryDetailsResult.class));

        return incList;
    }




    private void updateSent(List<String> listIdrIDs, String inventoryID, String collection, String subCollection) {
        WriteBatch batch = firebaseFirestore.batch();
        for (int k = 0; k < listIdrIDs.size(); k++) {
            DocumentReference documentReference = firebaseFirestore.collection(collection).document(inventoryID).collection(subCollection).document(listIdrIDs.get(k));
            batch.update(documentReference, "sent", true);
        }
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //OBAVESTENJE DA JE SVE ZAVRSENO
                setSentHeader(inventoryID, collection);
              //  getResponseMutableLiveData().postValue(ApiResponse.success());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //desila se greska SENT nije updatovan
                getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.sent_data_not_updated)));
            }
        });
    }

    public void setSentHeader(String id, String collection){
        firebaseFirestore.collection(collection).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Inventory incoming = task.getResult().toObject(Inventory.class);
                firebaseFirestore.collection(collection).document(id).update("finished", true, "inventoryStatusID", 4).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        getResponseMutableLiveData().setValue(ApiResponse.successWithAction("success_send"));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getResponseMutableLiveData().postValue(ApiResponse.error(resources.getString(R.string.sent_header_not_updated)));
                    }
                });

            }

        });
    }
}
