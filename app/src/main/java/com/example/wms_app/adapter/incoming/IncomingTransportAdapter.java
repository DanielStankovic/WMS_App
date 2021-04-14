package com.example.wms_app.adapter.incoming;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wms_app.databinding.ItemTransportBinding;
import com.example.wms_app.model.IncomingTruckResult;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IncomingTransportAdapter extends RecyclerView.Adapter<IncomingTransportAdapter.ViewHolder> {


    private List<IncomingTruckResult> incomingTruckResultList;
    private IncomingTransportAdapterListener listener;

    public IncomingTransportAdapter(IncomingTransportAdapterListener listener) {
        this.listener = listener;
    }

    public void setIncomingTruckResultList(List<IncomingTruckResult> incomingTruckResultList) {
        this.incomingTruckResultList = incomingTruckResultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTransportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        IncomingTruckResult incomingTruckResult = incomingTruckResultList.get(position);
        holder.mBinding.driverNameTv.setText(incomingTruckResult.getTruckDriver());
        holder.mBinding.licencePlateTv.setText(incomingTruckResult.getLicencePlate());
        if (incomingTruckResult.isSent()) {
            holder.mBinding.deleteTransportIv.setVisibility(View.INVISIBLE);
        } else {
            holder.mBinding.deleteTransportIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return incomingTruckResultList == null ? 0 : incomingTruckResultList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransportBinding mBinding;

        public ViewHolder(@NonNull ItemTransportBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.deleteTransportIv.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteTruckClicked(incomingTruckResultList.get(getAdapterPosition()));
                }
            });
        }

    }

    public interface IncomingTransportAdapterListener {
        void onDeleteTruckClicked(IncomingTruckResult incomingTruckResult);
    }
}
