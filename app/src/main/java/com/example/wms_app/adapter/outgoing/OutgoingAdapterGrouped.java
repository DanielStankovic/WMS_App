package com.example.wms_app.adapter.outgoing;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wms_app.R;
import com.example.wms_app.databinding.ItemOutgoingGroupedBinding;
import com.example.wms_app.model.OutgoingGrouped;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.InternetCheck;

public class OutgoingAdapterGrouped extends RecyclerView.Adapter<OutgoingAdapterGrouped.ViewHolder> {

    private final Resources resources;
    private final Context context;
    private OutgoingGrouped outgoingGrouped;
    private boolean isExpanded = false;
    private final OutgoingAdapterGroupedListener listener;

    public OutgoingAdapterGrouped(Context context, OutgoingAdapterGroupedListener listener) {
        resources = context.getResources();
        this.listener = listener;
        this.context = context;
    }

    public void setOutgoingGrouped(OutgoingGrouped outgoingGrouped) {
        this.outgoingGrouped = outgoingGrouped;
        isExpanded = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOutgoingGroupedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mBinding.outgoingPeriodTv.setText(resources.getString(R.string.period, outgoingGrouped.getPeriod()));
        holder.mBinding.outgoingTotalProdNum.setText(resources.getString(R.string.total_num_of_prod, outgoingGrouped.getTotalNumOfProds()));
        holder.mBinding.outgoingTotalOutgoingNum.setText(resources.getString(R.string.total_num_of_outgoing, outgoingGrouped.getTotalNumOfOutgoings()));
        holder.mBinding.prodNamesTv.setText(outgoingGrouped.getProductNames());
        holder.mBinding.outgoingCodesTv.setText(outgoingGrouped.getOutgoingCodes());

        holder.mBinding.outgoingGroupedExpandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.mBinding.outgoingGroupedButtonLayout.setRotation(isExpanded ? 180f : 0f);
    }

    @Override
    public int getItemCount() {
        return outgoingGrouped == null ? 0 : 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOutgoingGroupedBinding mBinding;

        public ViewHolder(@NonNull ItemOutgoingGroupedBinding binding, Context ctx) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.outgoingGroupedCard.setOnClickListener(view -> new InternetCheck(internet -> {
                if (internet) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onOutgoingGroupedClicked(outgoingGrouped);
                    }
                } else {
                    DialogBuilder.showNoInternetDialog(ctx);
                }
            }, ctx));
            mBinding.outgoingGroupedButtonLayout.setOnClickListener(view -> onClickButton(binding.outgoingGroupedExpandableLayout, binding.outgoingGroupedButtonImage));
        }
    }

    private void onClickButton(final LinearLayout expandableLayout, final View buttonImage) {

        if (expandableLayout.getVisibility() == View.VISIBLE) {
            createRotateAnimator(buttonImage, 180f, 0f).start();
            expandableLayout.setVisibility(View.GONE);
            isExpanded = false;
        } else {
            createRotateAnimator(buttonImage, 0f, 180f).start();
            expandableLayout.setVisibility(View.VISIBLE);
            isExpanded = true;
        }
    }

    private ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    public interface OutgoingAdapterGroupedListener {
        void onOutgoingGroupedClicked(OutgoingGrouped outgoingGrouped);
    }
}
