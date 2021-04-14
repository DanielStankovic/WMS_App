package com.example.wms_app.adapter.outgoing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.databinding.ItemTransportBinding;
import com.example.wms_app.model.OutgoingTruckResult;

import java.util.List;

public class OutgoingTransportAdapter extends RecyclerView.Adapter<OutgoingTransportAdapter.ViewHolder> {

    private List<OutgoingTruckResult> outgoingTruckResultList;
    private final OutgoingTransportAdapterListener listener;

    public OutgoingTransportAdapter(OutgoingTransportAdapterListener listener) {
        this.listener = listener;
    }

    public void setOutgoingTruckResultList(List<OutgoingTruckResult> outgoingTruckResultList) {
        this.outgoingTruckResultList = outgoingTruckResultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTransportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutgoingTruckResult outgoingTruckResult = outgoingTruckResultList.get(position);
        holder.mBinding.driverNameTv.setText(outgoingTruckResult.getTruckDriver());
        holder.mBinding.licencePlateTv.setText(outgoingTruckResult.getLicencePlate());
        if (outgoingTruckResult.isSent()) {
            holder.mBinding.deleteTransportIv.setVisibility(View.INVISIBLE);
        } else {
            holder.mBinding.deleteTransportIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return outgoingTruckResultList == null ? 0 : outgoingTruckResultList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransportBinding mBinding;

        public ViewHolder(@NonNull ItemTransportBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.deleteTransportIv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteTruckClicked(outgoingTruckResultList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OutgoingTransportAdapterListener {
        void onDeleteTruckClicked(OutgoingTruckResult outgoingTruckResult);
    }
}
