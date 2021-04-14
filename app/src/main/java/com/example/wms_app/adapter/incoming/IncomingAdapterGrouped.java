package com.example.wms_app.adapter.incoming;

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
import com.example.wms_app.databinding.ItemIncomingGroupedBinding;
import com.example.wms_app.databinding.ItemOutgoingGroupedBinding;
import com.example.wms_app.model.IncomingGrouped;
import com.example.wms_app.utilities.DialogBuilder;
import com.example.wms_app.utilities.InternetCheck;

public class IncomingAdapterGrouped extends RecyclerView.Adapter<IncomingAdapterGrouped.ViewHolder> {

    private final Resources resources;
    private final Context context;
    private boolean isExpanded = false;
    private IncomingGrouped incomingGrouped;
    private final IncomingAdapterGroupedListener listener;

    public IncomingAdapterGrouped(Context context, IncomingAdapterGroupedListener listener) {
        resources = context.getResources();
        this.context = context;
        this.listener = listener;
    }

    public void setIncomingGrouped(IncomingGrouped incomingGrouped) {
        this.incomingGrouped = incomingGrouped;
        isExpanded = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Koristim isti item za binding posto je sve isto kao kod grupne otpreme
        return new ViewHolder(ItemIncomingGroupedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mBinding.incomingPeriodTv.setText(resources.getString(R.string.period, incomingGrouped.getPeriod()));
        holder.mBinding.incomingTotalProdNum.setText(resources.getString(R.string.total_num_of_prod, incomingGrouped.getTotalNumOfProds()));
        holder.mBinding.incomingTotalIncomingNum.setText(resources.getString(R.string.total_num_of_inc, incomingGrouped.getTotalNumOfIncomings()));
        holder.mBinding.prodNamesTv.setText(incomingGrouped.getProductNames());
        holder.mBinding.incomingCodesTv.setText(incomingGrouped.getIncomingCodes());

        holder.mBinding.incomingGroupedExpandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.mBinding.outgoingGroupedButtonLayout.setRotation(isExpanded ? 180f : 0f);
    }

    @Override
    public int getItemCount() {
        return incomingGrouped == null ? 0 : 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Koristim isti item za binding posto je sve isto kao kod grupne otpreme
        private final ItemIncomingGroupedBinding mBinding;

        public ViewHolder(@NonNull ItemIncomingGroupedBinding binding, Context ctx) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.incomingGroupedCard.setOnClickListener(view -> new InternetCheck(internet -> {
                if (internet) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onIncomingGroupedClicked(incomingGrouped);
                    }
                } else {
                    DialogBuilder.showNoInternetDialog(ctx);
                }
            }, ctx));
            mBinding.outgoingGroupedButtonLayout.setOnClickListener(view -> onClickButton(binding.incomingGroupedExpandableLayout, binding.incomingGroupedButtonImage));

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

    public interface IncomingAdapterGroupedListener {
        void onIncomingGroupedClicked(IncomingGrouped incomingGrouped);
    }
}
