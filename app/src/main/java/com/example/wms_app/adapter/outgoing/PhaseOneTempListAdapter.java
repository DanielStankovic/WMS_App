package com.example.wms_app.adapter.outgoing;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemOutgoingPhaseOneTempListBinding;
import com.example.wms_app.model.OutgoingDetailsResultPreview;

import java.util.List;

public class PhaseOneTempListAdapter extends RecyclerView.Adapter<PhaseOneTempListAdapter.ViewHolder> {

    private List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList;
    private final Resources resources;
    private final PhaseOneTempListAdapterListener listener;

    public PhaseOneTempListAdapter(Context context, PhaseOneTempListAdapterListener listener) {
        resources = context.getResources();
        this.listener = listener;
    }

    public void setOutgoingDetailsResultPreviewList(List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList) {
        this.outgoingDetailsResultPreviewList = outgoingDetailsResultPreviewList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingPhaseOneTempListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutgoingDetailsResultPreview outgoingDetailsResultPreview = outgoingDetailsResultPreviewList.get(position);

        holder.mBinding.phaseOneTempListProdNameTv.setText(outgoingDetailsResultPreview.getProductBoxCodeAndName());
        holder.mBinding.phaseOneTempListSrNumTv.setText(resources.getString(R.string.sr_number, outgoingDetailsResultPreview.getSerialNumber()));
        if (outgoingDetailsResultPreview.getSerialNumber().isEmpty()) {
            holder.mBinding.phaseOneTempListSrNumTv.setVisibility(View.GONE);
        } else {
            holder.mBinding.phaseOneTempListSrNumTv.setVisibility(View.VISIBLE);
        }

        holder.mBinding.phaseOneTempListQtyTv.setText(resources.getString(R.string.quantity_res, outgoingDetailsResultPreview.getQuantity(), ""));
        holder.mBinding.phaseOneTempListPosTv.setText(resources.getString(R.string.position, outgoingDetailsResultPreview.getPositionBarcode()));
    }

    @Override
    public int getItemCount() {
        return outgoingDetailsResultPreviewList == null ? 0 : outgoingDetailsResultPreviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOutgoingPhaseOneTempListBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingPhaseOneTempListBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.phaseOneTempListIv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteProductBoxClicked(getAdapterPosition());
                }
            });
        }
    }

    public interface PhaseOneTempListAdapterListener {
        void onDeleteProductBoxClicked(int position);
    }
}
