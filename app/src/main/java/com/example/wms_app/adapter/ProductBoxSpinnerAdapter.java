package com.example.wms_app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.wms_app.R;
import com.example.wms_app.enums.EnumSpinnerViewType;
import com.example.wms_app.model.ProductBox;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProductBoxSpinnerAdapter extends ArrayAdapter<ProductBox> {
    private final List<ProductBox> productBoxList;
    private final int dropDownViewWidth;
    private final boolean isSpinnerForPhaseTwo;
    private final int phaseTwoTextViewHeight;

    public ProductBoxSpinnerAdapter(@NonNull Context context, int resource, List<ProductBox> list, boolean isSpinnerForPhaseTwo) {
        super(context, resource, list);
        this.productBoxList = list;
        this.dropDownViewWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.isSpinnerForPhaseTwo = isSpinnerForPhaseTwo;
        phaseTwoTextViewHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, EnumSpinnerViewType.DROP_DOWN);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, EnumSpinnerViewType.NORMAL);
    }

    private View getCustomView(final int position, View convertView, ViewGroup parent, EnumSpinnerViewType viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_product, parent, false);
        RelativeLayout relativeLayout = row.findViewById(R.id.spinnerRelativeLayout);
        final TextView spinnerProdNameTv = row.findViewById(R.id.spinnerProdNameTv);
        final View separatorLine = row.findViewById(R.id.spinnerSeparatorLine);

        ProductBox productBox = productBoxList.get(position);

        if (viewType == EnumSpinnerViewType.DROP_DOWN) {
            row.getLayoutParams().width = dropDownViewWidth;
        }
        if (viewType == EnumSpinnerViewType.NORMAL) {
            separatorLine.setVisibility(View.INVISIBLE);
        }

        if (viewType == EnumSpinnerViewType.NORMAL && isSpinnerForPhaseTwo) {
            row.getLayoutParams().height = phaseTwoTextViewHeight;
        }
        if (productBox.getProductBoxCode() == null)
            spinnerProdNameTv.setText(productBox.getProductBoxName());
        else
            spinnerProdNameTv.setText(productBox.getProductBoxCode() + ": " + productBox.getProductBoxName());

        if (productBox.getColorStatus() == 1) {
            relativeLayout.setBackgroundColor(Color.parseColor("#ebb734"));
            spinnerProdNameTv.setTextColor(Color.BLACK);
        } else if (productBox.getColorStatus() == 2) {
            relativeLayout.setBackgroundColor(Color.parseColor("#088A24"));
            spinnerProdNameTv.setTextColor(Color.WHITE);
        } else {
            relativeLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            spinnerProdNameTv.setTextColor(Color.BLACK);
        }

        return row;
    }

}
