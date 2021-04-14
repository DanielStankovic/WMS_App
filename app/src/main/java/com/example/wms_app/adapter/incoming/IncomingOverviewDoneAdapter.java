package com.example.wms_app.adapter.incoming;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemIncomingOverviewDoneBinding;
import com.example.wms_app.model.IncomingDetailsResultLocal;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IncomingOverviewDoneAdapter extends RecyclerView.Adapter<IncomingOverviewDoneAdapter.ViewHolder> {

    private List<IncomingDetailsResultLocal> incomingDetailsResultLocalList;
    private final Resources resources;
    private final IncomingOverviewDoneAdapterListener listener;

    public IncomingOverviewDoneAdapter(Context context, IncomingOverviewDoneAdapterListener listener) {
        resources = context.getResources();
        this.listener = listener;
    }

    public void setProductBoxList(List<IncomingDetailsResultLocal> incomingDetailsResultLocalList) {
        this.incomingDetailsResultLocalList = incomingDetailsResultLocalList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemIncomingOverviewDoneBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncomingDetailsResultLocal incomingDetailsResultLocal = incomingDetailsResultLocalList.get(position);
        String resLbl;
        holder.mBinding.incomingOverviewProdNameTv.setText(incomingDetailsResultLocal.getProductBoxNameAndCode());
        holder.mBinding.incomingOverviewDonePosTv.setText(resources.getString(R.string.position, incomingDetailsResultLocal.getPositionBarcode()));

        holder.mBinding.incomingOverviewDoneSrNumTv.setText(resources.getString(R.string.sr_number, incomingDetailsResultLocal.getSerialNumber()));
        if (incomingDetailsResultLocal.getSerialNumber().isEmpty()) {
            holder.mBinding.incomingOverviewDoneSrNumTv.setVisibility(View.GONE);
        } else {
            holder.mBinding.incomingOverviewDoneSrNumTv.setVisibility(View.VISIBLE);
        }

        if (incomingDetailsResultLocal.isSent()) {
            holder.mBinding.incomingOverviewDoneIv.setVisibility(View.INVISIBLE);
        } else {
            holder.mBinding.incomingOverviewDoneIv.setVisibility(View.VISIBLE);
        }

        if (incomingDetailsResultLocal.isReserved()) {
            holder.mBinding.incomingOverviewDoneCv.setCardBackgroundColor(Color.parseColor("#ffeb3b"));
            resLbl = resources.getString(R.string.res_lbl);
        } else {
            holder.mBinding.incomingOverviewDoneCv.setCardBackgroundColor(Color.parseColor("#ffffff"));
            resLbl = "";
        }

        holder.mBinding.incomingOverviewDoneQtyTv.setText(resources.getString(R.string.quantity_res, incomingDetailsResultLocal.getQuantity(), resLbl));

    }

    @Override
    public int getItemCount() {
        return incomingDetailsResultLocalList == null ? 0 : incomingDetailsResultLocalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemIncomingOverviewDoneBinding mBinding;

        public ViewHolder(@NonNull ItemIncomingOverviewDoneBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.incomingOverviewDoneIv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteProductClicked(incomingDetailsResultLocalList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface IncomingOverviewDoneAdapterListener {
        void onDeleteProductClicked(IncomingDetailsResultLocal incomingDetailsResultLocal);
    }
}
