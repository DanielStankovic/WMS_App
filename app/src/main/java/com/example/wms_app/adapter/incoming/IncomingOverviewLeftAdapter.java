package com.example.wms_app.adapter.incoming;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemIncomingOverviewLeftBinding;
import com.example.wms_app.model.ProductBox;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IncomingOverviewLeftAdapter extends RecyclerView.Adapter<IncomingOverviewLeftAdapter.ViewHolder> {

    private List<ProductBox> productBoxList;
    private Resources resources;

    public IncomingOverviewLeftAdapter(Context context) {
        resources = context.getResources();
    }

    public void setProductBoxList(List<ProductBox> productBoxList) {
        this.productBoxList = productBoxList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemIncomingOverviewLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductBox productBox = productBoxList.get(position);
        holder.mBinding.incomingOverviewLeftProdNameTv.setText(productBox.getProductBoxName());
        holder.mBinding.incomingOverviewLeftProdQtyTv.setText(resources.getString(R.string.added_and_expected_qty, productBox.getAddedQuantity(), productBox.getExpectedQuantity()));

        if (productBox.getColorStatus() == 1) {
            holder.mBinding.incomingOverViewLeftCv.setCardBackgroundColor(Color.parseColor("#ebb734"));
            holder.mBinding.incomingOverviewLeftProdNameTv.setTextColor(Color.BLACK);
            holder.mBinding.incomingOverviewLeftProdQtyTv.setTextColor(Color.BLACK);
        } else if (productBox.getColorStatus() == 2) {
            holder.mBinding.incomingOverViewLeftCv.setCardBackgroundColor(Color.parseColor("#088A24"));
            holder.mBinding.incomingOverviewLeftProdNameTv.setTextColor(Color.WHITE);
            holder.mBinding.incomingOverviewLeftProdQtyTv.setTextColor(Color.WHITE);
        } else {
            holder.mBinding.incomingOverViewLeftCv.setCardBackgroundColor(Color.parseColor("#ffffff"));
            holder.mBinding.incomingOverviewLeftProdNameTv.setTextColor(Color.BLACK);
            holder.mBinding.incomingOverviewLeftProdQtyTv.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return productBoxList == null ? 0 : productBoxList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemIncomingOverviewLeftBinding mBinding;

        public ViewHolder(@NonNull ItemIncomingOverviewLeftBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
