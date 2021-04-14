package com.example.wms_app.adapter.outgoing;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemOutgoingPhaseOnePreloadingBinding;
import com.example.wms_app.model.OutgoingDetailsResultPreview;

import java.util.List;

public class PhaseOnePreloadingAdapter extends RecyclerView.Adapter<PhaseOnePreloadingAdapter.ViewHolder> {

    private List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList;
    private final Resources resources;

    public PhaseOnePreloadingAdapter(Context context) {
        this.resources = context.getResources();
    }

    public void setOutgoingDetailsResultPreviewList(List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList) {
        this.outgoingDetailsResultPreviewList = outgoingDetailsResultPreviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingPhaseOnePreloadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OutgoingDetailsResultPreview outgoingDetailsResultPreview = outgoingDetailsResultPreviewList.get(position);
        holder.mBinding.phaseOneAddedProdNameTv.setText(outgoingDetailsResultPreview.getProductBoxName());
        holder.mBinding.phaseOneAddedProdCodeTv.setText(outgoingDetailsResultPreview.getProductBoxCode());
        holder.mBinding.phaseOneAddedSrNumTv.setText(outgoingDetailsResultPreview.getSerialNumber() == null ? "" : outgoingDetailsResultPreview.getSerialNumber());
        holder.mBinding.phaseOneAddedQtyTv.setText(resources.getString(R.string.quantity_lbl, outgoingDetailsResultPreview.getQuantity()));
        holder.mBinding.phaseOneAddedPositionTv.setText(resources.getString(R.string.position_lbl, outgoingDetailsResultPreview.getPositionBarcode()));
    }

    @Override
    public int getItemCount() {
        return outgoingDetailsResultPreviewList == null ? 0 : outgoingDetailsResultPreviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemOutgoingPhaseOnePreloadingBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingPhaseOnePreloadingBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
