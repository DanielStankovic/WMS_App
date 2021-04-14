package com.example.wms_app.model;

import com.example.wms_app.enums.EnumViewType;

public class ViewEnableHelper {
    private int viewID;
    private String viewText;
    private boolean isEnabled;
    private int mViewVisibility;
    private EnumViewType enumViewType;

    public ViewEnableHelper(int viewID, String viewText, boolean isEnabled, EnumViewType enumViewType, int viewVisibility) {
        this.viewID = viewID;
        this.viewText = viewText;
        this.isEnabled = isEnabled;
        this.enumViewType = enumViewType;
        this.mViewVisibility = viewVisibility;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }

    public String getViewText() {
        return viewText;
    }

    public void setViewText(String viewText) {
        this.viewText = viewText;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public EnumViewType getEnumViewType() {
        return enumViewType;
    }

    public void setEnumViewType(EnumViewType enumViewType) {
        this.enumViewType = enumViewType;
    }

    public int getmViewVisibility() {
        return mViewVisibility;
    }

    public void setmViewVisibility(int mViewVisibility) {
        this.mViewVisibility = mViewVisibility;
    }
}
