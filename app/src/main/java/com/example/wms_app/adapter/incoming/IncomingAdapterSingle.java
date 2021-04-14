package com.example.wms_app.adapter.incoming;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemIncomingBinding;
import com.example.wms_app.model.Incoming;
import com.example.wms_app.utilities.Utility;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class IncomingAdapterSingle extends RecyclerView.Adapter<IncomingAdapterSingle.ViewHolder> {
    private Resources resources;
    private Context ctx;
    private List<Incoming> incomingList;
    private SparseBooleanArray expandState = new SparseBooleanArray();
    private IncomingAdapterListener listener;


    public IncomingAdapterSingle(Context ctx, IncomingAdapterListener incomingAdapterListener) {
        this.ctx = ctx;
        resources = ctx.getResources();
        this.listener = incomingAdapterListener;
    }

    public void setIncomingList(List<Incoming> incomings) {
        this.incomingList = incomings;
        //setovanje da su svi zatvoreni inicijalno
        for (int i = 0; i < incomingList.size(); i++) {
            expandState.append(i, false);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemIncomingBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incoming incoming = incomingList.get(position);
        holder.mBinding.incomingCodeTv.setText(resources.getString(R.string.code, incoming.getIncomingID()));
     //   holder.mBinding.incomingCodeTv.setText(resources.getString(R.string.transport_no, incoming.getTransportNo()));
        holder.mBinding.docDateTv.setText(resources.getString(R.string.document_date, Utility.getStringFromDate(incoming.getDocDate(), true)));
        holder.mBinding.incomingStatusTv.setText(resources.getString(R.string.status, getIncomingStatusName(incoming.getIncomingStatusCode())));
        holder.mBinding.descriptionTv.setText(resources.getString(R.string.total_num_of_prod, incoming.getTotalNumOfProd()));
        holder.mBinding.outgoingPeriodTv.setText(resources.getString(R.string.date, Utility.getStringFromDate(incoming.getIncomingDate(), true)));
        holder.mBinding.outgoingNumBigProdTv.setText(resources.getString(R.string.partner, incoming.getPartnerName()));
        holder.mBinding.outgoingNumSmallProdTv.setText(incoming.getPartnerWarehouseName());
        holder.mBinding.totalNumOfProdTv.setText(resources.getString(R.string.address, incoming.getPartnerAddress()));

        holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, getColorByStatusCode(incoming.getIncomingStatusCode())));

        if (incoming.getTransportNo().isEmpty()) {
            holder.mBinding.incomingTypeTv.setText("Transport: N/A");
        } else {
            holder.mBinding.incomingTypeTv.setText("Transport: " + incoming.getTransportNo());
        }
//        if (incoming.getIncomingStatusCode().equals("03")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.color_incoming_inProgress));
//        } else if (incoming.getIncomingStatusCode().equals("04")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.medium_green));
//        } else if (incoming.getIncomingStatusCode().equals("05")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.color_incoming_return));
//        } else if (incoming.getIncomingStatusCode().equals("08")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.colorAccentDark));
//        } else {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.white));
//        }


        //provera da li je otvoren
        final boolean isExpanded = expandState.get(position);
        holder.mBinding.outgoingGroupedExpandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.mBinding.outgoingGroupedButtonLayout.setRotation(expandState.get(position) ? 180f : 0f);
    }

    private int getColorByStatusCode(String statusCode) {
        int colorResourceID;
        switch (statusCode) {
            case "03":
                colorResourceID = R.color.color_incoming_inProgress;
                break;
            case "04":
                colorResourceID = R.color.medium_green;
                break;
            case "05":
                colorResourceID = R.color.color_incoming_return;
                break;
            case "07":
                colorResourceID = R.color.color_incoming_canceled;
                break;
            case "08":
                colorResourceID = R.color.colorAccentDark;
                break;
            default:
                colorResourceID = R.color.white;
                break;
        }
        return colorResourceID;
    }

    @Override
    public int getItemCount() {
        return incomingList == null ? 0 : incomingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemIncomingBinding mBinding;

        public ViewHolder(@NonNull ItemIncomingBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.outgoingGroupedCard.setOnClickListener(view -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onIncomingClicked(incomingList.get(getAdapterPosition()));
                }
            });
            mBinding.outgoingGroupedButtonLayout.setOnClickListener(view -> {
                onClickButton(binding.outgoingGroupedExpandableLayout, binding.outgoingGroupedButtonLayout, binding.outgoingGroupedButtonImage, getAdapterPosition());
            });
        }
    }

    private void onClickButton(final LinearLayout expandableLayout, final LinearLayout buttonLayout, final View buttonImage, final int i) {

        if (expandableLayout.getVisibility() == View.VISIBLE) {
            createRotateAnimator(buttonImage, 180f, 0f).start();
            expandableLayout.setVisibility(View.GONE);
            expandState.put(i, false);
        } else {
            createRotateAnimator(buttonImage, 0f, 180f).start();
            expandableLayout.setVisibility(View.VISIBLE);
            expandState.put(i, true);
        }
    }

    private ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    private String getIncomingStatusName(String statusCode) {
        switch (statusCode) {
            case "03":
                return resources.getString(R.string.finished_partially);
            case "04":
                return resources.getString(R.string.finished);
            case "05":
                return resources.getString(R.string.returned);
            case "06":
                return resources.getString(R.string.sent_to_erp);
            case "07":
                return resources.getString(R.string.declined);
            case "08":
                return resources.getString(R.string.finished_with_surplus);
            case "02":
            default:
                return resources.getString(R.string.active);
        }
    }

    public interface IncomingAdapterListener {
        void onIncomingClicked(Incoming incoming);
    }
}
