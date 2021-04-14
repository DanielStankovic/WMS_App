package com.example.wms_app.adapter.outgoing;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemOutgoingPhaseOnePreviewBinding;
import com.example.wms_app.model.OutgoingDetailsResultPreview;

import java.util.List;

public class PhaseOnePreviewOutgoingAdapter extends RecyclerView.Adapter<PhaseOnePreviewOutgoingAdapter.ViewHolder> {

    private List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList;
    private final Context mContext;
    private final Resources resources;

    public PhaseOnePreviewOutgoingAdapter(Context context) {
        this.mContext = context;
        this.resources = context.getResources();
    }

    public void setOutgoingDetailsResultPreviewList(List<OutgoingDetailsResultPreview> outgoingDetailsResultPreviewList) {
        this.outgoingDetailsResultPreviewList = outgoingDetailsResultPreviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingPhaseOnePreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*Ovde koristim objekat OutgingDetailsResultPreview kao i u TempListPreview Fragment. Dodao sam odredjena polja kako bi
         * mogao da prikazem sve od podataka sta mi treba.
         * quantity - addedQty odnosno kolicina koja je vec dodata u temp listi plus na poz za prelaodin
         * expectedQuantitiy - ocekivana kolicina na otpremi
         */
        OutgoingDetailsResultPreview outgoingDetailsResultPreview = outgoingDetailsResultPreviewList.get(position);

        holder.mBinding.phaseOnePreviewProdNameTv.setText(outgoingDetailsResultPreview.getProductBoxName());
        holder.mBinding.phaseOnePreviewProdCodeTv.setText(outgoingDetailsResultPreview.getProductBoxCode());
        holder.mBinding.phaseOnePreviewQtyTv.setText(resources.getString(R.string.added_and_expected_qty, outgoingDetailsResultPreview.getQuantity(), outgoingDetailsResultPreview.getExpectedQuantity()));
        int colorID = outgoingDetailsResultPreview.getColorStatus() == 2 ? R.color.dark_green : R.color.white;
        int textColorID = outgoingDetailsResultPreview.getColorStatus() == 2 ? R.color.white : R.color.black;
        holder.mBinding.phaseOnePreviewCv.setCardBackgroundColor(ContextCompat.getColor(mContext, colorID));
        holder.mBinding.phaseOnePreviewQtyTv.setTextColor(ContextCompat.getColor(mContext, textColorID));
        holder.mBinding.phaseOnePreviewProdCodeTv.setTextColor(ContextCompat.getColor(mContext, textColorID));
        holder.mBinding.phaseOnePreviewProdNameTv.setTextColor(ContextCompat.getColor(mContext, textColorID));
    }

    @Override
    public int getItemCount() {
        return outgoingDetailsResultPreviewList == null ? 0 : outgoingDetailsResultPreviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOutgoingPhaseOnePreviewBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingPhaseOnePreviewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
