package com.example.wms_app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.wms_app.R;
import com.example.wms_app.enums.EnumSpinnerViewType;

public class DocumentStyleAdapter extends ArrayAdapter<String> {

    private final String[] docTypes;
    private final int dropDownViewWidth;

    public DocumentStyleAdapter(@NonNull Context context, int resource, String[] documentTypes) {
        super(context, resource, documentTypes);
        this.docTypes = documentTypes;
        this.dropDownViewWidth = (int) ((Resources.getSystem().getDisplayMetrics().widthPixels) * 0.7);
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
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_doc_type, parent, false);
        final TextView docTypeNameTv = row.findViewById(R.id.docTypeNameTv);
        final ImageView docTypeIv = row.findViewById(R.id.docTypeIv);
        final View separatorLine = row.findViewById(R.id.spinnerSeparatorLine);

        if (viewType == EnumSpinnerViewType.NORMAL) {
            separatorLine.setVisibility(View.INVISIBLE);
            docTypeNameTv.setVisibility(View.GONE);
            docTypeIv.setColorFilter(Color.argb(255, 255, 255, 255));
        } else {
            separatorLine.setVisibility(View.VISIBLE);
            docTypeNameTv.setVisibility(View.VISIBLE);
            row.getLayoutParams().width = dropDownViewWidth;
        }

        docTypeNameTv.setText(docTypes[position]);
        int imageResourceID = position == 0 ? R.drawable.ic_box : R.drawable.ic_boxes;

        Glide.with(parent.getContext()).load(imageResourceID).into(docTypeIv);

        return row;
    }
}
