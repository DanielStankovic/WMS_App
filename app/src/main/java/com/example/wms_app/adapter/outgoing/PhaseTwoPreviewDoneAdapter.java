package com.example.wms_app.adapter.outgoing;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemOutgoingOverviewDoneBinding;
import com.example.wms_app.model.OutgoingDetailsResultPreview;

import java.util.List;

public class PhaseTwoPreviewDoneAdapter extends RecyclerView.Adapter<PhaseTwoPreviewDoneAdapter.ViewHolder> {

    private final Resources resources;
    private final PhaseTwoPreviewDoneAdapterListener listener;
    private List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList;


    public PhaseTwoPreviewDoneAdapter(Context context, PhaseTwoPreviewDoneAdapterListener listener) {
        this.resources = context.getResources();
        this.listener = listener;
    }

    public void setOutgoingDetailsResultPreviewList(List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList) {
        this.outgoingDetailsResultPreviewList = outgoingDetailsResultPreviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingOverviewDoneBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutgoingDetailsResultPreview outgoingDetailsResultPreview = outgoingDetailsResultPreviewList.get(position);

        holder.mBinding.outgoingPhaseTwoOverviewProdNameTv.setText(outgoingDetailsResultPreview.getProductBoxCodeAndName());
        holder.mBinding.outgoingPhaseTwoOverviewDonePosTv.setText(resources.getString(R.string.position, outgoingDetailsResultPreview.getPositionBarcode()));

        holder.mBinding.outgoingPhaseTwoOverviewDoneSrNumTv.setText(resources.getString(R.string.sr_number, outgoingDetailsResultPreview.getSerialNumber()));
        if (outgoingDetailsResultPreview.getSerialNumber().isEmpty()) {
            holder.mBinding.outgoingPhaseTwoOverviewDoneSrNumTv.setVisibility(View.GONE);
        } else {
            holder.mBinding.outgoingPhaseTwoOverviewDoneSrNumTv.setVisibility(View.VISIBLE);
        }

        if (outgoingDetailsResultPreview.isSent()) {
            holder.mBinding.outgoingPhaseTwoOverviewDoneIv.setVisibility(View.INVISIBLE);
        } else {
            holder.mBinding.outgoingPhaseTwoOverviewDoneIv.setVisibility(View.VISIBLE);
        }

        holder.mBinding.outgoingPhaseTwoOverviewDoneQtyTv.setText(resources.getString(R.string.quantity_res, outgoingDetailsResultPreview.getQuantity(), ""));
    }

    @Override
    public int getItemCount() {
        return outgoingDetailsResultPreviewList == null ? 0 : outgoingDetailsResultPreviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOutgoingOverviewDoneBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingOverviewDoneBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.outgoingPhaseTwoOverviewDoneIv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteProductClicked(outgoingDetailsResultPreviewList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface PhaseTwoPreviewDoneAdapterListener {
        void onDeleteProductClicked(OutgoingDetailsResultPreview outgoingDetailsResultPreview);
    }
}
