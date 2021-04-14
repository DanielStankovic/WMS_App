package com.example.wms_app.adapter.inventory;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemIncomingOverviewDoneBinding;
import com.example.wms_app.databinding.ItemInventoryCurrentListBinding;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.viewmodel.inventory.InventoryViewModel;

import java.util.List;

public class InventoryDoneAdapter extends RecyclerView.Adapter<InventoryDoneAdapter.ViewHolder>  {

    private final LayoutInflater inflater;
    private Resources resources;
    private List<InventoryDetailsResult> idrList;
    private List<ProductBox> productBoxList;
    private ItemInventoryCurrentListBinding binding;
    private Context context;
    private InventoryViewModel inventoryViewModel;
    private InventoryDoneListener mListener;

    public InventoryDoneAdapter(Context context, InventoryDoneListener listener) {
        inflater = LayoutInflater.from(context);
        resources = context.getResources();
        this.mListener = listener;
        inventoryViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(InventoryViewModel.class);
    }

    public void setProductList(List<ProductBox> products){
        productBoxList = products;
        notifyDataSetChanged();
    }

    public void seIdrList(List<InventoryDetailsResult> idr){
        idrList = idr;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = ItemInventoryCurrentListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(idrList != null){
            InventoryDetailsResult inventoryDetailsResult = idrList.get(position);
            if(productBoxList != null){
                ProductBox currentProduct = productBoxList.stream().filter(x -> x.getProductBoxID() == inventoryDetailsResult.getProductBoxID()).findFirst().get();
                holder.tvProductName.setText(currentProduct.getProductBoxName());
                holder.tvProductCode.setText("ŠIFRA: " + currentProduct.getProductBoxCode());
                holder.tvQuantity.setText(resources.getString(R.string.quantity_lbl, (int)inventoryDetailsResult.getQuantity()));

                if(!inventoryDetailsResult.getSerialNo().equals("")){
                    holder.tvSerialNo.setText(resources.getString(R.string.lot_lbl, inventoryDetailsResult.getSerialNo()));
                }else{
                    holder.tvSerialNo.setText(resources.getString(R.string.lot_lbl,"/"));
                }

                holder.tvLocation.setText(resources.getString(R.string.position_lbl, inventoryDetailsResult.getWarehousePositionBarcode()));

                if(inventoryDetailsResult.isSent()){
                    holder.btnDelete.setVisibility(View.GONE);
                    holder.imgSent.setVisibility(View.VISIBLE);
                }else {
                    holder.btnDelete.setVisibility(View.VISIBLE);
                    holder.imgSent.setVisibility(View.GONE);
                }

                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogBuilder.showDialogWithYesNoCallback(context, resources.getString(R.string.warning), resources.getString(R.string.remove_product_from_pos_prompt), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onItemDeleted(inventoryDetailsResult);
                                dialog.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                    }
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        if(idrList != null)
            return idrList.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductName, tvSerialNo, tvQuantity, tvLocation, tvProductCode;
        CardView cardView;
        ImageView btnDelete, imgSent;

        public ViewHolder(@NonNull ItemInventoryCurrentListBinding itemBinding) {
            super(itemBinding.getRoot());

            tvProductName = itemBinding.phaseOneAddedProdNameTv;
            tvSerialNo = itemBinding.phaseOneAddedSrNumTv;
            tvQuantity = itemBinding.phaseOneAddedQtyTv;
            btnDelete = itemBinding.phaseOneAddedDeleteBtn;
            tvLocation = itemBinding.phaseOneAddedPositionTv;
            imgSent = itemBinding.imageSentToServer;
            tvProductCode = itemBinding.phaseOneAddedProdCodeTv;
        }
    }

    public interface InventoryDoneListener{
        void onItemDeleted(InventoryDetailsResult idr);
    }
}
