package com.example.wms_app.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemInventoryCurrentListBinding;
import com.example.wms_app.model.InventoryDetailsResult;
import com.example.wms_app.model.ProductBox;

import java.util.List;

public class InventoryCurrentListAdapter extends RecyclerView.Adapter<InventoryCurrentListAdapter.ViewHolder>  {

    private final LayoutInflater inflater;
    private Resources resources;
    private List<InventoryDetailsResult> idrLastList;
    private List<ProductBox> productForIncoming;
    private ItemInventoryCurrentListBinding binding;
    private Context context;
    private CurrentListListener mListener;



    public InventoryCurrentListAdapter(Context context, List<InventoryDetailsResult> inventoryDetailsResults, CurrentListListener listener) {
        inflater = LayoutInflater.from(context);
        resources = context.getResources();
        idrLastList = inventoryDetailsResults;
        mListener = listener;
    }
    public void setProductList(List<ProductBox> products){
        productForIncoming = products;
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
        InventoryDetailsResult idr = idrLastList.get(position);

        if(productForIncoming != null){
            ProductBox currentProduct = productForIncoming.stream().filter(x -> x.getProductBoxID() == idr.getProductBoxID()).findFirst().get();
            holder.tvProductCode.setText(resources.getString(R.string.product_code_lbl, currentProduct.getProductBoxCode()));
            holder.tvProductName.setText(currentProduct.getProductBoxName());
            holder.tvQuantity.setText(resources.getString(R.string.quantity_lbl, (int)idr.getQuantity()));
            holder.tvPosition.setText(resources.getString(R.string.position_lbl, idr.getWarehousePositionBarcode()));

            if(!idr.getSerialNo().equals("")){
                holder.tvSerial.setText(resources.getString(R.string.lot_lbl, idr.getSerialNo()));
            }else{
                holder.tvSerial.setText(resources.getString(R.string.lot_lbl,"/"));
            }

        }

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemDeleted(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return idrLastList != null ? idrLastList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductName, tvQuantity, tvSerial, tvProductCode, tvPosition;
        CardView cardView;
        ImageView deleteBtn;


        public ViewHolder(@NonNull ItemInventoryCurrentListBinding itemBinding) {
            super(itemBinding.getRoot());
            tvProductName = itemBinding.phaseOneAddedProdNameTv;
            tvQuantity = itemBinding.phaseOneAddedSrNumTv;
            tvSerial = itemBinding.phaseOneAddedQtyTv;
            tvProductCode = itemBinding.phaseOneAddedProdCodeTv;
            cardView = itemBinding.currentListCard;
            tvPosition = itemBinding.phaseOneAddedPositionTv;
            deleteBtn = itemBinding.phaseOneAddedDeleteBtn;

        }
    }
    public void setIdrLastList(List<InventoryDetailsResult> idr){
        idrLastList = idr;
        notifyDataSetChanged();
    }

    public interface CurrentListListener{
        void onItemDeleted(int index);
    }

}
