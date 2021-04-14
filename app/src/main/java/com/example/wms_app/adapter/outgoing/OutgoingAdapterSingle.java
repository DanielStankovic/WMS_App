package com.example.wms_app.adapter.outgoing;

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
import com.example.wms_app.databinding.ItemOutgoingBinding;
import com.example.wms_app.model.Outgoing;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.InternetCheck;
import com.example.wms_app.utilities.Utility;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class OutgoingAdapterSingle extends RecyclerView.Adapter<OutgoingAdapterSingle.ViewHolder> {

    private final Resources resources;
    private List<Outgoing> outgoingList;
    private SparseBooleanArray expandState = new SparseBooleanArray();
    private final OutgoingAdapterSingleListener listener;
    private final Context context;

    public OutgoingAdapterSingle(Context ctx, OutgoingAdapterSingleListener listener) {
        this.listener = listener;
        resources = ctx.getResources();
        this.context = ctx;
    }

    public void setOutgoingList(List<Outgoing> outgoingList) {
        this.outgoingList = outgoingList;
        //setovanje da su svi zatvoreni inicijalno
        for (int i = 0; i < outgoingList.size(); i++) {
            expandState.append(i, false);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false), context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outgoing outgoing = outgoingList.get(position);
        holder.mBinding.incomingCodeTv.setText(resources.getString(R.string.code, outgoing.getOutgoingCode()));
        holder.mBinding.docDateTv.setText(resources.getString(R.string.document_date, Utility.getStringFromDate(outgoing.getDocDate(), true)));
       // holder.mBinding.totalNumOfProdTv.setText(resources.getString(R.string.total_num_of_prod, outgoing.getTotalNumOfProd()));
        holder.mBinding.totalNumOfProdTv.setText(resources.getString(R.string.address, outgoing.getPartnerAddress()));

        holder.mBinding.outgoingPeriodTv.setText(resources.getString(R.string.date, Utility.getStringFromDate(outgoing.getOutgoingDate(), true)));
        holder.mBinding.outgoingNumBigProdTv.setText(resources.getString(R.string.partner, outgoing.getPartnerName()));
     //   holder.mBinding.outgoingNumSmallProdTv.setText(resources.getString(R.string.address, outgoing.getPartnerAddress()));
        holder.mBinding.outgoingNumSmallProdTv.setText(outgoing.getPartnerWarehouseName());
        holder.mBinding.descriptionTv.setText(resources.getString(R.string.total_num_of_prod, outgoing.getTotalNumOfProd()));


        holder.mBinding.incomingTypeTv.setText(resources.getString(R.string.transport_no, outgoing.getTransportNo()));
        holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, getColorByStatusCode(outgoing.getOutgoingStatusCode())));
        holder.mBinding.incomingStatusTv.setText(resources.getString(R.string.status, getIncomingStatusName(outgoing.getOutgoingStatusCode())));


        if (outgoing.getTransportNo().isEmpty()) {
            holder.mBinding.incomingTypeTv.setText("Transport: N/A");
        } else {
            holder.mBinding.incomingTypeTv.setText("Transport: " + outgoing.getTransportNo());
        }


//        if (outgoing.getOutgoingStatusCode().equals("03")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_incoming_inProgress));
//        } else if (outgoing.getOutgoingStatusCode().equals("04")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.medium_green));
//        } else if (outgoing.getOutgoingStatusCode().equals("05")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_incoming_return));
//        } else if (outgoing.getOutgoingStatusCode().equals("08")) {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentDark));
//        } else {
//            holder.mBinding.outgoingGroupedCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
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
            default:
                colorResourceID = R.color.white;
                break;
        }
        return colorResourceID;
    }

    @Override
    public int getItemCount() {
        return outgoingList == null ? 0 : outgoingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ItemOutgoingBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingBinding binding, Context ctx) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.outgoingGroupedCard.setOnClickListener(view -> {
                new InternetCheck(internet -> {
                    if (internet) {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            listener.onOutgoingClicked(outgoingList.get(getAdapterPosition()));
                        }
                    } else {
                        DialogBuilder.showNoInternetDialog(ctx);
                    }
                }, ctx);

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

    public interface OutgoingAdapterSingleListener {
        void onOutgoingClicked(Outgoing outgoing);
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

}
