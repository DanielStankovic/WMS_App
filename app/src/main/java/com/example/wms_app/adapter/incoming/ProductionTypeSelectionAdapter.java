package com.example.wms_app.adapter.incoming;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.wms_app.databinding.ItemProductionTypeBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductionTypeSelectionAdapter extends RecyclerView.Adapter<ProductionTypeSelectionAdapter.ViewHolder> {

    private List<String> productionTypeCodeList;
    private ProductionTypeSelectionAdapterListener listener;

    public ProductionTypeSelectionAdapter(ProductionTypeSelectionAdapterListener listener) {
        this.listener = listener;
    }

    public void setProductionTypeCodeList(List<String> productionTypeCodeList) {
        this.productionTypeCodeList = productionTypeCodeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemProductionTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String productionTypeCode = productionTypeCodeList.get(position);
        holder.mBinding.productionTypeSelectionTv.setText(productionTypeCode);
    }

    @Override
    public int getItemCount() {
        return productionTypeCodeList == null ? 0 : productionTypeCodeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemProductionTypeBinding mBinding;

        public ViewHolder(@NonNull ItemProductionTypeBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.productionTypeSelectionCv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onProductTypeClicked(productionTypeCodeList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface ProductionTypeSelectionAdapterListener {
        void onProductTypeClicked(String productTypeCode);
    }
}
